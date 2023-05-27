package im.zhaojun.zfile.module.storage.model.request.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 复制存储源请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "复制存储源请求请求类")
public class CopyStorageSourceRequest {

    @ApiModelProperty(value = "存储源 ID", required = true, example = "1")
    @NotNull(message = "存储源 id 不能为空")
    private Integer fromId;

    @ApiModelProperty(value = "复制后存储源名称", required = true, example = "1")
    @NotBlank(message = "复制后存储源名称不能为空")
    private String toName;

    @ApiModelProperty(value = "复制后存储源别名", required = true, example = "1")
    @NotBlank(message = "复制后存储源别名不能为空")
    private String toKey;



}
