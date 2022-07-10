package im.zhaojun.zfile.home.model.request.operator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 上传文件请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "上传文件请求类")
public class UploadFileRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "上传路径", example = "/movie", notes = "表示上传文件到哪个路径")
    @NotBlank(message = "上传路径不能为空")
    private String path;

    @ApiModelProperty(value = "上传的文件名", example = "test.mp4")
    @NotBlank(message = "上传的文件名不能为空")
    private String name;

    @ApiModelProperty(value = "文件大小", example = "129102")
    private Long size;

}