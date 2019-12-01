package im.zhaojun.common.model.dto;

import im.zhaojun.common.model.SystemConfigDTO;

import java.io.Serializable;

public class SiteConfigDTO implements Serializable {

    private static final long serialVersionUID = 8811196207046121740L;

    private String header;

    private String footer;

    private SystemConfigDTO systemConfigDTO;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public SystemConfigDTO getSystemConfigDTO() {
        return systemConfigDTO;
    }

    public void setSystemConfigDTO(SystemConfigDTO systemConfigDTO) {
        this.systemConfigDTO = systemConfigDTO;
    }

    @Override
    public String toString() {
        return "SiteConfigDTO{" +
                "header='" + header + '\'' +
                ", footer='" + footer + '\'' +
                ", systemConfig=" + systemConfigDTO +
                '}';
    }
}
