package im.zhaojun.zfile.home.chain.command;

import im.zhaojun.zfile.home.chain.FileContext;
import im.zhaojun.zfile.home.model.request.FileListRequest;
import im.zhaojun.zfile.admin.service.FilterConfigService;
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

		filterConfigService.checkPathPermission(storageId, fileListRequest.getPath());
		return false;
	}

}