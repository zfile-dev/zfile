package im.zhaojun.zfile.module.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 单点登录配置表的 Mapper 接口
 *
 * @author OnEvent
 */
@Mapper
public interface SsoConfigMapper extends BaseMapper<SsoConfig>
{
    SsoConfig findByProvider(@Param("provider") String provider);
}
