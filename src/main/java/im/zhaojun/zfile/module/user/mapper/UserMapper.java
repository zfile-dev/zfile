package im.zhaojun.zfile.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    Integer findIdByUsername(@Param("username") String username);

    int countByUsername(@Param("username") String username, @Param("ignoreId") Integer ignoreId);

    int updateUserNameAndPwdById(@Param("id") Integer id, @Param("username") String username, @Param("password") String password);

}