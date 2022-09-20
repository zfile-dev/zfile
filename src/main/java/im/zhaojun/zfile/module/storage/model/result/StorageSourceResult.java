package im.zhaojun.zfile.module.storage.model.result;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 存储源基本信息结果类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "存储源基本信息响应类")
public class StorageSourceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "存储源名称", example = "阿里云 OSS 存储")
    private String name;

    @ApiModelProperty(value = "存储源别名", example = "存储源别名，用于 URL 中展示, 如 http://ip:port/{存储源别名}")
    private String key;

    @ApiModelProperty(value = "存储源类型")
    private StorageTypeEnum type;

    @ApiModelProperty(value = "是否开启搜索", example = "true")
    private Boolean searchEnable;

    @ApiModelProperty(value = "是否默认开启图片模式", example = "true")
    private Boolean defaultSwitchToImgMode;

}