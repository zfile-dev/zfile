package im.zhaojun.zfile.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.log.model.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}