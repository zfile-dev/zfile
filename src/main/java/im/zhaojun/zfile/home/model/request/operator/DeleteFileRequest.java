package im.zhaojun.zfile.home.model.request.operator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 删除文件请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "删除文件请求类")
public class DeleteFileRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "请求路径", example = "/", notes = "表示在哪个文件夹下删除文件")
    @NotBlank(message = "请求路径不能为空")
    private String path;

    @ApiModelProperty(value = "删除的文件夹名称", example = "movie")
    @NotBlank(message = "删除的文件名称不能为空")
    private String name;

}