package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新存储源参数请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "更新存储源 id 请求类")
public class UpdateStorageIdRequest {

    @Schema(title = "存储源原 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "源 id 不能为空")
    private Integer updateId;


    @Schema(title = "存储源新 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotBlank(message = "修改后的 id 不能为空")
    private Integer newId;

}