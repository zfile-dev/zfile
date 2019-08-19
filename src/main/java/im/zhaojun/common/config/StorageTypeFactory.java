package im.zhaojun.common.config;

import im.zhaojun.aliyun.service.AliyunService;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import im.zhaojun.ftp.service.FtpService;
import im.zhaojun.huawei.service.HuaweiService;
import im.zhaojun.local.service.LocalService;
import im.zhaojun.qiniu.service.QiniuService;
import im.zhaojun.tencent.TencentService;
import im.zhaojun.upyun.service.UpYunService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 存储类型工厂类
 */
@Component
public class StorageTypeFactory implements ApplicationContextAware {

    private static Map<String, FileService> storageTypeEnumFileServiceMap;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext act) throws BeansException {
        applicationContext = act;
        storageTypeEnumFileServiceMap = act.getBeansOfType(FileService.class);
    }

    public static <T extends FileService> T getTrafficMode(StorageTypeEnum type) {
        String beanName = "";
        switch (type) {
            case UPYUN: beanName = applicationContext.getBeanNamesForType(UpYunService.class)[0]; break;
            case QINIU: beanName = applicationContext.getBeanNamesForType(QiniuService.class)[0]; break;
            case HUAWEI: beanName = applicationContext.getBeanNamesForType(HuaweiService.class)[0]; break;
            case FTP: beanName = applicationContext.getBeanNamesForType(FtpService.class)[0]; break;
            case ALIYUN: beanName = applicationContext.getBeanNamesForType(AliyunService.class)[0]; break;
            case LOCAL: beanName = applicationContext.getBeanNamesForType(LocalService.class)[0]; break;
            case TENCENT: beanName = applicationContext.getBeanNamesForType(TencentService.class)[0]; break;
        }
        return (T) storageTypeEnumFileServiceMap.get(beanName);
    }

}