package im.zhaojun.zfile.service.impl;

import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UFileServiceImpl extends UpYunServiceImpl {

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.UFILE;
    }

}