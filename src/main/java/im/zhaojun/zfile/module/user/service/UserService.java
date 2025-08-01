package im.zhaojun.zfile.module.user.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import im.zhaojun.zfile.core.cache.ZFileCacheManager;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.user.event.UserCopyEvent;
import im.zhaojun.zfile.module.user.event.UserDeleteEvent;
import im.zhaojun.zfile.module.user.manager.UserManager;
import im.zhaojun.zfile.module.user.mapper.UserMapper;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.request.*;
import im.zhaojun.zfile.module.user.model.response.UserDetailResponse;
import im.zhaojun.zfile.module.user.utils.PasswordVerifyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static im.zhaojun.zfile.module.user.service.UserService.USER_CACHE_KEY;

@Slf4j
@Service
@CacheConfig(cacheNames = USER_CACHE_KEY)
public class UserService {

    public static final String USER_CACHE_KEY = "user";

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserManager userManager;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private ZFileCacheManager zfileCacheManager;

    /**
     * 根据用户 ID 获取用户
     *
     * @param   id
     *          用户 ID
     *
     * @return  用户
     */
    @Cacheable(key = "#id", unless = "#result == null", condition = "#id != null")
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    @Cacheable(key = "#username", unless = "#result == null", condition = "#username != null")
    public Integer getIdByUsername(String username) {
        return userMapper.findIdByUsername(username);
    }

    /**
     * 根据用户名获取用户
     *
     * @param   username
     *          用户名
     *
     * @return  用户
     */
    public User getByUsername(String username) {
        UserService userService = (UserService) AopContext.currentProxy();
        Integer userId = userService.getIdByUsername(username);
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }


    /**
     * 根据用户 ID 获取用户详细信息，包含用户的基本信息和用户与存储策略的关联关系.
     *
     * @param   userId
     *          用户 ID
     *
     * @return  用户详细信息
     */
    public UserDetailResponse getUserDetailById(Integer userId) {
        User user = ((UserService) AopContext.currentProxy()).getById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.BIZ_USER_NOT_EXIST);
        }
        return userManager.assembleUserDetail(user);
    }


    /**
     * 根据查询条件查询用户列表
     *
     * @param   queryObj
     *          查询条件对象
     *
     * @return  用户列表
     */
    public List<UserDetailResponse> listUserDetail(QueryUserRequest queryObj) {
        List<UserDetailResponse> userDetailResponseList = new ArrayList<>();

        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .orderBy(true, queryObj.getSortAsc(), StringUtils.camelToUnderline(queryObj.getSortField()))
                .lambda()
                .like(ObjUtil.isNotEmpty(queryObj.getUsername()), User::getUsername, queryObj.getUsername())
                .like(ObjUtil.isNotEmpty(queryObj.getNickname()), User::getNickname, queryObj.getNickname())
                .eq(ObjUtil.isNotEmpty(queryObj.getEnable()), User::getEnable, queryObj.getEnable())
                .ge(ObjUtil.isNotEmpty(queryObj.getDateFrom()), User::getCreateTime, queryObj.getDateFrom())
                .le(ObjUtil.isNotEmpty(queryObj.getDateTo()), User::getCreateTime, queryObj.getDateTo());

        List<User> users = userMapper.selectList(queryWrapper);

        users.forEach(user -> userDetailResponseList.add(userManager.assembleUserDetail(user)));
        if (queryObj.getHideDisabledStorage()) {
            userDetailResponseList.forEach(userDetailResponse -> {
                userDetailResponse.getUserStorageSourceList().removeIf(userStorageSourceDetailDTO -> !userStorageSourceDetailDTO.getEnable());
            });
        }
        return userDetailResponseList;
    }


    /**
     * 保存或更新用户
     *
     * @param   saveUserRequest
     *          用户信息
     *
     * @return  保存后的用户
     */
    @CacheEvict(allEntries = true)
    public User saveOrUpdate(SaveUserRequest saveUserRequest) {
        // 校验用户是否存在
        boolean userNameIsDuplicate = checkDuplicateUsername(saveUserRequest.getId(), saveUserRequest.getUsername());
        if (userNameIsDuplicate) {
            throw new BizException(ErrorCode.BIZ_USER_EXIST);
        }

        User user = new User();
        user.setId(saveUserRequest.getId());
        user.setUsername(saveUserRequest.getUsername());
        user.setNickname(saveUserRequest.getNickname());
        user.setEnable(saveUserRequest.getEnable());
        user.setDefaultPermissions(saveUserRequest.getDefaultPermissions());
        if (StringUtils.isNotBlank(saveUserRequest.getPassword())) {
            passwordEncryptAndSet(saveUserRequest.getPassword(), user);
        }

        userManager.saveUserInfo(user, saveUserRequest.getUserStorageSourceList());
        if (user.getId() != null) {
            zfileCacheManager.clearUserEnableStorageSourceCache(user.getId());
        }
        return user;
    }


    /**
     * 更新用户启用状态
     *
     * @param   id
     *          用户 ID
     *
     * @param   enable
     *          是否启用
     */
    @CacheEvict(key = "#id")
    public void updateUserEnable(Integer id, boolean enable) {
        User user = new User();
        user.setId(id);
        user.setEnable(enable);
        userMapper.updateById(user);
        zfileCacheManager.clearUserEnableStorageSourceCache(id);
    }


    /**
     * 初始化管理员用户名、密码、权限.
     *
     * @param   username
     *          用户名
     *
     * @param   password
     *          密码
     *
     * @return  是否更新成功
     */
    public boolean initAdminUser(String username, String password) {
        User user = userMapper.selectById(UserConstant.ADMIN_ID);
        if (user == null) {
            throw new BizException("系统异常，管理员用户不存在，请检测数据库或重建数据库。");
        }

        user.setUsername(username);
        passwordEncryptAndSet(password, user);

        // 管理员用户默认权限
        Set<String> defaultPermissions = new HashSet<>();
        for (FileOperatorTypeEnum value : FileOperatorTypeEnum.values()) {
            if (!StringUtils.startWith(value.getValue(), "ignore")) {
                defaultPermissions.add(value.getValue());
            }
        }
        user.setDefaultPermissions(defaultPermissions);
        return userMapper.updateById(user) == 1;
    }


    /**
     * 修改当前用户的用户名和密码，需要校验旧密码是否正确.
     *
     * @param   updateUserPwdRequest
     *          修改密码请求对象
     */
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateUserNameAndPwdById(Integer id, UpdateUserPwdRequest updateUserPwdRequest) {
        User user = userMapper.selectById(id);
        if (user == null || Objects.equals(id, UserConstant.ANONYMOUS_ID)) {
            throw new BizException(ErrorCode.BIZ_USER_NOT_EXIST);
        }

        // 验证旧密码是否正确，如果旧密码不为空，则进行验证
        if (StringUtils.isNotBlank(user.getPassword()) &&
                !PasswordVerifyUtils.verify(user.getPassword(), user.getSalt(), updateUserPwdRequest.getOldPassword())) {
            throw new BizException(ErrorCode.BIZ_OLD_PASSWORD_ERROR);
        }

        // 验证新密码和确认密码是否一致
        if (!updateUserPwdRequest.getNewPassword().equals(updateUserPwdRequest.getConfirmPassword())) {
            throw new BizException(ErrorCode.BIZ_PASSWORD_NOT_SAME);
        }

        passwordEncryptAndSet(updateUserPwdRequest.getNewPassword(), user);
        userMapper.updateById(user);
    }


    /**
     * 根据 ID 删除用户，无法删除内置的管理员和匿名用户，删除是会自动删除用户与存储策略的关联关系.
     *
     * @param   id
     *          要删除调用用户 ID
     */
    @CacheEvict(allEntries = true)
    public void deleteById(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.BIZ_USER_NOT_EXIST);
        }
        if (user.getId() <= UserConstant.ANONYMOUS_ID) {
            throw new BizException(ErrorCode.BIZ_DELETE_BUILT_IN_USER);
        }

        // 删除用户及关联的数据
        userManager.deleteAllByUserId(id);

        // 发布用户删除事件
        applicationEventPublisher.publishEvent(new UserDeleteEvent(user));
        zfileCacheManager.clearUserEnableStorageSourceCache(id);
    }


    /**
     * 根据用户 ID 判断是否是管理员，(管理员 ID 强制为 1)
     *
     * @param   id
     *          用户 ID
     *
     * @return  是否是管理员
     */
    public boolean isAdmin(Integer id) {
        return ObjUtil.equal(UserConstant.ADMIN_ID, id);
    }


    /**
     * 根据用户名判断是否是管理员，(管理员 ID 强制为 1)
     *
     * @param   username
     *          用户名
     *
     * @return  是否是管理员
     */
    public boolean isAdmin(String username) {
        User user = ((UserService) AopContext.currentProxy()).getByUsername(username);
        return user != null && isAdmin(user.getId());
    }


    /**
     * 检查用户名是否重复
     *
     * @param   ignoreUserId
     *          忽略的用户 ID, 用于检查重复时, 排除自身.
     *
     * @param   username
     *          要检查的用户名
     *
     * @return  是否重复
     */
    public boolean checkDuplicateUsername(Integer ignoreUserId, String username) {
        return userMapper.countByUsername(username, ignoreUserId) > 0;
    }

    /**
     * 重置管理员用户的用户名、密码。
     *
     * @param   requestObj
     *          重置用户名和密码请求对象
     */
    @CacheEvict(allEntries = true)
    public void resetAdminLoginInfo(ResetAdminUserNameAndPasswordRequest requestObj) {
        User user = userMapper.selectById(UserConstant.ADMIN_ID);
        user.setUsername(requestObj.getUsername());
        user.setPassword(SecureUtil.md5(requestObj.getPassword()));
        passwordEncryptAndSet(requestObj.getPassword(), user);
        userMapper.updateById(user);

    }

    /**
     * 密码加盐并设置到用户对象中
     *
     * @param   originPassword
     *          原始密码
     *
     * @param   user
     *          用户对象
     */
    private static void passwordEncryptAndSet(String originPassword, User user) {
        Pair<String, String> encryptPair = PasswordVerifyUtils.encrypt(originPassword);
        user.setPassword(encryptPair.getFirst());
        user.setSalt(encryptPair.getSecond());
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer copy(CopyUserRequest copyUserRequest) {
        // 检查目标用户名是否已存在
        String toUsername = copyUserRequest.getToUsername();
        boolean existUser = ((UserService)AopContext.currentProxy()).getByUsername(toUsername) != null;
        if (existUser) {
            throw new BizException(ErrorCode.BIZ_USER_EXIST);
        }

        // 检查复制源是否存在
        Integer fromUserId = copyUserRequest.getFromId();
        User user = ((UserService)AopContext.currentProxy()).getById(fromUserId);
        if (user == null) {
            throw new BizException(ErrorCode.BIZ_USER_NOT_EXIST);
        }

        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        newUser.setId(null);
        newUser.setCreateTime(null);
        newUser.setUsername(null);
        newUser.setUsername(copyUserRequest.getToUsername());
        newUser.setNickname(copyUserRequest.getToNickname());
        if (StringUtils.isNotEmpty(copyUserRequest.getToPassword())) {
            passwordEncryptAndSet(copyUserRequest.getToPassword(), newUser);
        }
        userMapper.insert(newUser);

        Integer newUserId = newUser.getId();
        log.info("复制用户成功，源 [id: {}, username: {}, nickname: {}], 复制后 [id: {}, username: {}, nickname: {}]",
                fromUserId, user.getUsername(), user.getNickname(),
                newUserId, newUser.getUsername(), user.getNickname());

        UserCopyEvent userCopyEvent = new UserCopyEvent(fromUserId, newUserId);
        applicationEventPublisher.publishEvent(userCopyEvent);
        return newUserId;
    }

}