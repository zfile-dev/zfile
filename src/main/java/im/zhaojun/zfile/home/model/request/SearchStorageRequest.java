package im.zhaojun.zfile.home.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 搜索存储源中文件请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "搜索存储源中文件请求类")
public class SearchStorageRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "搜索 key", required = true, example = "png")
    @NotBlank(message = "搜索 key 不能为空")
    private String searchVal;

}