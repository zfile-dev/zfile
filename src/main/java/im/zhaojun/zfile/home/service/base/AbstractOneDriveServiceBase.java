package im.zhaojun.zfile.home.service.base;

import im.zhaojun.zfile.admin.model.param.OneDriveParam;
import lombok.extern.slf4j.Slf4j;

/**
 * -50
 * +70
 * -100
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractOneDriveServiceBase<P extends OneDriveParam> extends MicrosoftDriveServiceBase<OneDriveParam> {

    @Override
    public void init() {
        refreshAccessToken();
    }

    @Override
    public String getType() {
        return "me";
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        return getFileItem(pathAndName).getUrl();
    }

}