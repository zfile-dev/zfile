package im.zhaojun.ufile.service;

import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.upyun.service.UpYunServiceImpl;
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
