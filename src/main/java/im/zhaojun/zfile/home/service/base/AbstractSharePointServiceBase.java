package im.zhaojun.zfile.home.service.base;

import im.zhaojun.zfile.admin.model.param.SharePointParam;

public abstract class AbstractSharePointServiceBase<P extends SharePointParam> extends MicrosoftDriveServiceBase<SharePointParam> {

    @Override
    public void init() {
        refreshAccessToken();
    }

    @Override
    public String getType() {
        return "sites/" + param.getSiteId();
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        return getFileItem(pathAndName).getUrl();
    }

}