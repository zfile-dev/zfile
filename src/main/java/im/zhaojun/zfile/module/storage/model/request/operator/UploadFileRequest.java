package im.zhaojun.zfile.module.storage.model.request.operator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 上传文件请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "上传文件请求类")
public class UploadFileRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "上传路径", example = "/movie", description ="表示上传文件到哪个路径")
    private String path = "/";

    @Schema(title = "上传的文件名", example = "test.mp4")
    @NotBlank(message = "上传的文件名不能为空")
    private String name;

    @Schema(title = "文件大小", example = "129102")
    private Long size;
    
    @Schema(title = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String password;

}