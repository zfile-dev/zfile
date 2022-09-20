package im.zhaojun.zfile.module.storage.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.exception.file.operator.StorageSourceFileOperatorException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 文件操作异常包装切面, 用于在存储源实现中不用分别捕获异常并处理，而是在此处统一将文件操作异常包装为 {@link StorageSourceFileOperatorException} 异常
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
@Order(2)
public class FileOperatorExceptionWrapperAspect {
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.newFolder(..))")
	public void newExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String path = getArgStr(point, 0);
		String name = getArgStr(point, 1);
		String errMsg = StrUtil.format("新建文件夹失败, 文件路径: {}, 文件名: {}", path, name);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_NEW_FOLDER_FAIL, storageId, errMsg, error);
	}

	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.delete*(..))")
	public void deleteExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String path = getArgStr(point, 0);
		String name = getArgStr(point, 1);
		String errMsg = StrUtil.format("删除文件/文件夹失败, 文件路径: {}, 文件名: {}", path, name);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_DELETE_FAIL, storageId, errMsg, error);
	}
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.rename*(..))")
	public void renameExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String path = getArgStr(point, 0);
		String name = getArgStr(point, 1);
		String newName = getArgStr(point, 2);
		String errMsg = StrUtil.format("重命名文件/文件夹失败, 文件路径: {}, 原文件名: {}, 修改为: {}", path, name, newName);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_RENAME_FAIL, storageId, errMsg, error);
	}
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.getDownloadUrl(..))")
	public void getDownloadUrl(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String pathAndName = getArgStr(point, 0);
		String errMsg = StrUtil.format("获取下载链接失败, 文件路径: {}", pathAndName);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_GET_UPLOAD_FAIL, storageId, errMsg, error);
	}
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.getUploadUrl(..))")
	public void getUploadUrlExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String path = getArgStr(point, 0);
		String name = getArgStr(point, 1);
		String size = getArgStr(point, 2);
		String errMsg = StrUtil.format("获取文件上传链接失败, 文件路径: {}, 文件名: {}, 文件大小: {}", path, name, size);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_GET_UPLOAD_FAIL, storageId, errMsg, error);
	}
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService.uploadFile(..))")
	public void proxyUploadExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String pathAndName = getArgStr(point, 0);
		String errMsg = StrUtil.format("文件代理上传失败, 文件路径: {}", pathAndName);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_PROXY_UPLOAD_FAIL, storageId, errMsg, error);
	}
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService.downloadToStream(..))")
	public void proxyDownloadExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String pathAndName = getArgStr(point, 0);
		String errMsg = StrUtil.format("文件代理下载失败, 文件路径: {}", pathAndName);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_PROXY_DOWNLOAD_FAIL, storageId, errMsg, error);
	}
	
	
	@AfterThrowing(throwing = "error", value = "execution(public * im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService.getFileItem(..))")
	public void getFileItemExceptionWrapper(JoinPoint point, Throwable error) {
		Integer storageId = getStorageId(point);
		String pathAndName = getArgStr(point, 0);
		String errMsg = StrUtil.format("文件代理下载失败, 文件路径: {}", pathAndName);
		throw new StorageSourceFileOperatorException(CodeMsg.STORAGE_SOURCE_FILE_DISABLE_PROXY_DOWNLOAD, storageId, errMsg, error);
	}
	
	
	/**
	 * 获取存储源 id
	 *
	 * @param 	point
	 * 			切入点
	 *
	 * @return	存储源 id
	 */
	private Integer getStorageId(JoinPoint point) {
		AbstractBaseFileService<?> targetService = (AbstractBaseFileService<?>) point.getTarget();
		return targetService.getStorageId();
	}
	
	/**
	 * 获取切入点方法第 n 个参数
	 *
	 * @param 	point
	 * 			切入点
	 *
	 * @param 	index
	 * 			参数索引
	 *
	 * @return	参数值
	 */
	private Object getArg(JoinPoint point, int index) {
		Object[] args = point.getArgs();
		return ArrayUtil.get(args, index);
	}
	
	
	private String getArgStr(JoinPoint point, int index) {
		Object arg = getArg(point, index);
		return Convert.toStr(arg);
	}
	
}