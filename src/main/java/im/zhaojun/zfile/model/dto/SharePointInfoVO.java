package im.zhaojun.zfile.model.dto;

import lombok.Data;

@Data
public class SharePointInfoVO {

    private String type;

    private String accessToken;

    private String domainPrefix;

    private String siteType;

    private String siteName;

    private String domainType;

}