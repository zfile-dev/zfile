package im.zhaojun.zfile.module.share.model.request;

import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 获取分享文件列表请求
 *
 * @author zhaojun
 */
@Data
@Schema(description = "获取分享文件列表请求")
public class ShareFileListRequest {

    @Schema(title = "分享链接 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc12345")
    @NotBlank(message = "分享链接 key 不能为空")
    private String shareKey;

    @Schema(title = "请求路径", example = "/")
    private String path;

    @Schema(title = "分享密码", example = "123456")
    private String password;

    @Schema(title = "目录密码", example = "123456")
    private String folderPassword;

    @Schema(title = "排序字段", example = "name")
    private String orderBy;

    @Schema(title = "排序方向", example = "asc")
    private String orderDirection;

    public void handleDefaultValue() {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = "name";
        }
        if (StringUtils.isEmpty(orderDirection)) {
            orderDirection = "asc";
        }

        // 自动补全路径, 如 a 补全为 /a/
        path = StringUtils.concat(path);
    }
}
