package im.zhaojun.zfile.module.storage.chain.command;

import im.zhaojun.zfile.module.storage.chain.FileContext;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 检查是否有权限下载 command 命令
 *      检查是否有权限下载和预览文件，如果都没有权限，则去除返回结果中的下载地址
 *
 * @author zhaojun
 */
@Service
public class FileDownloadPermissionCommand implements Command {

    @Resource
    private UserStorageSourceService userStorageSourceService;

    /**
     * 当没有预览和下载权限时，去除返回结果中的 url 字段.
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
        Integer operatorUserId = fileContext.getOperatorUserId();

        boolean hasDownloadPermission = userStorageSourceService.hasUserStorageOperatorPermission(operatorUserId, storageId, FileOperatorTypeEnum.DOWNLOAD);
        boolean hasPreviewPermission = userStorageSourceService.hasUserStorageOperatorPermission(operatorUserId, storageId, FileOperatorTypeEnum.PREVIEW);

        if (hasDownloadPermission || hasPreviewPermission) {
            return false;
        }
        fileContext.getFileItemList().forEach(file -> { file.setUrl(null);});
        return false;
    }

}