package im.zhaojun.zfile.service.impl;

import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
public class UFileServiceImpl extends UpYunServiceImpl {

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.UFILE;
    }

}
