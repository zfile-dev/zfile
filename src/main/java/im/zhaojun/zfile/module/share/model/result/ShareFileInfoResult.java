package im.zhaojun.zfile.module.share.model.result;

import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分享文件信息响应
 *
 * @author zhaojun
 */
@Data
@Schema(description = "分享文件信息响应")
public class ShareFileInfoResult {

    @Schema(title = "文件列表")
    private List<FileItemResult> fileItemList;

    @Schema(title = "当前路径")
    private String currentPath;

    @Schema(title = "父路径")
    private String parentPath;

    @Schema(title = "分享信息")
    private ShareLinkResult shareLinkInfo;

    @Schema(title = "是否为根目录")
    private Boolean isRoot;

    @Schema(title = "分享者在该存储源的权限映射")
    private Map<String, Boolean> permission;

    public ShareFileInfoResult() {
    }

    public ShareFileInfoResult(List<FileItemResult> fileItemList, String currentPath, ShareLinkResult shareLinkInfo) {
        this.fileItemList = fileItemList;
        this.currentPath = currentPath;
        this.shareLinkInfo = shareLinkInfo;
        this.isRoot = "/".equals(currentPath);
        if (!isRoot && currentPath != null) {
            int lastSlashIndex = currentPath.lastIndexOf('/');
            if (lastSlashIndex > 0) {
                this.parentPath = currentPath.substring(0, lastSlashIndex);
            } else {
                this.parentPath = "/";
            }
        }
    }
}
