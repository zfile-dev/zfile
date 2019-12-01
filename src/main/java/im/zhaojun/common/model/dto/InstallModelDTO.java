package im.zhaojun.common.model.dto;

import im.zhaojun.common.model.enums.StorageTypeEnum;

import java.util.Map;

public class InstallModelDTO {
    private String siteName;
    private StorageTypeEnum storageStrategy;
    private String username;
    private String password;
    private Map<String, String> storageStrategyConfig;

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public StorageTypeEnum getStorageStrategy() {
        return storageStrategy;
    }

    public void setStorageStrategy(StorageTypeEnum storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    public Map<String, String> getStorageStrategyConfig() {
        return storageStrategyConfig;
    }

    public void setStorageStrategyConfig(Map<String, String> storageStrategyConfig) {
        this.storageStrategyConfig = storageStrategyConfig;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "InstallModelDTO{" +
                "siteName='" + siteName + '\'' +
                ", storageStrategy=" + storageStrategy +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", storageStrategyConfig=" + storageStrategyConfig +
                '}';
    }
}
