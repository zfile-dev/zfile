package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 复制存储源请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "复制存储源请求请求类")
public class CopyStorageSourceRequest {

    @Schema(title = "存储源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "存储源 id 不能为空")
    private Integer fromId;

    @Schema(title = "复制后存储源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "复制后存储源名称不能为空")
    private String toName;

    @Schema(title = "复制后存储源别名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "复制后存储源别名不能为空")
    private String toKey;



}
