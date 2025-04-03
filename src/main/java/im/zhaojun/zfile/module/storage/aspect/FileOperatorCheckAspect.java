package im.zhaojun.zfile.module.storage.aspect;

import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.user.model.entity.User;
import org.apache.commons.lang3.BooleanUtils;
import im.zhaojun.zfile.core.exception.biz.StorageSourceIllegalOperationBizException;
import im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
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
	@Around("@annotation(im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck)")
	public Object annotationCheck(ProceedingJoinPoint point) throws Throwable {
		Signature s = point.getSignature();
		MethodSignature ms = (MethodSignature) s;
		Method method = ms.getMethod();
		StoragePermissionCheck storagePermissionCheck = method.getAnnotation(StoragePermissionCheck.class);
		FileOperatorTypeEnum action = storagePermissionCheck.action();
		if (action == FileOperatorTypeEnum.LINK) {
			return linkActionCheck(point);
		}

		return point.proceed();
	}

	public Object linkActionCheck(ProceedingJoinPoint point) throws Throwable {
		FileOperatorTypeEnum action = FileOperatorTypeEnum.LINK;
		Object arg = point.getArgs()[0];
		String storageKey = (String) arg;
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
		return check(point, FileOperatorTypeEnum.AVAILABLE);
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
		return check(point, FileOperatorTypeEnum.NEW_FOLDER);
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
		return check(point, FileOperatorTypeEnum.DELETE);
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
		return check(point, FileOperatorTypeEnum.UPLOAD);
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
		return check(point, FileOperatorTypeEnum.RENAME);
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
		return check(point, FileOperatorTypeEnum.MOVE);
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
		return check(point, FileOperatorTypeEnum.COPY);
	}

	/**
	 * 校验是否有此文件操作的权限
	 *
	 * @param   point
	 *          连接点
	 *
	 * @param   fileOperatorType
	 *          文件操作类型
	 *
	 * @return  方法运行结果
	 */
	private Object check(ProceedingJoinPoint point, FileOperatorTypeEnum fileOperatorType) throws Throwable {
		// 获取对应的存储源 service
		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		Integer storageId = targetService.storageId;

		boolean allowAccess = allowAccess(storageId, fileOperatorType);

		if (BooleanUtils.isFalse(allowAccess)) {
			throw new StorageSourceIllegalOperationBizException(storageId, fileOperatorType);
		}

		return point.proceed();
	}


	private boolean allowAccess(Integer storageId, FileOperatorTypeEnum fileOperatorType) {
		User currentUser = ZFileAuthUtil.getCurrentUser();
		if (BooleanUtils.isNotTrue(currentUser.getEnable())) {
			return false;
		}

		UserStorageSource userStorageSource = userStorageSourceService.getCurrentUserStoragePermission(storageId);

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

}