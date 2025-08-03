package im.zhaojun.zfile.module.storage.model.request.operator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量(移动/复制)(文件/文件夹)请求，不支持跨存储策略操作，也不支持批量时源路径不相同或目标路径不相同的操作。
 *
 *
 * @author zhaojun
 */
@Data
@Schema(description = "(移动/复制)(文件/文件夹)请求")
public class BatchMoveOrCopyFileRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "请求路径", example = "/", description ="表示要移动的文件夹所在的文件夹")
    @NotBlank
    private String path;

    @Schema(title = "文件夹名称", example = "movie", description ="表示要移动的文件夹名称，支持多个")
    @NotEmpty
    private List<String> nameList;

    @Schema(title = "目标路径", example = "/", description ="表示要移动到的文件夹")
    @NotBlank
    private String targetPath;

    @Schema(title = "目标文件夹名称", example = "电影", description ="表示要移动到的文件夹名称，支持多个")
    @NotEmpty
    private List<String> targetNameList;

    @Schema(title = "源文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String srcPathPassword;

    @Schema(title = "目标文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
    private String targetPathPassword;

}
