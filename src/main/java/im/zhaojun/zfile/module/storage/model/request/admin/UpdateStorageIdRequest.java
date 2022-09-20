package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新存储源参数请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "更新存储源 id 请求类")
public class UpdateStorageIdRequest {

    @ApiModelProperty(value = "存储源原 ID", required = true, example = "1")
    @NotBlank(message = "源 id 不能为空")
    private Integer updateId;


    @ApiModelProperty(value = "存储源新 ID", required = true, example = "2")
    @NotBlank(message = "修改后的 id 不能为空")
    private Integer newId;

}