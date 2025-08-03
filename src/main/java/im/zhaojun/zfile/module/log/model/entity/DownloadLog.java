package im.zhaojun.zfile.module.log.model.entity;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.core.util.RequestHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件下载日志 entity
 *
 * @author zhaojun
 */
@Data
@Tag(name ="文件下载日志")
@TableName(value = "`download_log`")
@NoArgsConstructor
public class DownloadLog implements Serializable {

    public static final String DOWNLOAD_TYPE_DIRECT_LINK = "directLink";

    public static final String DOWNLOAD_TYPE_SHORT_LINK = "shortLink";

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`download_type`")
    @Schema(title="下载类型", example = "directLink", allowableValues = "directLink, shortLink")
    private String downloadType;


    @TableField(value = "`path`")
    @Schema(title="文件路径")
    private String path;


    @TableField(value = "`storage_key`")
    @Schema(title="存储源 key")
    private String storageKey;


    @TableField(value = "`create_time`")
    @Schema(title="访问时间")
    private Date createTime;


    @TableField(value = "`ip`")
    @Schema(title="访问 ip")
    private String ip;


    @TableField(value = "short_key")
    @Schema(title = "短链 key", example = "voldd3")
    private String shortKey;


    @TableField(value = "`user_agent`")
    @Schema(title="访问 user_agent")
    private String userAgent;


    @TableField(value = "`referer`")
    @Schema(title="访问 referer")
    private String referer;

    public DownloadLog(String downloadType, String path, String storageKey, String shortKey) {
        this.downloadType = downloadType;
        this.path = path;
        this.storageKey = storageKey;
        this.shortKey = shortKey;
        this.createTime = new Date();
        HttpServletRequest request = RequestHolder.getRequest();
        this.ip = JakartaServletUtil.getClientIP(request);
        this.referer = request.getHeader(HttpHeaders.REFERER);
        this.userAgent = request.getHeader(HttpHeaders.USER_AGENT);
    }

}