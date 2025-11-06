package im.zhaojun.zfile.module.share.model.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import im.zhaojun.zfile.module.share.model.dto.ShareEntryDTO;
import im.zhaojun.zfile.module.share.model.enums.ShareTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 分享链接详情响应
 *
 * @author zhaojun
 */
@Data
@Schema(description = "分享链接详情响应")
public class ShareLinkResult {

    @Schema(title = "分享链接 key")
    private String shareKey;

    @Schema(title = "是否需要密码")
    private Boolean needPassword;

    @Schema(title = "是否已过期")
    private Boolean expired;

    @Schema(title = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireDate;

    @Schema(title = "存储源 key")
    private String storageKey;

    @Schema(title = "存储源 ID")
    private Integer storageId;

    @Schema(title = "存储源名称")
    private String storageName;

    @Schema(title = "分享所在目录")
    private String sharePath;

    @Schema(title = "分享条目列表")
    private List<ShareEntryDTO> shareEntries;

    @Schema(title = "分享类型")
    private ShareTypeEnum shareType;

    @Schema(title = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @Schema(title = "访问次数")
    private Integer accessCount;

    @Schema(title = "下载次数")
    private Integer downloadCount;

    @Schema(title = "分享密码")
    private String password;

    @Schema(title = "分享创建者用户ID")
    private Integer userId;

    @Schema(title = "分享创建者用户名")
    private String username;

    @Schema(title = "分享创建者昵称")
    private String nickname;
}
