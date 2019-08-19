package im.zhaojun.common.mapper;

import im.zhaojun.common.model.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemConfigMapper {

    int insert(SystemConfig record);

    int updateByPrimaryKeySelective(SystemConfig record);

    int updateByPrimaryKey(SystemConfig record);

    SystemConfig selectFirstConfig();
}