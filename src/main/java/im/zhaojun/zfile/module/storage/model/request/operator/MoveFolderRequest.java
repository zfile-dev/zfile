package im.zhaojun.zfile.module.storage.model.request.operator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 移动文件夹请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "移动文件夹请求")
public class MoveFolderRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "请求路径", example = "/", notes = "表示要移动的文件夹所在的文件夹")
    private String path;

    @ApiModelProperty(value = "文件夹名称", example = "movie", notes = "表示要移动的文件夹名称")
    private String name;

    @ApiModelProperty(value = "目标路径", example = "/", notes = "表示要移动到的文件夹")
    private String targetPath;

    @ApiModelProperty(value = "目标文件夹名称", example = "电影", notes = "表示要移动到的文件夹名称")
    private String targetName;

    @ApiModelProperty(value = "源文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String srcPassword;

    @ApiModelProperty(value = "目标文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String targetPassword;

}
