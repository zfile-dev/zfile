package im.zhaojun.zfile.module.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import im.zhaojun.zfile.module.sso.model.response.SsoLoginItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 单点登录配置表的 Mapper 接口
 *
 * @author OnEvent
 */
@Mapper
public interface SsoConfigMapper extends BaseMapper<SsoConfig> {

    List<SsoConfig> findAll();

    List<SsoLoginItemResponse> findAllLoginItems();

    SsoConfig findByProvider(@Param("provider") String provider);

    int countByProvider(@Param("provider") String provider, @Param("ignoreId") Integer ignoreId);

}