package im.zhaojun.zfile.module.link.convert;

import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.link.model.request.ShortLinkResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

/**
 * 直链实体类器
 *
 * @author zhaojun
 */
@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShortLinkConvert {

	@Mapping(source = "shortLink.id", target = "id")
	@Mapping(source = "storageSource.name", target = "storageName")
	@Mapping(source = "storageSource.type", target = "storageType")
	ShortLinkResult entityToResultList(ShortLink shortLink, StorageSource storageSource);

}