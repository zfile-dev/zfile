package im.zhaojun.zfile.module.storage.chain.command;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.exception.StorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.module.filter.service.FilterConfigService;
import im.zhaojun.zfile.module.storage.chain.FileContext;
import im.zhaojun.zfile.module.storage.model.request.base.FileListRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 目录访问权限责任链 command 命令
 *      检查请求的目录是否有访问权限
 *
 * @author zhaojun
 */
@Service
public class FileAccessPermissionVerifyCommand implements Command {

	@Resource
	private FilterConfigService filterConfigService;

	/**
	 * 校验是否有权限访问此目录
	 *
	 * @param   context
	 *          文件处理责任链上下文
	 *
	 * @return  是否停止执行责任链, true: 停止执行责任链, false: 继续执行责任链
	 */
	@Override
	public boolean execute(Context context) throws Exception {
		FileContext fileContext = (FileContext) context;
		Integer storageId = fileContext.getStorageId();
		FileListRequest fileListRequest = fileContext.getFileListRequest();
		
		// 检查文件目录是否是不可访问的, 如果是则抛出异常
		boolean isInaccessible = filterConfigService.checkFileIsInaccessible(storageId, fileListRequest.getPath());
		
		if (isInaccessible) {
			String errorMsg = StrUtil.format("文件目录 [{}] 无访问权限", fileListRequest.getPath());
			throw new StorageSourceException(CodeMsg.STORAGE_SOURCE_FILE_FORBIDDEN, storageId, errorMsg);
		}
		
		return false;
	}

}