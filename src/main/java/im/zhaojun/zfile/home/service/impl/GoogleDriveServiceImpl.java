package im.zhaojun.zfile.home.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.admin.constant.StorageConfigConstant;
import im.zhaojun.zfile.admin.model.dto.OAuth2Token;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.admin.model.param.GoogleDriveParam;
import im.zhaojun.zfile.admin.service.StorageSourceConfigService;
import im.zhaojun.zfile.common.cache.RefreshTokenCache;
import im.zhaojun.zfile.common.exception.StorageSourceRefreshTokenException;
import im.zhaojun.zfile.common.exception.file.StorageSourceException;
import im.zhaojun.zfile.common.util.RequestHolder;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.home.service.base.ProxyTransferService;
import im.zhaojun.zfile.home.service.base.RefreshTokenService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class GoogleDriveServiceImpl extends ProxyTransferService<GoogleDriveParam> implements RefreshTokenService {
	
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
	
	@javax.annotation.Resource
	private StorageSourceConfigService storageSourceConfigService;
	
	@Override
	public void init() {
		refreshAccessToken();
	}
	
	/**
	 * 根据路径获取文件/文件夹 id
	 *
	 * @param 	path
	 * 			路径
	 *
	 * @return	文件/文件夹 id
	 */
	private String getIdByPath(String path) {
		String fullPath = StringUtils.concat(param.getBasePath(), path);
		if (StrUtil.isEmpty(fullPath) || StrUtil.equals(fullPath, "/")) {
			return StrUtil.isEmpty(param.getDriveId()) ? "root" : param.getDriveId();
		}
		
		List<String> pathList = StrUtil.splitTrim(fullPath, "/");
		
		String driveId = "";
		for (String subPath : pathList) {
			String folderIdParam = new GoogleDriveAPIParam().getDriveIdByPathParam(subPath, driveId);
			HttpRequest httpRequest = HttpUtil.createGet(DRIVE_FILE_URL);
			httpRequest.header("Authorization", "Bearer " + param.getAccessToken());
			httpRequest.body(folderIdParam);
			
			HttpResponse httpResponse = httpRequest.execute();
			
			if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
				log.info("根据文件夹路径获取文件夹id失败, storageId: {}，folderPath: {}, httpResponse body: {}", storageId, path, httpResponse.body());
				throw new StorageSourceException(storageId, "文件目录不存在或请求异常[1]");
			}
			
			String body = httpResponse.body();
			
			JSONObject jsonObject = JSON.parseObject(body);
			JSONArray files = jsonObject.getJSONArray("files");
			
			if (files.size() == 0) {
				throw new StorageSourceException(storageId, "文件目录不存在或请求异常[2]");
			}
			
			driveId = files.getJSONObject(0).getString("id");
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
			HttpRequest httpRequest = HttpUtil.createGet(DRIVE_FILE_URL);
			httpRequest.header("Authorization", "Bearer " + param.getAccessToken());
			httpRequest.body(folderIdParam);
			
			HttpResponse httpResponse = httpRequest.execute();
			
			if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
				log.info("根据文件夹路径获取文件夹列表失败, storageId: {}，folderPath: {}, httpResponse body: {}", storageId, folderPath, httpResponse.body());
				throw new StorageSourceException(storageId, "文件目录不存在或请求异常[3]");
			}
			
			String body = httpResponse.body();
			
			JSONObject jsonObject = JSON.parseObject(body);
			pageToken = jsonObject.getString("nextPageToken");
			JSONArray files = jsonObject.getJSONArray("files");
			result.addAll(jsonArrayToFileList(files, folderPath));
		} while (StrUtil.isNotEmpty(pageToken));
		
		return result;
	}
	
	@Override
	public FileItemResult getFileItem(String pathAndName) {
		String fileId = getIdByPath(pathAndName);
		
		String folderName = FileUtil.getParent(pathAndName, 1);
		
		HttpRequest httpRequest = HttpUtil.createGet(DRIVE_FILE_URL + "/" + fileId);
		httpRequest.header("Authorization", "Bearer " + param.getAccessToken());
		httpRequest.body("fields=id,name,mimeType,shortcutDetails,size,modifiedTime");
		HttpResponse httpResponse = httpRequest.execute();
		
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			log.info("根据文件路径获取文件详情失败, storageId: {}，pathAndName: {}, httpResponse body: {}", storageId, pathAndName, httpResponse.body());
			throw new StorageSourceException(storageId, "文件不存在或请求异常");
		}
		
		String body = httpResponse.body();
		JSONObject jsonObject = JSON.parseObject(body);
		return jsonObjectToFileItem(jsonObject, folderName);
	}
	
	
	@Override
	public boolean newFolder(String path, String name) {
		HttpResponse httpResponse = HttpRequest.post(DRIVE_FILE_URL)
				.header("Authorization", "Bearer " + param.getAccessToken())
				.body(new JSONObject()
						.fluentPut("name", name)
						.fluentPut("mimeType", FOLDER_MIME_TYPE)
						.fluentPut("parents", Collections.singletonList(getIdByPath(path)))
						.toJSONString())
				.execute();
		
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			log.info("新建文件夹失败, storageId: {}，path: {}, name: {}, httpResponse body: {}", storageId, path, name, httpResponse.body());
			throw new StorageSourceException(storageId, "新建文件夹失败");
		}
		return true;
	}
	
	@Override
	public boolean deleteFile(String path, String name) {
		String pathAndName = StringUtils.concat(path, name);
		HttpResponse httpResponse = HttpRequest.delete(DRIVE_FILE_URL + "/" + getIdByPath(pathAndName))
				.header("Authorization", "Bearer " + param.getAccessToken())
				.execute();
		
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			log.info("删除文件/文件夹失败, storageId: {}，pathAndName: {}, httpResponse body: {}", storageId, pathAndName, httpResponse.body());
			throw new StorageSourceException(storageId, "删除失败");
		}
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
		
		HttpResponse httpResponse = HttpRequest.patch(DRIVE_FILE_URL + "/" + fileId)
				.header("Authorization", "Bearer " + param.getAccessToken())
				.body(new JSONObject()
						.fluentPut("name", newName)
						.toJSONString())
				.execute();
		
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			log.info("重命名文件/文件夹失败, storageId: {}，path: {}, name: {}, httpResponse body: {}", storageId, path, name, httpResponse.body());
			throw new StorageSourceException(storageId, "重命名文件/文件夹失败");
		}
		return true;
	}
	
	@Override
	public boolean renameFolder(String path, String name, String newName) {
		return renameFile(path, name, newName);
	}
	
	@Override
	public void uploadFile(String pathAndName, InputStream inputStream) {
		String boundary = IdUtil.fastSimpleUUID();
		String fileName = FileUtil.getName(pathAndName);
		String folderName = StringUtils.getParentPath(pathAndName);
		
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
					.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + param.getAccessToken())
					.setEntity(entity)
					.build();
			
			CloseableHttpResponse response = httpClient.execute(httpUriRequest);
			StatusLine statusLine = response.getStatusLine();
			if (HttpStatus.valueOf(statusLine.getStatusCode()).isError()) {
				HttpEntity responseEntity = response.getEntity();
				String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
				log.error("上传文件失败, storageId: {}，pathAndName: {}, httpResponse body: {}", storageId, pathAndName, responseBody);
				throw new StorageSourceException(storageId, "上传文件失败:" + responseBody);
			}
		} catch (IOException e) {
			log.error("上传文件失败, storageId: {}，pathAndName: {}, httpResponse body: {}", storageId, pathAndName, e);
			throw new StorageSourceException(storageId, "上传文件失败:" + e.getMessage(), e);
		}
	}
	
	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) {
		String fileId = getIdByPath(pathAndName);
		
		HttpServletRequest request = RequestHolder.getRequest();
		
		HttpRequest httpRequest = HttpUtil.createGet(DRIVE_FILE_URL + "/" + fileId);
		httpRequest.header("Authorization", "Bearer " + param.getAccessToken());
		httpRequest.body("alt=media");
		httpRequest.header(HttpHeaders.RANGE, request.getHeader(HttpHeaders.RANGE));
		HttpResponse httpResponse = httpRequest.executeAsync();
		if (HttpStatus.valueOf(httpResponse.getStatus()).isError()) {
			log.info("GoogleDrive下载文件失败, storageId: {}，pathAndName: {}", storageId, pathAndName);
			throw new StorageSourceException(storageId, "下载文件失败");
		}
		
		try {
			HttpServletResponse response = RequestHolder.getResponse();
			response.setHeader(HttpHeaders.CONTENT_RANGE, httpResponse.header(HttpHeaders.CONTENT_RANGE));
			response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
			response.setHeader(HttpHeaders.CONTENT_LENGTH, httpResponse.header(HttpHeaders.CONTENT_LENGTH));
			response.setContentType(httpResponse.header(HttpHeaders.CONTENT_TYPE));
			OutputStream outputStream = response.getOutputStream();
			httpResponse.writeBody(outputStream, true, null);
		} catch (IOException e) {
			throw new RuntimeException("下载文件失败:" + e.getMessage(), e);
		}
		return null;
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
	public OAuth2Token getRefreshToken() {
		StorageSourceConfig refreshStorageSourceConfig =
				storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
		
		String paramStr = "client_id=" + param.getClientId() +
				"&client_secret=" + param.getClientSecret() +
				"&refresh_token=" + refreshStorageSourceConfig.getValue() +
				"&grant_type=refresh_token" +
				"&access_type=offline";
		
		log.info("{} 尝试刷新令牌, 参数信息为: {}", this, param);
		
		HttpRequest post = HttpUtil.createPost(REFRESH_TOKEN_URL);
		
		post.body(paramStr);
		HttpResponse response = post.execute();
		String body = response.body();
		log.info("{} 尝试刷新令牌成功, 响应信息为: {}", this, body);
		
		JSONObject jsonBody = JSONObject.parseObject(body);
		
		if (response.getStatus() != HttpStatus.OK.value()) {
			return OAuth2Token.fail(param.getClientId(), param.getClientSecret(), param.getRedirectUri(), body);
		}
		
		String accessToken = jsonBody.getString("access_token");
		return OAuth2Token.success(param.getClientId(), param.getClientSecret(), param.getRedirectUri(), accessToken, null, body);
	}
	
	/**
	 * 刷新当前存储源 AccessToken
	 */
	@Override
	public void refreshAccessToken() {
		try {
			OAuth2Token refreshToken = getRefreshToken();
			
			if (refreshToken.getAccessToken() == null) {
				throw new StorageSourceRefreshTokenException("获取 AccessToken 失败, 获取到的令牌为空, 相关诊断信息为: " + refreshToken, storageId);
			}
			
			StorageSourceConfig accessTokenConfig =
					storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
			accessTokenConfig.setValue(refreshToken.getAccessToken());
			
			storageSourceConfigService.updateStorageConfig(Collections.singletonList(accessTokenConfig));
			RefreshTokenCache.putRefreshTokenInfo(storageId, RefreshTokenCache.RefreshTokenInfo.success());
			param.setAccessToken(refreshToken.getAccessToken());
			param.setRefreshToken(refreshToken.getRefreshToken());
			log.info("存储源 {} 刷新 AccessToken 成功", storageId);
		} catch (Exception e) {
			RefreshTokenCache.putRefreshTokenInfo(storageId, RefreshTokenCache.RefreshTokenInfo.fail(getStorageTypeEnum().getDescription() + " AccessToken 刷新失败: " + e.getMessage()));
			throw new StorageSourceRefreshTokenException("存储源 ID: [{}] 刷新 AccessToken 失败", e, storageId);
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
		
		if (StrUtil.equals(mimeType, FOLDER_MIME_TYPE)) {
			fileItemResult.setType(FileTypeEnum.FOLDER);
		} else {
			fileItemResult.setType(FileTypeEnum.FILE);
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, fileItemResult.getName())));
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
			GoogleDriveAPIParam googleDriveAPIParam = getBasicParam();
			
			String parentIdParam = "";
			
			if (StrUtil.isNotEmpty(parentId)) {
				parentIdParam = "'" + parentId + "' in parents and ";
			}
			
			googleDriveAPIParam.setFields("files(id)");
			googleDriveAPIParam.setQ(parentIdParam + " name = '" + folderPath + "'  and trashed = false");
			
			return googleDriveAPIParam.toString();
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
			if (StrUtil.isNotEmpty(folderId)) {
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
			boolean isTeamDrive = StrUtil.isNotEmpty(driveId);
			
			googleDriveAPIParam.setCorpora("user");
			if (isTeamDrive) {
				googleDriveAPIParam.setDriveId(driveId);
				googleDriveAPIParam.setIncludeItemsFromAllDrives(true);
				googleDriveAPIParam.setSupportsAllDrives(true);
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
			Field[] fields = ReflectUtil.getFields(this.getClass());
			
			StringBuilder param = new StringBuilder();
			
			for (Field field : fields) {
				if (StrUtil.startWith(field.getName(), "this")) {
					continue;
				}
				Object fieldValue = ReflectUtil.getFieldValue(this, field);
				
				if (ObjectUtil.isNotEmpty(fieldValue) && ObjectUtil.notEqual(fieldValue, false)) {
					param.append(field.getName()).append("=").append(fieldValue).append("&");
				}
			}
			
			param.deleteCharAt(param.length() - 1);
			return param.toString();
		}
	}
}