package im.zhaojun.zfile.module.storage.service.base;

import im.zhaojun.zfile.module.storage.model.param.SharePointParam;

/**
 * @author zhaojun
 */
public abstract class AbstractSharePointServiceBase<P extends SharePointParam> extends AbstractMicrosoftDriveService<SharePointParam> {

    @Override
    public String getType() {
        return "sites/" + param.getSiteId();
    }

}