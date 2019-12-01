package im.zhaojun.common.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnumSerializerConvert;

public class SystemConfigDTO {

    @JsonIgnore
    private Integer id;

    private String siteName;

    private Boolean infoEnable;

    private Boolean searchEnable;

    private Boolean searchIgnoreCase;

    @JsonSerialize(using = StorageTypeEnumSerializerConvert.class)
    private StorageTypeEnum storageStrategy;

    private String username;

    @JsonIgnore
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Boolean getInfoEnable() {
        return infoEnable;
    }

    public void setInfoEnable(Boolean infoEnable) {
        this.infoEnable = infoEnable;
    }

    public Boolean getSearchEnable() {
        return searchEnable;
    }

    public void setSearchEnable(Boolean searchEnable) {
        this.searchEnable = searchEnable;
    }

    public Boolean getSearchIgnoreCase() {
        return searchIgnoreCase;
    }

    public void setSearchIgnoreCase(Boolean searchIgnoreCase) {
        this.searchIgnoreCase = searchIgnoreCase;
    }

    public StorageTypeEnum getStorageStrategy() {
        return storageStrategy;
    }

    public void setStorageStrategy(StorageTypeEnum storageStrategy) {
        this.storageStrategy = storageStrategy;
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
}