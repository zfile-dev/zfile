package im.zhaojun.zfile.module.user.service;

import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.storage.event.StorageSourceCopyEvent;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.user.event.UserCopyEvent;
import im.zhaojun.zfile.module.user.manager.UserManager;
import im.zhaojun.zfile.module.user.mapper.UserStorageSourceMapper;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static im.zhaojun.zfile.module.user.service.UserStorageSourceService.USER_STORAGE_SOURCE_CACHE_KEY;

@Slf4j
@Service
@CacheConfig(cacheNames = USER_STORAGE_SOURCE_CACHE_KEY)
public class UserStorageSourceService {

    public static final String USER_STORAGE_SOURCE_CACHE_KEY = "userStorageSource";

    @Resource
    private UserManager userManager;

    @Resource
    private UserStorageSourceMapper userStorageSourceMapper;


    /**
     * 根据用户 ID 和存储策略 ID 查询存储策略权限
     *
     * @param   userId
     *          用户 ID
     *
     * @param   storageId
     *          存储策略 ID
     *
     * @return  存储策略权限
     */
    @Cacheable(key = "#userId + '-' + #storageId",
            unless = "#result == null",
            condition = "#userId != null && #storageId != null")
    public UserStorageSource getByUserIdAndStorageId(Integer userId, Integer storageId) {
        return userStorageSourceMapper.getByUserIdAndStorageId(userId, storageId);
    }

    /**
     * 判断当前登录用户在指定存储策略是否有指定操作的权限
     *
     * @param   storageId
     *          存储策略 ID
     *
     * @param   operatorTypeEnum
     *          操作类型
     *
     * @return  当前登录用户在指定存储策略是否有指定操作的权限
     */
    public boolean hasCurrentUserStorageOperatorPermission(Integer storageId, FileOperatorTypeEnum operatorTypeEnum) {
        UserStorageSource userStorageSource = ((UserStorageSourceService) AopContext.currentProxy()).getByUserIdAndStorageId(ZFileAuthUtil.getCurrentUserId(), storageId);
        return userStorageSource.getPermissions().contains(operatorTypeEnum.getValue());
    }

    /**
     * 判断指定用户在指定存储策略是否有指定操作的权限（分享模式下按分享者判断）
     */
    public boolean hasUserStorageOperatorPermission(Integer userId, Integer storageId, FileOperatorTypeEnum operatorTypeEnum) {
        if (userId == null) {
            return hasCurrentUserStorageOperatorPermission(storageId, operatorTypeEnum);
        }
        UserStorageSource userStorageSource = ((UserStorageSourceService) AopContext.currentProxy()).getByUserIdAndStorageId(userId, storageId);
        if (userStorageSource == null || userStorageSource.getPermissions() == null) {
            return false;
        }
        return userStorageSource.getPermissions().contains(operatorTypeEnum.getValue());
    }


    /**
     * 获取当前登录用户在指定存储策略的权限支持情况，数据结构为 Map，Key 为权限名称，Value 为布尔值表示是否支持
     *
     * @param   storageId
     *          存储策略 ID
     *
     * @return  当前登录用户在指定存储策略的权限支持情况
     */
    public HashMap<String, Boolean> getCurrentUserPermissionMapByStorageId(Integer storageId) {
        Integer currentUserId = ZFileAuthUtil.getCurrentUserId();
        UserStorageSource userStorageSource = ((UserStorageSourceService) AopContext.currentProxy()).getByUserIdAndStorageId(currentUserId, storageId);
        return buildPermissionMap(userStorageSource);
    }


    /**
     * 获取指定用户在指定存储源下的权限映射表
     */
    public HashMap<String, Boolean> getPermissionMapByUserIdAndStorageId(Integer userId, Integer storageId) {
        if (userId == null || storageId == null) {
            return buildPermissionMap(null);
        }
        UserStorageSource userStorageSource = ((UserStorageSourceService) AopContext.currentProxy()).getByUserIdAndStorageId(userId, storageId);
        return buildPermissionMap(userStorageSource);
    }


    private HashMap<String, Boolean> buildPermissionMap(UserStorageSource userStorageSource) {
        HashMap<String, Boolean> map = new HashMap<>();
        Set<String> permissions = userStorageSource != null ? userStorageSource.getPermissions() : null;
        for (FileOperatorTypeEnum operatorTypeEnum : FileOperatorTypeEnum.values()) {
            map.put(operatorTypeEnum.getValue(), permissions != null && permissions.contains(operatorTypeEnum.getValue()));
        }
        return map;
    }


    /**
     * 新增存储源时，自动按照各个用户的默认权限配置，为该存储源添加权限
     *
     * @param   storageId
     *          存储源 ID
     */
    public void addDefaultPermissionsForAllUsersInStorageSource(Integer storageId) {
        userManager.addDefaultPermissionsForAllUsersInStorageSource(storageId);
    }


    /**
     * 删除指定存储策略 ID 的存储策略权限
     *
     * @param   storageId
     *          存储策略 ID
     *
     * @return  删除的条数
     */
    @CacheEvict(allEntries = true)
    public int deleteByStorageId(Integer storageId) {
        int deleteSize = userStorageSourceMapper.deleteByStorageId(storageId);
        log.info("删除存储源 ID 为 {} 的存储源用户权限 {} 条", storageId, deleteSize);
        return deleteSize;
    }

    /**
     * 监听存储源删除事件，根据存储源 id 删除相关的用户权限
     *
     * @param   storageSourceDeleteEvent
     *          存储源删除事件
     */
    @EventListener
    public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
        Integer storageId = storageSourceDeleteEvent.getId();
        int updateRows = ((UserStorageSourceService) AopContext.currentProxy()).deleteByStorageId(storageId);
        log.info("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源用户权限 {} 条",
                storageId,
                storageSourceDeleteEvent.getName(),
                storageSourceDeleteEvent.getType().getDescription(),
                updateRows);
    }


    /**
     * 监听存储源复制事件, 复制存储源时, 复制用户的存储源权限
     *
     * @param   storageSourceCopyEvent
     *          存储源复制事件
     */
    @EventListener
    public void onStorageSourceCopy(StorageSourceCopyEvent storageSourceCopyEvent) {
        Integer fromId = storageSourceCopyEvent.getFromId();
        Integer newId = storageSourceCopyEvent.getNewId();

        List<UserStorageSource> userStorageSourceList = userStorageSourceMapper.selectByStorageId(fromId);

        userStorageSourceList.forEach(userStorageSource -> {
            UserStorageSource newUserStorageSource = new UserStorageSource();
            BeanUtils.copyProperties(userStorageSource, newUserStorageSource);
            newUserStorageSource.setId(null);
            newUserStorageSource.setStorageSourceId(newId);
            userStorageSourceMapper.insert(newUserStorageSource);
        });

        log.info("复制存储源 ID 为 {} 的存储源用户权限设置到存储源 ID 为 {} 成功, 共 {} 条", fromId, newId, userStorageSourceList.size());
    }


    /**
     * 监听用户复制事件, 复制用户时, 复制原用户的存储源权限到新用户
     *
     * @param   userCopyEvent
     *          用户复制事件
     */
    @EventListener
    public void onUserCopy(UserCopyEvent userCopyEvent) {
        Integer fromId = userCopyEvent.getFromId();
        Integer newId = userCopyEvent.getNewId();

        List<UserStorageSource> userStorageSourceList = userStorageSourceMapper.selectByUserId(fromId);

        userStorageSourceList.forEach(userStorageSource -> {
            UserStorageSource newUserStorageSource = new UserStorageSource();
            BeanUtils.copyProperties(userStorageSource, newUserStorageSource);
            newUserStorageSource.setId(null);
            newUserStorageSource.setUserId(newId);
            userStorageSourceMapper.insert(newUserStorageSource);
        });

        log.info("复制 ID 为 {} 的存储源用户权限设置到用户 ID 为 {} 成功, 共 {} 条", fromId, newId, userStorageSourceList.size());
    }

}