package im.zhaojun.zfile.module.share.model.request;

import im.zhaojun.zfile.module.share.model.dto.ShareEntryDTO;
import im.zhaojun.zfile.module.share.model.enums.ShareTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 创建分享链接请求
 *
 * @author zhaojun
 */
@Data
@Schema(description = "创建分享链接请求")
public class CreateShareLinkRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "分享所在目录", requiredMode = Schema.RequiredMode.REQUIRED, example = "/documents")
    @NotBlank(message = "分享目录不能为空")
    private String sharePath;

    @Schema(title = "分享条目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分享条目不能为空")
    private List<ShareEntryDTO> shareEntries;

    @Schema(title = "分享密码（可选）", example = "123456")
    private String password;

    @Schema(title = "过期时间（可选）", example = "2024-12-31T23:59:59")
    private Date expireDate;

    @Schema(title = "分享类型", example = "FOLDER")
    @NotNull(message = "分享类型不能为空")
    private ShareTypeEnum shareType;

    @Schema(title = "自定义分享 key（可选）", description = "如果不提供则自动生成", example = "my-custom-key")
    private String shareKey;
}
