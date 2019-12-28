package im.zhaojun.common.model;

import cn.hutool.core.util.ObjectUtil;
import im.zhaojun.common.util.StringUtils;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhaojun
 * @date 2019/12/26 22:07
 */
@Builder
@Data
public class S3Model {

    private String bucketName;

    private String path;

    private String basePath;

    private String domain;

    private long timeout;

    /**
     * 获取 basePath + path 的全路径地址.
     * @return basePath + path 的全路径地址.
     */
    public String getFullPath() {
        String basePath = ObjectUtil.defaultIfNull(this.basePath, "");
        String path = ObjectUtil.defaultIfNull(this.path, "");
        return StringUtils.removeDuplicateSeparator(basePath + "/" + path);
    }

}