package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新存储源排序值请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "更新存储源排序值请求类")
public class UpdateStorageSortRequest {

    @Schema(title = "存储源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "存储源 id 不能为空")
    private Integer id;


    @Schema(title = "排序值，值越小越靠前", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotBlank(message = "排序值不能为空")
    private Integer orderNum;

}