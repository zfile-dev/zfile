package im.zhaojun.zfile.module.storage.model.request.base;

import im.zhaojun.zfile.core.util.PatternMatcherUtils;
import im.zhaojun.zfile.module.storage.model.enums.SearchFolderModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 搜索存储源中文件请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "搜索存储源中文件请求类")
public class SearchStorageRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "搜索关键字", requiredMode = Schema.RequiredMode.REQUIRED, example = "png")
    private String searchKeyword;

    @Schema(title = "搜索模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "search_all")
    private SearchFolderModeEnum searchMode;

    @Schema(title = "搜索路径", example = "/")
    private String path;

    @Schema(title = "密码缓存")
    private Map<String, String> passwordCache;


    /**
     * 根据路径获取缓存的密码
     *
     * @param   path
     *          文件夹路径
     *
     * @return  密码, 没找到则返回 null.
     */
    public String getPathPasswordCache(String path) {
        if (passwordCache == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : passwordCache.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
    
            // 判断当前请求路径是否和规则路径表达式匹配
            boolean match = PatternMatcherUtils.testCompatibilityGlobPattern(key, path);
            if (match) {
                return value;
            }
        }

        return null;
    }


}