package im.zhaojun.zfile.admin.convert;

import im.zhaojun.zfile.admin.model.entity.DownloadLog;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.result.link.DownloadLogResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

/**
 * 下载日志实体类转换器
 *
 * @author zhaojun
 */
@Component
@Mapper(componentModel = "spring")
public interface DownloadLogConvert {

	@Mapping(source = "downloadLog.id", target = "id")
	@Mapping(source = "storageSource.name", target = "storageName")
	@Mapping(source = "storageSource.type", target = "storageType")
	DownloadLogResult entityToResultList(DownloadLog downloadLog, StorageSource storageSource);

}