package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新存储源排序值请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "更新存储源排序值请求类")
public class UpdateStorageSortRequest {

    @ApiModelProperty(value = "存储源 ID", required = true, example = "1")
    @NotBlank(message = "存储源 id 不能为空")
    private Integer id;


    @ApiModelProperty(value = "排序值，值越小越靠前", required = true, example = "5")
    @NotBlank(message = "排序值不能为空")
    private Integer orderNum;

}