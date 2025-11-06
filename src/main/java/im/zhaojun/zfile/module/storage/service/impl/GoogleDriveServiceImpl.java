package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageConfigConstant;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.dto.RefreshTokenInfoDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.GoogleDriveParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.oauth2.service.IOAuth2Service;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class GoogleDriveServiceImpl extends AbstractProxyTransferService<GoogleDriveParam> implements RefreshTokenService {

	/**
	 * 文件类型：文件夹
	 */
	private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

	/**
	 * 文件类型：快捷方式
	 */
	private static final String SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut";

	/**
	 * 文件基础操作 API
	 */
	private static final String DRIVE_FILE_URL = "https://www.googleapis.com/drive/v3/files";


	/**
	 * 文件上传操作 API
	 */
	private static final String DRIVE_FILE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";

	/**
	 * 刷新 AccessToken URL
	 */
	private static final String REFRESH_TOKEN_URL = "https://oauth2.googleapis.com/token";

	@jakarta.annotation.Resource
	private StorageSourceConfigService storageSourceConfigService;

	@Override
	public void init() {
		Integer refreshTokenExpiredAt = param.getRefreshTokenExpiredAt();
		if (refreshTokenExpiredAt == null) {
			refreshAccessToken();
		} else {
			RefreshTokenInfoDTO tokenInfoDTO = RefreshTokenInfoDTO.success(param.getAccessToken(), param.getRefreshToken(), refreshTokenExpiredAt);
			RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));
		}
	}

	private String getIdByPath(String path) {
		return getIdByPath(path, true);
	}

	/**
	 * 根据路径获取文件/文件夹 id
	 *
	 * @param 	path
	 * 			路径
	 *
	 * @return	文件/文件夹 id
	 */
	private String getIdByPath(String path, boolean concatCurrentUserBasePath) {
		String fullPath = StringUtils.concat(param.getBasePath(), concatCurrentUserBasePath ? getCurrentUserBasePath() : "", path);
		if (StringUtils.isEmpty(fullPath) || StringUtils.equals(fullPath, StringUtils.SLASH)) {
			return StringUtils.isEmpty(param.getDriveId()) ? "root" : param.getDriveId();
		}

		List<String> pathList = StringUtils.split(fullPath, StringUtils.SLASH, false, true);

		String driveId = "";
		for (String subPath : pathList) {
			String folderIdParam = new GoogleDriveAPIParam().getDriveIdByPathParam(subPath, driveId);
			HttpRequest httpRequest = commonHttpRequest(HttpUtil.createGet(DRIVE_FILE_URL + "?" + folderIdParam));

			HttpResponse httpResponse = httpRequest.execute();

			checkHttpResponseIsError(httpResponse);

			String body = httpResponse.body();

			JSONObject jsonRoot = JSON.parseObject(body);
			JSONArray files = jsonRoot.getJSONArray("files");

			if (files.isEmpty()) {
				throw ExceptionUtil.wrapRuntime(new FileNotFoundException());
			}

			JSONObject jsonLastItem = files.getJSONObject(files.size() - 1);
			if (jsonLastItem.containsKey("shortcutDetails")) {
				driveId = jsonLastItem.getJSONObject("shortcutDetails").getString("targetId");
			} else {
				driveId = jsonLastItem.getString("id");
			}
		}

		return driveId;
	}

	@Override
	public List<FileItemResult> fileList(String folderPath) throws Exception {
		List<FileItemResult> result = new ArrayList<>();

		String folderId = getIdByPath(folderPath);
		String pageToken = "";
		do {
			String folderIdParam = new GoogleDriveAPIParam().getFileListParam(folderId, pageToken);
			HttpRequest httpRequest = commonHttpRequest(HttpUtil.createGet(DRIVE_FILE_URL + "?" + folderIdParam));
			httpRequest.setConnectionTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);

			HttpResponse httpResponse = httpRequest.execute();

			checkHttpResponseIsError(httpResponse);

			String body = httpResponse.body();

			JSONObject jsonObject = JSON.parseObject(body);
			pageToken = jsonObject.getString("nextPageToken");
			JSONArray files = jsonObject.getJSONArray("files");
			result.addAll(jsonArrayToFileList(files, folderPath));
		} while (StringUtils.isNotEmpty(pageToken));

		return result;
	}

	@Override
	public FileItemResult getFileItem(String pathAndName) {
		String fileId = getIdByPath(pathAndName);

		HttpRequest httpRequest = commonHttpRequest(HttpUtil.createGet(DRIVE_FILE_URL + StringUtils.SLASH + fileId));
		httpRequest.body("fields=id,name,mimeType,shortcutDetails,size,modifiedTime");
		HttpResponse httpResponse = httpRequest.execute();

		if (httpResponse.getStatus() == HttpStatus.NOT_FOUND.value()) {
			return null;
		}

		checkHttpResponseIsError(httpResponse);

		String body = httpResponse.body();
		JSONObject jsonObject = JSON.parseObject(body);
		String folderPath = FileUtils.getParentPath(pathAndName);
		return jsonObjectToFileItem(jsonObject, folderPath);
	}


	@Override
	public boolean newFolder(String path, String name) {
		HttpResponse httpResponse = commonHttpRequest(HttpUtil.createPost(DRIVE_FILE_URL))
						.body(new JSONObject()
						.fluentPut("name", name)
						.fluentPut("mimeType", FOLDER_MIME_TYPE)
						.fluentPut("parents", Collections.singletonList(getIdByPath(path)))
						.toJSONString())
				.execute();

		checkHttpResponseIsError(httpResponse);

		return true;
	}

	@Override
	public boolean deleteFile(String path, String name) {
		String pathAndName = StringUtils.concat(path, name);
		HttpResponse httpResponse = commonHttpRequest(HttpUtil.createRequest(Method.DELETE, DRIVE_FILE_URL + StringUtils.SLASH + getIdByPath(pathAndName)))
				.execute();

		checkHttpResponseIsError(httpResponse);

		return true;
	}

	@Override
	public boolean deleteFolder(String path, String name) {
		return deleteFile(path, name);
	}

	@Override
	public boolean renameFile(String path, String name, String newName) {
		String pathAndName = StringUtils.concat(path, name);
		String fileId = getIdByPath(pathAndName);

		HttpResponse httpResponse = commonHttpRequest(HttpUtil.createRequest(Method.PATCH, DRIVE_FILE_URL + StringUtils.SLASH + fileId))
				.body(new JSONObject()
						.fluentPut("name", newName)
						.toJSONString())
				.execute();

		checkHttpResponseIsError(httpResponse);

		return true;
	}

	@Override
	public boolean renameFolder(String path, String name, String newName) {
		return renameFile(path, name, newName);
	}


	@Override
	public String getUploadUrl(String path, String name, Long size) {
		return super.getProxyUploadUrl(path, name);
	}


	@Override
	public void uploadFile(String pathAndName, InputStream inputStream, Long size) {
		String boundary = IdUtil.fastSimpleUUID();
		String fileName = FileUtils.getName(pathAndName);
		String folderName = FileUtils.getParentPath(pathAndName);

		String jsonString = new JSONObject()
				.fluentPut("name", fileName)
				.fluentPut("parents", Collections.singletonList(getIdByPath(folderName)))
				.toJSONString();
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMimeSubtype("related")
				.setBoundary(boundary)
				.addTextBody(boundary, jsonString, ContentType.APPLICATION_JSON)
				.addBinaryBody(boundary, inputStream)
				.build();

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpUriRequest httpUriRequest = RequestBuilder.post(DRIVE_FILE_UPLOAD_URL)
					.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + checkExpiredAndGetAccessToken())
					.setEntity(entity)
					.build();

			CloseableHttpResponse response = httpClient.execute(httpUriRequest);
			checkHttpResponseIsError(response);
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public String getDownloadUrl(String pathAndName) {
		return super.getProxyDownloadUrl(pathAndName);
	}

	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) {
		String fileId = getIdByPath(pathAndName, false);

		HttpServletRequest request = RequestHolder.getRequest();

		HttpRequest httpRequest = commonHttpRequest(HttpUtil.createGet(DRIVE_FILE_URL + StringUtils.SLASH + fileId));
		httpRequest.body("alt=media");
		httpRequest.header(HttpHeaders.RANGE, request.getHeader(HttpHeaders.RANGE));
		HttpResponse httpResponse = httpRequest.executeAsync();
		checkHttpResponseIsError(httpResponse);


		try {
			HttpServletResponse response = RequestHolder.getResponse();
			response.setStatus(httpResponse.getStatus());
			for (Map.Entry<String, List<String>> stringListEntry : httpResponse.headers().entrySet()) {
				String key = stringListEntry.getKey();
				List<String> values = stringListEntry.getValue();
				if (key != null && CollectionUtils.isNotEmpty(values)) {
					response.setHeader(key, stringListEntry.getValue().get(0));
				}
			}
			OutputStream outputStream = response.getOutputStream();
			httpResponse.writeBody(outputStream, true, null);
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
		return null;
	}

	@Override
	public boolean copyFile(String path, String name, String targetPath, String targetName) {
		String srcPathAndName = StringUtils.concat(path, name);
		String srcFileId = getIdByPath(srcPathAndName);

		HttpResponse httpResponse = commonHttpRequest(HttpUtil.createPost(DRIVE_FILE_URL + StringUtils.SLASH + srcFileId + "/copy"))
				.body(new JSONObject()
						.fluentPut("name", targetName)
						.fluentPut("parents", Collections.singletonList(getIdByPath(targetPath)))
						.fluentPut("supportsAllDrives", true)
						.toJSONString())
				.execute();

		checkHttpResponseIsError(httpResponse);

		return true;
	}

	@Override
	public boolean copyFolder(String path, String name, String targetPath, String targetName) {
		throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
	}

	@Override
	public boolean moveFile(String path, String name, String targetPath, String targetName) {
		String pathAndName = StringUtils.concat(path, name);
		String fileId = getIdByPath(pathAndName);

		HttpResponse httpResponse = commonHttpRequest(
				HttpUtil.createRequest(Method.PATCH,
						UrlBuilder.of(DRIVE_FILE_URL + StringUtils.SLASH + fileId)
								.setQuery(UrlQuery.of(new JSONObject()
												.fluentPut("addParents", getIdByPath(targetPath))
												.fluentPut("removeParents", getIdByPath(path))
												.fluentPut("supportsAllDrives", true)
										)
								).build()
				)
		).execute();

		checkHttpResponseIsError(httpResponse);

		return true;
	}

	@Override
	public boolean moveFolder(String path, String name, String targetPath, String targetName) {
		return moveFile(path, name, targetPath, targetName);
	}

	@Override
	public StorageTypeEnum getStorageTypeEnum() {
		return StorageTypeEnum.GOOGLE_DRIVE;
	}

	/**
	 * 根据 RefreshToken 刷新 AccessToken, 返回刷新后的 Token.
	 *
	 * @return  刷新后的 Token
	 */
	public RefreshTokenInfoDTO getRefreshToken() {
		StorageSourceConfig refreshStorageSourceConfig =
				storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);

		String paramStr = "client_id=" + param.getClientId() +
				"&client_secret=" + param.getClientSecret() +
				"&refresh_token=" + refreshStorageSourceConfig.getValue() +
				"&grant_type=refresh_token" +
				"&access_type=offline";

		if (log.isDebugEnabled()) {
			log.debug("{} 尝试刷新令牌, 请求参数: {}", getStorageSimpleInfo(), param);
		}

		HttpRequest post = HttpUtil.createPost(REFRESH_TOKEN_URL + "?" + paramStr);
		post.timeout(5 * 1000);
		HttpResponse response = post.execute();

		String responseBody = response.body();
		int responseStatus = response.getStatus();

		if (log.isDebugEnabled()) {
			log.debug("{} 刷新令牌完成. 响应状态码: {}, 响应体: {}", getStorageSimpleInfo(), responseStatus, responseBody);
		}

		if (response.getStatus() != HttpStatus.OK.value()) {
			throw new SystemException(responseBody);
		}

		JSONObject jsonBody = JSONObject.parseObject(responseBody);
		String accessToken = jsonBody.getString(IOAuth2Service.ACCESS_TOKEN_FIELD_NAME);
		String refreshToken = jsonBody.getString(IOAuth2Service.REFRESH_TOKEN_FIELD_NAME);
		Integer expiresIn = jsonBody.getInteger(IOAuth2Service.EXPIRES_IN_FIELD_NAME);
		return RefreshTokenInfoDTO.success(accessToken, refreshToken, expiresIn);
	}

	/**
	 * 刷新当前存储源 AccessToken
	 */
	@Override
	public void refreshAccessToken() {
		try {
			RefreshTokenInfoDTO tokenInfoDTO = getRefreshToken();

			if (tokenInfoDTO.getAccessToken() == null) {
				throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取到令牌为空.");
			}

			StorageSourceConfig accessTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
			StorageSourceConfig refreshTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
			StorageSourceConfig refreshTokenExpiredAtConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_EXPIRED_AT_KEY);
			accessTokenConfig.setValue(tokenInfoDTO.getAccessToken());
			refreshTokenConfig.setValue(tokenInfoDTO.getRefreshToken());
			refreshTokenExpiredAtConfig.setValue(String.valueOf(tokenInfoDTO.getExpiredAt()));

			storageSourceConfigService.updateBatch(storageId, Arrays.asList(accessTokenConfig, refreshTokenConfig, refreshTokenExpiredAtConfig));
			RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));
		} catch (Exception e) {
			RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.fail("AccessToken 刷新失败: " + e.getMessage()));
			throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取时发生异常.", e);
		}
	}

	/**
	 * 转换 api 返回的 json array 为 zfile 文件对象列表
	 *
	 * @param 	jsonArray
	 * 			api 返回文件 json array
	 *
	 * @param 	folderPath
	 * 			所属文件夹路径
	 *
	 * @return	zfile 文件对象列表
	 */
	public List<FileItemResult> jsonArrayToFileList(JSONArray jsonArray, String folderPath) {
		ArrayList<FileItemResult> fileList = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			fileList.add(jsonObjectToFileItem(jsonArray.getJSONObject(i), folderPath));
		}

		return fileList;
	}


	/**
	 * 转换 api 返回的 json object 为 zfile 文件对象
	 *
	 * @param 	jsonObject
	 * 			api 返回文件 json object
	 *
	 * @param 	folderPath
	 * 			所属文件夹路径
	 *
	 * @return	zfile 文件对象
	 */
	public FileItemResult jsonObjectToFileItem(JSONObject jsonObject, String folderPath) {
		FileItemResult fileItemResult = new FileItemResult();
		fileItemResult.setName(jsonObject.getString("name"));
		fileItemResult.setPath(folderPath);
		fileItemResult.setSize(jsonObject.getLong("size"));

		String mimeType = jsonObject.getString("mimeType");
		if (ObjectUtil.equals(SHORTCUT_MIME_TYPE, mimeType)) {
			JSONObject shortcutDetails = jsonObject.getJSONObject("shortcutDetails");
			mimeType = shortcutDetails.getString("targetMimeType");
		}

		if (StringUtils.equals(mimeType, FOLDER_MIME_TYPE)) {
			fileItemResult.setType(FileTypeEnum.FOLDER);
		} else {
			fileItemResult.setType(FileTypeEnum.FILE);
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName())));
		}

		fileItemResult.setTime(jsonObject.getDate("modifiedTime"));

		if (fileItemResult.getSize() == null) {
			fileItemResult.setSize(-1L);
		}

		return fileItemResult;
	}


	/**
	 * 请求参数类
	 */
	@Data
	class GoogleDriveAPIParam {

		private final Integer DEFAULT_PAGE_SIZE = 1000;

		// 存储源 id
		private String driveId;

		// 是否返回共享驱动器或团队盘的内容
		private boolean includeItemsFromAllDrives;

		// 查询适用的文件分组, 支持 'user', 'drive', 'allDrives'
		private String corpora;

		// 请求的应用程序是否同时支持“我的云端硬盘”和共享云端硬盘
		private boolean supportsAllDrives;

		// 请求的字段
		private String fields;

		// 查询参数
		private String q;

		// 每页多少条
		private Integer pageSize;

		// 下页的页码
		private String pageToken;

		/**
		 * 根据路径获取 id 的 api 请求参数
		 *
		 * @param 	folderPath
		 * 			文件夹路径
		 */
		public String getDriveIdByPathParam(String folderPath, String parentId) {
			GoogleDriveAPIParam googleDriveApiParam = getBasicParam();

			String parentIdParam = "";

			if (StringUtils.isNotEmpty(parentId)) {
				parentIdParam = "'" + parentId + "' in parents and ";
			}

			folderPath = folderPath.replace("'", "\\'");

			googleDriveApiParam.setFields("files(id,shortcutDetails)");
			googleDriveApiParam.setQ(parentIdParam + "name = '" + folderPath + "' and trashed = false");

			return googleDriveApiParam.toString();
		}


		/**
		 * 根据路径获取 id 的 api 请求参数
		 *
		 * @param 	folderId
		 * 			google drive 文件夹 id
		 *
		 * @param   pageToken
		 * 			分页 token
		 */
		public String getFileListParam(String folderId, String pageToken) {
			GoogleDriveAPIParam googleDriveAPIParam = getBasicParam();

			googleDriveAPIParam.setFields("files(id,name,mimeType,shortcutDetails,size,modifiedTime),nextPageToken");
			googleDriveAPIParam.setQ("'" + folderId + "' in parents and trashed = false");
			googleDriveAPIParam.setPageToken(pageToken);
			googleDriveAPIParam.setPageSize(DEFAULT_PAGE_SIZE);
			return googleDriveAPIParam.toString();
		}


		/**
		 * 根据关键字和路径搜索文件 api 请求参数
		 *
		 * @param 	folderId
		 * 			搜索的父文件夹 id
		 *
		 * @param   pageToken
		 * 			分页 token
		 *
		 * @param 	keyword
		 * 			搜索关键字
		 */
		public String getSearchParam(String folderId, String pageToken, String keyword) {
			GoogleDriveAPIParam googleDriveAPIParam = getBasicParam();

			String parentIdParam = "";
			if (StringUtils.isNotEmpty(folderId)) {
				parentIdParam = "'" + folderId + "' in parents and ";
			}

			googleDriveAPIParam.setFields("files(id,name,mimeType,shortcutDetails,size,modifiedTime),nextPageToken");
			googleDriveAPIParam.setQ(parentIdParam + " name contains '" + keyword + "' and trashed = false");
			googleDriveAPIParam.setPageToken(pageToken);
			googleDriveAPIParam.setPageSize(DEFAULT_PAGE_SIZE);
			return googleDriveAPIParam.toString();
		}


		/**
		 * 判断是否是团队盘，填充基础参数
		 */
		public GoogleDriveAPIParam getBasicParam() {
			GoogleDriveAPIParam googleDriveAPIParam = new GoogleDriveAPIParam();
			String driveId = param.getDriveId();

			// 判断是否是团队盘，如果是，则需要添加团队盘的参数
			boolean isTeamDrive = StringUtils.isNotEmpty(driveId);

			googleDriveAPIParam.setCorpora("user");
			googleDriveAPIParam.setIncludeItemsFromAllDrives(true);
			googleDriveAPIParam.setSupportsAllDrives(true);
			if (isTeamDrive) {
				googleDriveAPIParam.setDriveId(driveId);
				googleDriveAPIParam.setCorpora("drive");
			}

			return googleDriveAPIParam;
		}

		/**
		 * 请求对象转 url param string
		 *
		 * @return	url param string
		 */
		@Override
		public String toString() {
			return toString(true);
		}

		/**
		 * 请求对象转 url param string
		 *
		 * @return	url param string
		 */
		public String toString(boolean encodeValue) {
			Field[] fields = ReflectUtil.getFields(this.getClass());

			UrlQuery urlQuery = new UrlQuery();

			for (Field field : fields) {
				if (StringUtils.startWith(field.getName(), "this")) {
					continue;
				}
				Object fieldValue = ReflectUtil.getFieldValue(this, field);

				if (ObjectUtil.isNotEmpty(fieldValue) && ObjectUtil.notEqual(fieldValue, false)) {
					urlQuery.add(field.getName(),
							encodeValue ?
									URLEncoder.encode(fieldValue.toString(), StandardCharsets.UTF_8)
									:
									fieldValue.toString()
					);
				}
			}
			return urlQuery.toString();
		}
	}


	/**
	 * 检查 http 响应是否为 5xx, 如果是，则抛出异常
	 *
	 * @param 	httpResponse
	 * 			http 响应
	 */
	private void checkHttpResponseIsError(HttpResponse httpResponse) {
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			int statusCode = httpResponse.getStatus();
			String responseBody = httpResponse.body();
			String msg = String.format("statusCode: %s, responseBody: %s", statusCode, responseBody);
			throw new SystemException(msg);
		}
	}

	/**
	 * 检查 http 响应是否为 5xx, 如果是，则抛出异常  (http client)
	 *
	 * @param 	closeableHttpResponse
	 * 			http 响应
	 */
	private void checkHttpResponseIsError(CloseableHttpResponse closeableHttpResponse) throws IOException {
		int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
		if (HttpStatus.valueOf(statusCode).isError()) {
			HttpEntity responseEntity = closeableHttpResponse.getEntity();
			String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
			String msg = String.format("statusCode: %s, responseBody: %s", statusCode, responseBody);
			throw new SystemException(msg);
		}
	}

	@Override
	public StorageSourceMetadata getStorageSourceMetadata() {
		StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
		storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
		return storageSourceMetadata;
	}

	private HttpRequest commonHttpRequest(HttpRequest httpRequest) {
		String accessToken = checkExpiredAndGetAccessToken();
		httpRequest.bearerAuth(accessToken);
		return httpRequest;
	}

	/**
	 * 检查 AccessToken 是否过期，如果过期则刷新 AccessToken 并返回新的 AccessToken。
	 */
	private String checkExpiredAndGetAccessToken() {
		RefreshTokenCacheBO.RefreshTokenInfo refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);

		if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
			// 使用双重检查锁定机制，确保同一个 storageId 只会有一个线程在刷新 AccessToken
			synchronized (("storage-refresh-" + storageId).intern()) {
				// 双重检查，再次从缓存中获取，确认是否其他线程已经刷新过
				refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
				if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
					log.info("{} AccessToken 未获取或已过期, 尝试刷新.", getStorageSimpleInfo());
					refreshAccessToken();
					refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
				}
			}
		}

		if (refreshTokenInfo == null) {
			throw new SystemException("存储源 " + storageId + " AccessToken 刷新失败: 未找到刷新令牌信息.");
		}

		return refreshTokenInfo.getData().getAccessToken();
	}

}