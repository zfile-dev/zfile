package im.zhaojun.common.model;

import java.io.Serializable;

public class SiteConfig implements Serializable {

    private static final long serialVersionUID = 8811196207046121740L;

    private String header;

    private String footer;

    private ViewConfig viewConfig;

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

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public void setViewConfig(ViewConfig viewConfig) {
        this.viewConfig = viewConfig;
    }
}
