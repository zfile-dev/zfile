package im.zhaojun.zfile.module.storage.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件信息结果类
 * 
 * @author zhaojun
 */
@Data
@ApiModel(value="文件列表信息结果类")
public class FileItemResult implements Serializable {

    @ApiModelProperty(value = "文件名", example = "a.mp4")
    private String name;
    
    @ApiModelProperty(value = "时间", example = "2020-01-01 15:22")
    private Date time;
    
    @ApiModelProperty(value = "大小", example = "1024")
    private Long size;
    
    @ApiModelProperty(value = "类型", example = "file")
    private FileTypeEnum type;
    
    @ApiModelProperty(value = "所在路径", example = "/home/")
    private String path;
    
    @ApiModelProperty(value = "下载地址", example = "http://www.example.com/a.mp4")
    private String url;
    
    /**
     * 获取路径和名称的组合, 并移除重复的路径分隔符 /.
     *
     * @return  路径和名称的组合
     */
    @JsonIgnore
    public String getFullPath() {
        return StringUtils.concat(path, name);
    }

}