package im.zhaojun.zfile.home.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * 缓存信息 DTO
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@ApiModel(description = "缓存信息类")
public class CacheInfoDTO {

    @ApiModelProperty(value = "缓存的 key 个数", required = true, example = "100")
    private Integer cacheCount;

    @ApiModelProperty(value = "缓存命中数", required = true, example = "70")
    private Long hitCount;

    @ApiModelProperty(value = "缓存未命中数", required = true, example = "30")
    private Long missCount;

    @ApiModelProperty(value = "缓存的路径", required = true)
    private Set<String> cacheKeys;

}