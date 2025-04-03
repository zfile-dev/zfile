package im.zhaojun.zfile.module.permission.convert;

import im.zhaojun.zfile.module.permission.model.entity.PermissionConfig;
import im.zhaojun.zfile.module.permission.model.result.PermissionConfigResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限配置实体类转换器
 *
 * @author zhaojun
 */
@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionConfigConvert {

	List<PermissionConfigResult> toResult(List<PermissionConfig> permissionConfig);

}