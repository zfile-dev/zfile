package im.zhaojun.zfile.module.link.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 短链接搜索请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "搜索存储源中文件请求类")
public class ShortLinkSearchRequest {

    @ApiModelProperty(value = "存储源 id", required = true, example = "1")
    @NotBlank(message = "存储源 id 不能为空")
    private Integer storageId;

    @ApiModelProperty(value = "存储源 key", example = "local")
    private String key;

    @ApiModelProperty(value = "文件 url/路径", example = "/a")
    private String url;

    @ApiModelProperty(value = "开始时间", example = "2022-01-01 00:00:00")
    private String dateFrom;

    @ApiModelProperty(value = "结束时间", example = "2022-12-31 23:59:59")
    private String dateTo;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer limit;

    @ApiModelProperty(value = "排序字段", example = "id")
    private String orderBy;

    @ApiModelProperty(value = "排序方式", example = "desc")
    private String orderDirection;

}