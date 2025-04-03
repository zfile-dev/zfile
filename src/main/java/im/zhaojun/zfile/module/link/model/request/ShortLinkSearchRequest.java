package im.zhaojun.zfile.module.link.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 短链接搜索请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "搜索存储源中文件请求类")
public class ShortLinkSearchRequest {

    @Schema(name = "存储源 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "存储源 id 不能为空")
    private Integer storageId;

    @Schema(name = "存储源 key", example = "local")
    private String key;

    @Schema(name = "文件 url/路径", example = "/a")
    private String url;

    @Schema(name = "开始时间", example = "2022-01-01 00:00:00")
    private String dateFrom;

    @Schema(name = "结束时间", example = "2022-12-31 23:59:59")
    private String dateTo;

    @Schema(name = "页码", example = "1")
    private Integer page;

    @Schema(name = "每页数量", example = "10")
    private Integer limit;

    @Schema(name = "排序字段", example = "id")
    private String orderBy;

    @Schema(name = "排序方式", example = "desc")
    private String orderDirection;

}