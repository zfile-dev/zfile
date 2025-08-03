package im.zhaojun.zfile.module.storage.model.request.operator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重命名文件夹请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "重命名文件夹请求类")
public class RenameFolderRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "请求路径", example = "/", description ="表示在哪个文件夹下重命名文件夹")
    private String path = "/";

    @Schema(title = "重命名的原文件夹名称", example = "movie")
    @NotBlank(message = "原文件夹名称不能为空")
    private String name;

    @Schema(title = "重命名后的文件名称", example = "music")
    @NotBlank(message = "新文件夹名称不能为空")
    private String newName;
    
    @Schema(title = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String password;

}