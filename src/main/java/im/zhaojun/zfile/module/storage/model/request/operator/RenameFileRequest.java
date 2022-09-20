package im.zhaojun.zfile.module.storage.model.request.operator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 重命名文件请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "重命名文件请求类")
public class RenameFileRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "请求路径", example = "/", notes = "表示在哪个文件夹下重命名文件")
    private String path = "/";

    @ApiModelProperty(value = "重命名的原文件名称", example = "test.txt")
    @NotBlank(message = "原文件名不能为空")
    private String name;

    @ApiModelProperty(value = "重命名后的文件名称", example = "text-1.txt")
    @NotBlank(message = "新文件名不能为空")
    private String newName;
    
    @ApiModelProperty(value = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String password;

}