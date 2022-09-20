package im.zhaojun.zfile.module.storage.model.request.operator;

import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 删除文件夹请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "删除文件夹请求类")
public class BatchDeleteRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;
    
    @ApiModelProperty(value = "删除的文件详情")
    @NotEmpty(message = "要删除的文件/文件夹不能为空")
    private List<DeleteItem> deleteItems;
    
    @Data
    public static class DeleteItem {
        
        private String name;
        
        private String path;
        
        private FileTypeEnum type;
        
        private String password;
        
    }

}