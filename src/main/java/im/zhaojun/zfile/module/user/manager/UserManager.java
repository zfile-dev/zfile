package im.zhaojun.zfile.module.user.manager;

import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.user.mapper.UserMapper;
import im.zhaojun.zfile.module.user.mapper.UserStorageSourceMapper;
import im.zhaojun.zfile.module.user.model.dto.UserStorageSourceDetailDTO;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.model.response.UserDetailResponse;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class UserManager {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserStorageSourceMapper userStorageSourceMapper;


    /**
     * 保存用户信息及用户关联的存储策略权限
     *
     * @param   user
     *          用户
     *
     * @param   userStorageSourceList
     *          用户存储策略权限列表
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = UserStorageSourceService.USER_STORAGE_SOURCE_CACHE_KEY, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void saveUserInfo(User user, List<UserStorageSource> userStorageSourceList) {
        // 保存或新增用户
        Integer userId = user.getId();
        if (userId == null) {
            userMapper.insert(user);
            userId = user.getId();
        } else {
            userMapper.updateById(user);
        }

        // 更新用户存储策略权限列表
        userStorageSourceMapper.deleteByUserId(userId);
        for (UserStorageSource userStorageSource : userStorageSourceList) {
            userStorageSource.setUserId(userId);
            userStorageSourceMapper.insert(userStorageSource);
        }
    }


    /**
     * 删除用户, 同时删除用户存储策略权限
     *
     * @param   userId
     *          用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllByUserId(Integer userId) {
        int deleteUserCount = userMapper.deleteById(userId);
        log.info("删除用户, userId: {}, deleteCount: {}", userId, deleteUserCount);

        int deleteUserStorageSourceCount = userStorageSourceMapper.deleteByUserId(userId);
        log.info("删除用户存储策略权限, userId: {}, deleteCount: {}", userId, deleteUserStorageSourceCount);
    }


    /**
     * 根据 user 对象获取包含用户的基本信息和用户与存储策略的关联关系的对象
     *
     * @param   user
     *          用户对象
     *
     * @return  用户详细信息
     */
    public UserDetailResponse assembleUserDetail(User user) {
        Integer userId = user.getId();
        // 获取用户与存储策略的关联关系
        List<UserStorageSourceDetailDTO> userStorageListByUserId = userStorageSourceMapper.getDTOListByUserId(userId);

        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .enable(user.getEnable())
                .createTime(user.getCreateTime())
                .userStorageSourceList(userStorageListByUserId)
                .defaultPermissions(user.getDefaultPermissions())
                .build();
    }


    /**
     * 新增存储源时，自动按照各个用户的默认权限配置，为该存储源添加权限
     *
     * @param   storageId
     *          存储源 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDefaultPermissionsForAllUsersInStorageSource(Integer storageId) {
        log.info("为存储源添加默认权限, storageId: {}", storageId);
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            Set<String> defaultPermissions = user.getDefaultPermissions();
            UserStorageSource userStorageSource = new UserStorageSource();
            userStorageSource.setUserId(user.getId());
            userStorageSource.setStorageSourceId(storageId);
            userStorageSource.setRootPath(StringUtils.SLASH);
            userStorageSource.setPermissions(defaultPermissions);
            userStorageSource.setEnable(CollectionUtils.isNotEmpty(defaultPermissions));
            userStorageSourceMapper.insert(userStorageSource);
            log.info("为用户添加存储源的默认权限: username: {}, storageId: {}, defaultPermissions: {}", user.getUsername(), storageId, defaultPermissions);
        }
    }

}
