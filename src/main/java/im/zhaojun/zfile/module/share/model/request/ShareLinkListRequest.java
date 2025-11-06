package im.zhaojun.zfile.module.share.model.request;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.model.request.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户分享列表分页请求对象.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户分享列表分页请求")
public class ShareLinkListRequest extends PageQueryRequest {

    /**
     * 关键词，支持分享 key、名称模糊搜索.
     */
    @Schema(title = "搜索关键词")
    private String keyword;

    /**
     * 分享状态，支持 all / active / expired.
     */
    @Schema(title = "分享状态", allowableValues = {"all", "active", "expired"})
    private String status;

    /**
     * 存储源 key，传值时仅查询对应存储源的分享记录.
     */
    @Schema(title = "存储源 key")
    private String storageKey;

    /**
     * 创建时间起始.
     */
    @Schema(title = "创建时间起始", example = "2024-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDateStart;

    /**
     * 创建时间结束.
     */
    @Schema(title = "创建时间结束", example = "2024-01-31 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDateEnd;

    public void handleDefaultValue() {
        if (getPage() == null || getPage() < 1) {
            setPage(1);
        }
        if (getLimit() == null || getLimit() < 1) {
            setLimit(10);
        }
        if (getLimit() > 100) {
            setLimit(100);
        }
        if (StrUtil.isBlank(getOrderBy())) {
            setOrderBy("create_date");
        }
        if (StrUtil.isBlank(getOrderDirection())) {
            setOrderDirection("desc");
        }
        if (StrUtil.isBlank(status)) {
            status = "all";
        }
    }
}
