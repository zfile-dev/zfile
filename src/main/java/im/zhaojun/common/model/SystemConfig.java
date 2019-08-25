package im.zhaojun.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.enums.ViewModeEnum;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class SystemConfig {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String siteName;

    private ViewModeEnum mode;

    private Boolean sidebarEnable;

    private Boolean infoEnable;

    private Boolean searchEnable;

    private Boolean searchIgnoreCase;

    @JsonIgnore
    private StorageTypeEnum storageStrategy;

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

    public ViewModeEnum getMode() {
        return mode;
    }

    public void setMode(ViewModeEnum mode) {
        this.mode = mode;
    }

    public Boolean getSidebarEnable() {
        return sidebarEnable;
    }

    public void setSidebarEnable(Boolean sidebarEnable) {
        this.sidebarEnable = sidebarEnable;
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
}