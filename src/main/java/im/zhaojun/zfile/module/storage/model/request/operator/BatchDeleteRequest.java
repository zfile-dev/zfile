package im.zhaojun.zfile.module.storage.model.request.operator;

import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 删除文件夹请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "删除文件夹请求类")
public class BatchDeleteRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;
    
    @Schema(title = "删除的文件详情")
    @NotEmpty(message = "要删除的文件/文件夹不能为空")
    private List<DeleteItem> deleteItems;
    
    @Data
    public static class DeleteItem {

        private String path;

        private String name;

        private FileTypeEnum type;
        
        private String password;
        
    }

}