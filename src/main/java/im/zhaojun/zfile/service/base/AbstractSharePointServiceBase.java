package im.zhaojun.zfile.service.base;

import im.zhaojun.zfile.model.entity.StorageConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSharePointServiceBase extends MicrosoftDriveServiceBase {

    protected String siteId;

    @Override
    public String getType() {
        return "sites/" + siteId;
    }

    @Override
    public String getDownloadUrl(String path) {
        return null;
    }

    @Override
    public List<StorageConfig> storageStrategyConfigList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("accessToken", "访问令牌"));
            add(new StorageConfig("refreshToken", "刷新令牌"));
            add(new StorageConfig("basePath", "基路径"));
            add(new StorageConfig("siteName", "站点名称"));
            add(new StorageConfig("siteId", "SiteId"));
            add(new StorageConfig("siteType", "siteType"));
        }};
    }
}
