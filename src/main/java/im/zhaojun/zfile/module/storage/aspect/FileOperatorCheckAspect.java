package im.zhaojun.zfile.module.storage.aspect;

import im.zhaojun.zfile.core.exception.biz.StorageSourceIllegalOperationBizException;
import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.OnlyOfficeKeyCacheUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.link.model.request.BatchGenerateLinkRequest;
import im.zhaojun.zfile.module.onlyoffice.model.OnlyOfficeFile;
import im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 根据权限设置, 校验文件操作权限
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
public class FileOperatorCheckAspect {

	@Resource
	private StorageSourceService storageSourceService;

	@Resource
	private UserStorageSourceService userStorageSourceService;

	/**
	 * 生成直链权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("@annotation(storagePermissionCheck)")
	public Object annotationCheck(ProceedingJoinPoint point, StoragePermissionCheck storagePermissionCheck) throws Throwable {
		FileOperatorTypeEnum action = storagePermissionCheck.action();
		if (action == FileOperatorTypeEnum.LINK) {
			return linkActionCheck(point);
		}

		return point.proceed();
	}

	public Object linkActionCheck(ProceedingJoinPoint point) throws Throwable {
		FileOperatorTypeEnum action = FileOperatorTypeEnum.LINK;
		Object arg = point.getArgs()[0];
		String storageKey = (arg instanceof BatchGenerateLinkRequest) ?((BatchGenerateLinkRequest) arg).getStorageKey() : (String) arg;
		Integer storageId = storageSourceService.findIdByKey(storageKey);

		boolean allowAccess = allowAccess(storageId, action);
		if (allowAccess) {
			return point.proceed();
		} else {
			throw new StorageSourceIllegalOperationBizException(storageId, action);
		}
	}

	/**
	 * 存储源是否可用权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.fileList(..)) || " +
			"execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.getFileItem(..))")
	public Object availableAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.AVAILABLE);
		return point.proceed();
	}

	/**
	 * 新建文件/文件夹权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.newFolder(..))")
	public Object newFolderAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.NEW_FOLDER);
		return point.proceed();
	}

	/**
	 * 删除文件/文件夹权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.delete*(..))")
	public Object deleteAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.DELETE);

		Object result = point.proceed();

		boolean isFolder = point.getSignature().getName().equals("deleteFolder");
		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		String path = (String) point.getArgs()[0];
		String name = (String) point.getArgs()[1];
		String currentUserBasePath = targetService.getCurrentUserBasePath();
		String fullPath = StringUtils.concat(currentUserBasePath, path, name);
		clearOnlyOfficeCache(fullPath, targetService.storageId, isFolder);

		return result;
	}

	/**
	 * 获取文件上传地址校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.getUploadUrl(..)) || " +
			"execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService.uploadFile(..))")
	public Object uploadAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.UPLOAD);

		Object[] args = point.getArgs();
		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		String currentUserBasePath = targetService.getCurrentUserBasePath();

		String fullPath;
		String methodName = point.getSignature().getName();
		if (Objects.equals(methodName, "getUploadUrl")) {
			fullPath = StringUtils.concat(currentUserBasePath, (String) args[0], (String) args[1]);
		} else if (Objects.equals(methodName, "uploadFile")) {
			fullPath = StringUtils.concat(currentUserBasePath, (String) args[0]);
		} else {
			throw new IllegalArgumentException("上传校验异常.");
		}

		Object result = point.proceed();
		clearOnlyOfficeCache(fullPath, targetService.storageId, false);

		return result;
	}

	/**
	 * 重命名文件/文件夹权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.rename*(..))")
	public Object renameAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.RENAME);

		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		String currentUserBasePath = targetService.getCurrentUserBasePath();

		Object[] args = point.getArgs();
		String path = (String) args[0];
		String name = (String) args[1];
		String newName = (String) args[2];
		String sourceFullPath = StringUtils.concat(currentUserBasePath, path, name);
		String targetFullPath = StringUtils.concat(currentUserBasePath, path, newName);

		Object result = point.proceed();
		clearOnlyOfficeCache(sourceFullPath, targetService.storageId, Objects.equals(point.getSignature().getName(), "renameFolder"));

		return result;
	}

	/**
	 * 移动权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.move*(..))")
	public Object moveAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.MOVE);
		Object result = point.proceed();

		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		String path = (String) point.getArgs()[0];
		String name = (String) point.getArgs()[1];
		String currentUserBasePath = targetService.getCurrentUserBasePath();
		String fullPath = StringUtils.concat(currentUserBasePath, path, name);
		clearOnlyOfficeCache(fullPath, targetService.storageId, Objects.equals(point.getSignature().getName(), "moveFolder"));

		return result;
	}

	/**
	 * 复制权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.copy*(..))")
	public Object copyAround(ProceedingJoinPoint point) throws Throwable {
		checkPermission(point, FileOperatorTypeEnum.COPY);
		return point.proceed();
	}

	/**
	 * 校验是否有此文件操作的权限
	 *
	 * @param   point
	 *          连接点
	 *
	 * @param   fileOperatorType
	 *          文件操作类型
	 */
	private void checkPermission(ProceedingJoinPoint point, FileOperatorTypeEnum fileOperatorType) {
		// 获取对应的存储源 service
		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		Integer storageId = targetService.storageId;

		boolean allowAccess = allowAccess(storageId, fileOperatorType);

		if (BooleanUtils.isFalse(allowAccess)) {
			throw new StorageSourceIllegalOperationBizException(storageId, fileOperatorType);
		}
	}


	private boolean allowAccess(Integer storageId, FileOperatorTypeEnum fileOperatorType) {
		User currentUser = ZFileAuthUtil.getCurrentUser();
		if (BooleanUtils.isNotTrue(currentUser.getEnable())) {
			return false;
		}

		UserStorageSource userStorageSource = userStorageSourceService.getByUserIdAndStorageId(ZFileAuthUtil.getCurrentUserId(), storageId);

		// 如果未授权该存储源，则默认禁止所有类型的操作
		Boolean enable = userStorageSource.getEnable();
		if (enable == null || !enable) {
			return false;
		}

		if (fileOperatorType == FileOperatorTypeEnum.AVAILABLE) {
			return true;
		}

		Set<String> permissions = userStorageSource.getPermissions();
		return permissions.contains(fileOperatorType.getValue());
	}

	/**
	 * 清除 OnlyOffice 缓存
	 *
	 * @param 	fullPath
	 * 			文件全路径(包含用户路径)
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 */
	private void clearOnlyOfficeCache(String fullPath, Integer storageId, boolean isFolder) {
		try {
			String storageKey = storageSourceService.findStorageKeyById(storageId);
			if (isFolder) {
				List<OnlyOfficeFile> caches = OnlyOfficeKeyCacheUtils.removeByFolder(new OnlyOfficeFile(storageKey, fullPath));
				if (CollectionUtils.isNotEmpty(caches)) {
					log.debug("删除/重命名文件夹时, 清除 OnlyOffice 缓存 {} 个", caches);
				}
			} else {
				OnlyOfficeFile onlyOfficeFile = OnlyOfficeKeyCacheUtils.removeByFile(new OnlyOfficeFile(storageKey, fullPath));
				if (onlyOfficeFile != null) {
					log.debug("删除/重命名文件时, 清除 OnlyOffice 缓存: {}", onlyOfficeFile);
				}
			}
		} catch (Exception e) {
			log.error("清除 OnlyOffice 缓存失败", e);
		}
	}

}