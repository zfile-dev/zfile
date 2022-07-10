package im.zhaojun.zfile.home.aspect;

import cn.hutool.core.util.BooleanUtil;
import im.zhaojun.zfile.admin.exception.ForbidFileOperationException;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 校验文件操作权限
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
public class FileOperatorCheckAspect {

	@Resource
	private StorageSourceService storageSourceService;

	/**
	 * 新建文件/文件夹权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.newFolder(..))")
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
	@Around("execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.delete*(..))")
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
	@Around("execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.getUploadUrl(..))")
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
	@Around("execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.rename*(..))")
	public Object renameAround(ProceedingJoinPoint point) throws Throwable {
		return check(point, FileOperatorTypeEnum.RENAME);
	}

	/**
	 * 搜索功能权限校验
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around("execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.search(..))")
	public Object searchAround(ProceedingJoinPoint point) throws Throwable {
		return check(point, FileOperatorTypeEnum.SEARCH);
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

		// 判断是否允许文件操作. 如果不允许, 则抛出异常
		StorageSource storageSource = storageSourceService.findById(storageId);
		boolean allowOperator = storageSource.allowOperator();

		if (BooleanUtil.isFalse(allowOperator)) {
			throw new ForbidFileOperationException(storageId, fileOperatorType.getName());
		}

		return point.proceed();
	}

}