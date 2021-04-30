package im.zhaojun.zfile.service.base;

import im.zhaojun.zfile.model.entity.StorageConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhao Jun
 */
@Slf4j
public abstract class AbstractOneDriveServiceBase extends MicrosoftDriveServiceBase {

    @Override
    public String getType() {
        return "me";
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
            add(new StorageConfig("proxyDomain", "反代域名"));
        }};
    }
}