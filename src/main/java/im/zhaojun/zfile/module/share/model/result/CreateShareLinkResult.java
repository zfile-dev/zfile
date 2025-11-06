package im.zhaojun.zfile.module.share.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建分享链接响应
 *
 * @author zhaojun
 */
@Data
@Schema(description = "创建分享链接响应")
public class CreateShareLinkResult {

    @Schema(title = "分享链接 key", example = "abc12345")
    private String shareKey;

    @Schema(title = "完整分享链接", example = "https://example.com/s/abc12345")
    private String fullShareUrl;
}