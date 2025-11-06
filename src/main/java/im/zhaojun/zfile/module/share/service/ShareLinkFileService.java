package im.zhaojun.zfile.module.share.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.share.context.ShareAccessContext;
import im.zhaojun.zfile.module.share.model.dto.ShareEntryDTO;
import im.zhaojun.zfile.module.share.model.entity.ShareLink;
import im.zhaojun.zfile.module.share.model.enums.ShareEntryTypeEnum;
import im.zhaojun.zfile.module.share.model.result.ShareFileInfoResult;
import im.zhaojun.zfile.module.share.model.result.ShareLinkResult;
import im.zhaojun.zfile.module.storage.chain.FileChain;
import im.zhaojun.zfile.module.storage.chain.FileContext;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.request.base.FileListRequest;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分享文件操作服务
 * 专门处理分享文件的访问逻辑，绕过用户基础路径限制
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class ShareLinkFileService {

	@Resource
	private ShareLinkService shareLinkService;

    @Resource
    private FileChain fileChain;

    @Resource
    private UserStorageSourceService userStorageSourceService;

	/**
	 * 获取分享文件列表
	 *
	 * @param shareKey     分享链接 key
	 * @param relativePath 相对路径
	 * @param password     分享密码
	 * @return 分享文件信息
	 */
    public ShareFileInfoResult getShareFileList(String shareKey, String relativePath, String password, String folderPassword, String orderBy, String orderDirection) {
		ShareLink shareLink = getValidShareLink(shareKey);
		
		// 验证密码
		validateSharePassword(shareLink, password);
		
		try {
			// 设置分享访问上下文，getCurrentUserBasePath() 会返回分享的基础路径
            ShareAccessContext.setShareAccess(shareKey, shareLink.getSharePath(), shareLink.getUserId());
			
			AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(shareLink.getStorageKey());
			if (fileService == null) {
				throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
			}
			
		// 根据分享条目获取文件列表
			List<FileItemResult> fileItemList = getFilteredFileList(shareLink, fileService, relativePath);

            // 执行责任链
            FileListRequest fileListRequest = new FileListRequest();
            fileListRequest.setPath(relativePath);
            // 目录密码（与分享密码独立）
            fileListRequest.setPassword(folderPassword);
            fileListRequest.setOrderBy(orderBy);
            fileListRequest.setOrderDirection(orderDirection);
            FileContext fileContext = FileContext.builder()
                    .storageId(fileService.getStorageId())
                    .fileListRequest(fileListRequest)
                    .fileItemList(fileItemList)
                    .fileService(fileService)
                    .operatorUserId(shareLink.getUserId())
                    .build();
            fileChain.execute(fileContext);

            // 构建分享链接信息
			ShareLinkResult shareLinkResult = shareLinkService.getShareLinkInfo(shareKey);
			
			// 更新访问次数（只在访问根路径时更新）
			if (StrUtil.isBlank(relativePath) || "/".equals(relativePath)) {
				shareLinkService.incrementAccessCount(shareKey);
			}
			
			ShareFileInfoResult shareFileInfoResult = new ShareFileInfoResult(fileContext.getFileItemList(), relativePath, shareLinkResult);
			Integer storageId = fileService.getStorageId();
			shareFileInfoResult.setPermission(userStorageSourceService.getPermissionMapByUserIdAndStorageId(shareLink.getUserId(), storageId));
			return shareFileInfoResult;

		} catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.BIZ_SHARE_FILE_LIST_ERROR.setMessage(ErrorCode.BIZ_SHARE_FILE_LIST_ERROR.getMessage() + ": " + e.getMessage()));
        } finally {
			// 确保清理上下文
			ShareAccessContext.clear();
		}
	}

	/**
	 * 获取分享文件的下载地址
	 *
	 * @param shareKey 分享链接 key
	 * @param filePath 文件路径
	 * @param password 分享密码
	 * @return 下载地址
	 */
	public String getShareFileDownloadUrl(String shareKey, String filePath, String password) {
		ShareLink shareLink = getValidShareLink(shareKey);
		
		// 验证密码
		validateSharePassword(shareLink, password);
		
		try {
			AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(shareLink.getStorageKey());
			if (fileService == null) {
				throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
			}
			
			// 更新下载次数
			shareLinkService.incrementDownloadCount(shareKey);
			
			// 获取下载地址，getCurrentUserBasePath() 会返回分享的基础路径
			return fileService.getDownloadUrl(StringUtils.concat(shareLink.getSharePath(), filePath));
			
		} catch (Exception e) {
			throw new BizException(ErrorCode.BIZ_SHARE_FILE_DOWNLOAD_ERROR.setMessage(ErrorCode.BIZ_SHARE_FILE_DOWNLOAD_ERROR.getMessage() + ": " + e.getMessage()));
		} finally {
			ShareAccessContext.clear();
		}
	}

	/**
	 * 获取分享文件项信息
	 *
	 * @param shareKey 分享链接 key
	 * @param filePath 文件路径
	 * @param password 分享密码
	 * @return 文件项信息
	 */
	public FileItemResult getShareFileItem(String shareKey, String filePath, String password) {
		ShareLink shareLink = getValidShareLink(shareKey);
		
		// 验证密码
		validateSharePassword(shareLink, password);
		
		try {
			// 设置分享访问上下文
            ShareAccessContext.setShareAccess(shareKey, shareLink.getSharePath(), shareLink.getUserId());
			
			AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(shareLink.getStorageKey());
			if (fileService == null) {
				throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
			}
			
			// 获取文件信息，getCurrentUserBasePath() 会返回分享的基础路径
			return fileService.getFileItem(filePath);
			
		} catch (Exception e) {
			throw new BizException(ErrorCode.BIZ_SHARE_FILE_INFO_ERROR.setMessage(ErrorCode.BIZ_SHARE_FILE_INFO_ERROR.getMessage() + ": " + e.getMessage()));
		} finally {
			ShareAccessContext.clear();
		}
	}

	/**
	 * 获取有效的分享链接
	 *
	 * @param shareKey 分享链接 key
	 * @return 分享链接
	 */
	private ShareLink getValidShareLink(String shareKey) {
		return shareLinkService.getValidShareLink(shareKey);
	}

	/**
	 * 验证分享密码
	 *
	 * @param shareLink 分享链接
	 * @param password  输入的密码
	 */
	private void validateSharePassword(ShareLink shareLink, String password) {
		// 如果分享设置了密码，则需要验证
		if (StrUtil.isNotBlank(shareLink.getPassword())) {
			if (StrUtil.isBlank(password) || !Objects.equals(shareLink.getPassword(), password)) {
				throw new BizException(ErrorCode.BIZ_SHARE_PASSWORD_ERROR);
			}
		}
	}


	/**
	 * 根据分享条目获取过滤后的文件列表
	 *
	 * @param shareLink    分享链接
	 * @param fileService  文件服务
	 * @param relativePath 相对路径
	 * @return 过滤后的文件列表
	 */
	private List<FileItemResult> getFilteredFileList(ShareLink shareLink, AbstractBaseFileService<?> fileService, String relativePath) throws Exception {
		// 如果是访问根路径且指定了分享条目，需要进行过滤
		if (StrUtil.isBlank(relativePath) || "/".equals(relativePath)) {
			return getFilteredRootFileList(shareLink, fileService);
		}

		// 如果是访问子路径，直接返回该路径下的所有文件
		return fileService.fileList(relativePath);
	}

	/**
	 * 获取过滤后的根路径文件列表
	 *
	 * @param shareLink   分享链接
	 * @param fileService 文件服务
	 * @return 过滤后的文件列表
	 */
	private List<FileItemResult> getFilteredRootFileList(ShareLink shareLink, AbstractBaseFileService<?> fileService) throws Exception {
		// 获取分享路径下的所有文件
		List<FileItemResult> allFiles = fileService.fileList("/");
		
		// 解析分享条目
		List<ShareEntryDTO> shareEntries = parseShareEntries(shareLink.getShareItem());
		
		// 如果没有指定分享项目，返回所有文件
		if (CollectionUtils.isEmpty(shareEntries)) {
			return allFiles;
		}
		
		return filterByShareEntries(allFiles, shareEntries);
	}

	private List<FileItemResult> filterByShareEntries(List<FileItemResult> allFiles, List<ShareEntryDTO> shareEntries) {
		if (CollectionUtils.isEmpty(shareEntries)) {
			return allFiles;
		}

		Set<String> folderNames = shareEntries.stream()
				.filter(entry -> entry.getType() == ShareEntryTypeEnum.FOLDER)
				.map(ShareEntryDTO::getName)
				.collect(Collectors.toSet());

		Set<String> fileNames = shareEntries.stream()
				.filter(entry -> entry.getType() == ShareEntryTypeEnum.FILE)
				.map(ShareEntryDTO::getName)
				.collect(Collectors.toSet());

		return allFiles.stream()
				.filter(file -> {
					if (file.getType() == FileTypeEnum.FOLDER) {
						return folderNames.contains(file.getName());
					}
					return fileNames.contains(file.getName());
				})
				.collect(Collectors.toList());
	}

	private List<ShareEntryDTO> parseShareEntries(String shareItemJson) {
		if (StrUtil.isBlank(shareItemJson)) {
			return List.of();
		}

		try {
			return JSON.parseArray(shareItemJson, ShareEntryDTO.class);
		} catch (Exception e) {
			return List.of();
		}
	}

}
