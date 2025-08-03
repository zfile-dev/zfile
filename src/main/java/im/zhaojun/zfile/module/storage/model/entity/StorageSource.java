package im.zhaojun.zfile.module.storage.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.storage.model.enums.SearchModeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 存储源基本属性 entity
 *
 * @author zhaojun
 */
@Data
@Schema(description = "存储源基本属性")
@TableName(value = "storage_source")
public class StorageSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`enable`")
    @Schema(title = "是否启用", example = "true")
    private Boolean enable;


    @TableField(value = "`enable_file_operator`")
    @Schema(title = "是否启用文件操作功能", example = "true", description ="是否启用文件上传，编辑，删除等操作.")
    @Deprecated
    private Boolean enableFileOperator;


    @TableField(value = "`enable_file_anno_operator`")
    @Schema(title = "是否允许匿名进行文件操作", example = "true", description ="是否允许匿名进行文件上传，编辑，删除等操作.")
    @Deprecated
    private Boolean enableFileAnnoOperator;


    @TableField(value = "`enable_cache`")
    @Schema(title = "是否开启缓存", example = "true")
    private Boolean enableCache;


    @TableField(value = "`name`")
    @Schema(title = "存储源名称", example = "阿里云 OSS 存储")
    private String name;


    @TableField(value = "`key`")
    @Schema(title = "存储源别名", example = "存储源别名，用于 URL 中展示, 如 http://ip:port/{存储源别名}")
    private String key;


    @TableField(value = "`remark`")
    @Schema(title = "存储源备注", example = "这是一个备注信息, 用于管理员区分不同的存储源, 此字段仅管理员可见")
    private String remark;


    @TableField(value = "auto_refresh_cache")
    @Schema(title = "是否开启缓存自动刷新", example = "true")
    private Boolean autoRefreshCache;


    @TableField(value = "`type`")
    @Schema(title = "存储源类型")
    private StorageTypeEnum type;


    @TableField(value = "search_enable")
    @Schema(title = "是否开启搜索", example = "true")
    private Boolean searchEnable;


    @TableField(value = "search_ignore_case")
    @Schema(title = "搜索是否忽略大小写", example = "true")
    private Boolean searchIgnoreCase;


    @TableField(value = "`search_mode`")
    @Schema(title = "搜索模式", example = "SEARCH_CACHE", description ="仅从缓存中搜索或直接全量搜索")
    private SearchModeEnum searchMode;


    @TableField(value = "order_num")
    @Schema(title = "排序值", example = "1")
    private Integer orderNum;


    @TableField(value = "default_switch_to_img_mode")
    @Schema(title = "是否默认开启图片模式", example = "true")
    private Boolean defaultSwitchToImgMode;
    
    
    @TableField(value = "compatibility_readme")
    @Schema(title = "兼容 readme 模式", example = "true", description ="兼容模式, 目录文档读取 readme.md 文件")
    private Boolean compatibilityReadme;

}