package im.zhaojun.common.mapper;

import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.StorageConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StorageConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(StorageConfig record);

    int insertSelective(StorageConfig record);

    StorageConfig selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(StorageConfig record);

    int updateByPrimaryKey(StorageConfig record);

    List<StorageConfig> selectStorageConfigByType(StorageTypeEnum type);
}