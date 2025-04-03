package im.zhaojun.zfile.module.storage.model.dto;

import cn.hutool.core.collection.CollUtil;
import im.zhaojun.zfile.core.util.StringUtils;
import lombok.Data;
import software.amazon.awssdk.services.s3.model.CORSRule;

import java.io.Serializable;
import java.util.*;

@Data
public class ZFileCORSRule implements Serializable {

    private String id;

    private List<String> allowedMethods;

    private List<String> allowedOrigins;

    private Integer maxAgeSeconds;

    private List<String> exposedHeaders;

    private List<String> allowedHeaders;

    public static List<ZFileCORSRule> fromCORSRule(List<CORSRule> corsRules) {
        List<ZFileCORSRule> zFileCORSRules = new ArrayList<>();
        for (CORSRule corsRule : corsRules) {
            ZFileCORSRule zFileCORSRule = new ZFileCORSRule();
            zFileCORSRule.setId(StringUtils.isEmpty(corsRule.id()) ? String.valueOf(corsRule.hashCode()) : corsRule.id());
            zFileCORSRule.setAllowedMethods(new ArrayList<>(corsRule.allowedMethods()));
            zFileCORSRule.setAllowedOrigins(new ArrayList<>(corsRule.allowedOrigins()));
            zFileCORSRule.setAllowedHeaders(new ArrayList<>(corsRule.allowedHeaders()));
            zFileCORSRule.setExposedHeaders(new ArrayList<>(corsRule.exposeHeaders()));
            zFileCORSRule.setMaxAgeSeconds(corsRule.maxAgeSeconds());
            zFileCORSRules.add(zFileCORSRule);
        }
        return sortAndDistinct(zFileCORSRules);
    }

    public static Set<CORSRule> toCORSRule(List<ZFileCORSRule> zFileCORSRules) {
        Set<CORSRule> corsRules = new HashSet<>();
        for (ZFileCORSRule zFileCORSRule : sortAndDistinct(zFileCORSRules)) {
            CORSRule.Builder builder = CORSRule.builder();
            builder.id(zFileCORSRule.getId());
            builder.allowedMethods(zFileCORSRule.getAllowedMethods());
            builder.allowedOrigins(zFileCORSRule.getAllowedOrigins());
            builder.allowedHeaders(zFileCORSRule.getAllowedHeaders());
            builder.exposeHeaders(zFileCORSRule.getExposedHeaders());
            builder.maxAgeSeconds(zFileCORSRule.getMaxAgeSeconds());
            corsRules.add(builder.build());
        }
        return corsRules;
    }

    public static List<ZFileCORSRule> sortAndDistinct(List<ZFileCORSRule> zFileCORSRules) {
        for (ZFileCORSRule zFileCORSRule : zFileCORSRules) {
            if (zFileCORSRule.getAllowedMethods() != null) {
                Collections.sort(zFileCORSRule.getAllowedMethods());
            }
            if (zFileCORSRule.getAllowedHeaders() != null) {
                Collections.sort(zFileCORSRule.getAllowedHeaders());
            }
            if (zFileCORSRule.getAllowedOrigins() != null) {
                Collections.sort(zFileCORSRule.getAllowedOrigins());
            }
            if (zFileCORSRule.getExposedHeaders() != null) {
                Collections.sort(zFileCORSRule.getExposedHeaders());
            }
        }
        return CollUtil.distinct(zFileCORSRules);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZFileCORSRule that = (ZFileCORSRule) o;
        return Objects.equals(allowedMethods, that.allowedMethods) &&
                Objects.equals(allowedOrigins, that.allowedOrigins) &&
                Objects.equals(maxAgeSeconds, that.maxAgeSeconds) &&
                Objects.equals(exposedHeaders, that.exposedHeaders) &&
                Objects.equals(allowedHeaders, that.allowedHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedMethods, allowedOrigins, maxAgeSeconds, exposedHeaders, allowedHeaders);
    }

}