package im.zhaojun.zfile.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.user.model.dto.UserStorageSourceDetailDTO;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserStorageSourceMapper extends BaseMapper<UserStorageSource> {

    int deleteByUserId(@Param("userId") Integer userId);

    int deleteByStorageId(@Param("storageId") Integer storageId);

    List<UserStorageSourceDetailDTO> getDTOListByUserId(@Param("userId") Integer userId);

    UserStorageSource getByUserIdAndStorageId(@Param("userId") Integer userId, @Param("storageId") Integer storageId);

    List<UserStorageSource> selectByStorageId(@Param("storageId") Integer storageId);

    List<UserStorageSource> selectByUserId(@Param("userId") Integer userId);

}