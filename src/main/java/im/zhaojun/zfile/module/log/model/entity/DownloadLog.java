package im.zhaojun.zfile.module.log.model.entity;

import cn.hutool.extra.servlet.ServletUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.core.util.RequestHolder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件下载日志 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="文件下载日志")
@TableName(value = "`download_log`")
@NoArgsConstructor
public class DownloadLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`path`")
    @ApiModelProperty(value="文件路径")
    private String path;


    @TableField(value = "`storage_key`")
    @ApiModelProperty(value="存储源 key")
    private String storageKey;


    @TableField(value = "`create_time`")
    @ApiModelProperty(value="访问时间")
    private Date createTime;


    @TableField(value = "`ip`")
    @ApiModelProperty(value="访问 ip")
    private String ip;


    @TableField(value = "short_key")
    @ApiModelProperty(value = "短链 key", example = "voldd3")
    private String shortKey;


    @TableField(value = "`user_agent`")
    @ApiModelProperty(value="访问 user_agent")
    private String userAgent;


    @TableField(value = "`referer`")
    @ApiModelProperty(value="访问 referer")
    private String referer;

    public DownloadLog(String path, String storageKey, String shortKey) {
        this.path = path;
        this.storageKey = storageKey;
        this.shortKey = shortKey;
        this.createTime = new Date();
        HttpServletRequest request = RequestHolder.getRequest();
        this.ip = ServletUtil.getClientIP(request);
        this.referer = request.getHeader(HttpHeaders.REFERER);
        this.userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    }

}