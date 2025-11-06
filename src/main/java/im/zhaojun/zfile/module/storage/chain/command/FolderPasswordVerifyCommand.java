package im.zhaojun.zfile.module.storage.chain.command;

import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.storage.chain.FileContext;
import im.zhaojun.zfile.module.storage.model.request.base.FileListRequest;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 校验文件夹密码责任链 command 命令
 *      校验当前请求的文件夹是否需要密码校验，如果需求则校验密码，密码不正确则抛出异常
 *
 * @author zhaojun
 */
@Service
public class FolderPasswordVerifyCommand implements Command {

	@Resource
	private PasswordConfigService passwordConfigService;

    @Resource
    private UserStorageSourceService userStorageSourceService;
	
	/**
	 * 校验当前文件是否需要密码.
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
		String path = fileListRequest.getPath();
		String password = fileListRequest.getPassword();

		AbstractBaseFileService<?> fileService = fileContext.getFileService();
		String fullPath = StringUtils.concat(fileService.getCurrentUserBasePath(), path);

        // 分享模式下，如果分享者拥有忽略密码权限，则跳过目录密码校验
        Integer operatorUserId = fileContext.getOperatorUserId();
        if (operatorUserId != null) {
            boolean ignorePwd = userStorageSourceService.hasUserStorageOperatorPermission(operatorUserId, storageId, FileOperatorTypeEnum.IGNORE_PASSWORD);
            if (ignorePwd) {
                return false;
            }
        }

		// 校验密码, 如果校验不通过, 则返回错误消息
		VerifyResultDTO verifyResultDTO = passwordConfigService.verifyPassword(storageId, fullPath, password);
		if (!verifyResultDTO.isPassed()) {
			throw new BizException(verifyResultDTO.getErrorCode());
		}

		// 设置当前文件夹所对应的文件夹路径表达式.
		fileContext.setPasswordPattern(verifyResultDTO.getPattern());;
		return false;
	}

}