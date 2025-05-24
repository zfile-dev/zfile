package im.zhaojun.zfile.module.password.service;

import cn.hutool.core.util.ObjectUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.PatternMatcherUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.password.mapper.PasswordConfigMapper;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.model.entity.PasswordConfig;
import im.zhaojun.zfile.module.storage.event.StorageSourceCopyEvent;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 存储源密码配置 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "passwordConfig")
public class PasswordConfigService {

    @Resource
    private PasswordConfigMapper passwordConfigMapper;

    @Resource
    private UserStorageSourceService userStorageSourceService;


    /**
     * 根据存储源 ID 查询密码规则列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  密码规则列表
     */
    @Cacheable(key = "#storageId",
            condition = "#storageId != null")
    public List<PasswordConfig> findByStorageId(Integer storageId) {
        return passwordConfigMapper.findByStorageId(storageId);
    }


    /**
     * 批量保存指定存储源 ID 的密码规则列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   passwordConfigList
     *          存储源类别
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Integer storageId, List<PasswordConfig> passwordConfigList) {
        ((PasswordConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);
        log.info("更新存储源 ID 为 {} 的过滤规则 {} 条", storageId, passwordConfigList.size());

        passwordConfigList.forEach(passwordConfig -> {
            passwordConfig.setId(null);
            passwordConfig.setStorageId(storageId);
            passwordConfigMapper.insert(passwordConfig);

            if (log.isDebugEnabled()) {
                log.debug("新增过滤规则, 存储源 ID: {}, 表达式: {}, 描述: {}, 密码: {}",
                        passwordConfig.getStorageId(), passwordConfig.getExpression(),
                        passwordConfig.getDescription(), passwordConfig.getPassword());
            }
        });
    }


    /**
     * 根据存储源 id 删除所有密码规则
     *
     * @param   storageId
     *          存储源 ID
     */
    @CacheEvict(key = "#storageId")
    public int deleteByStorageId(Integer storageId) {
        int deleteSize = passwordConfigMapper.deleteByStorageId(storageId);
        log.info("删除存储源 ID 为 {} 的密码规则 {} 条", storageId, deleteSize);
        return deleteSize;
    }

    /**
     * 监听存储源删除事件，根据存储源 id 删除相关的密码设置
     *
     * @param   storageSourceDeleteEvent
     *          存储源删除事件
     */
    @EventListener
    public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
        Integer storageId = storageSourceDeleteEvent.getId();
        int updateRows = ((PasswordConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);
        if (log.isDebugEnabled()) {
            log.debug("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源密码设置 {} 条",
                    storageId,
                    storageSourceDeleteEvent.getName(),
                    storageSourceDeleteEvent.getType().getDescription(),
                    updateRows);
        }
    }

    /**
     * 校验密码
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   path
     *          请求路径(全路径，包含用户目录)
     *
     * @param   inputPassword
     *          用户输入的密码
     *
     * @return  是否校验通过
     */
    public VerifyResultDTO verifyPassword(Integer storageId, String path, String inputPassword) {
        // 判断是否需要忽略密码校验
        boolean isIgnorePassword = userStorageSourceService.hasCurrentUserStorageOperatorPermission(storageId, FileOperatorTypeEnum.IGNORE_PASSWORD);
        if (isIgnorePassword) {
            if (log.isDebugEnabled()) {
                log.debug("权限配置忽略密码校验, 请求路径: {}, 存储源 ID: {}, 输入密码: {}", path, storageId, inputPassword);
            }
            return VerifyResultDTO.success();
        }

        List<PasswordConfig> passwordConfigList = ((PasswordConfigService) AopContext.currentProxy()).findByStorageId(storageId);

        // 如果规则列表为空, 则表示不需要过滤, 直接返回 false
        if (CollectionUtils.isEmpty(passwordConfigList)) {
            if (log.isDebugEnabled()) {
                log.debug("密码规则列表为空, 请求路径: {}, 存储源 ID: {}, 输入密码: {}", path, storageId, inputPassword);
            }
            return VerifyResultDTO.success();
        }

        // 校验密码
        for (PasswordConfig passwordConfig : passwordConfigList) {
            String expression = passwordConfig.getExpression();
            String expectPassword = passwordConfig.getPassword();

            // 规则为空跳过
            if (StringUtils.isEmpty(expression)) {
                if (log.isDebugEnabled()) {
                    log.debug("密码规则测试表达式: {}, 请求路径: {}, 表达式为空，跳过该规则比对", expression, path);
                }
                continue;
            }

            try {
                // 判断当前请求路径是否和规则路径表达式匹配
                boolean match = PatternMatcherUtils.testCompatibilityGlobPattern(expression, path);

                if (log.isDebugEnabled()) {
                    log.debug("密码规则测试表达式: {}, 请求路径: {}, 匹配结果: {}, 预期密码: {}, 输入密码; {}", expression, path, match, expectPassword, inputPassword);
                }

                // 如果匹配且输入了密码则校验
                if (match) {
                    if (StringUtils.isEmpty(inputPassword)) {
                        if (log.isDebugEnabled()) {
                            log.debug("密码规则匹配, 但未输入密码；" +
                                            "表达式: {}, 请求路径: {}, 存储源 ID: {}, 预期密码：{}, 输入密码: {}",
                                    expression, path, storageId, expectPassword, inputPassword);
                        }
                        return VerifyResultDTO.fail(ErrorCode.BIZ_STORAGE_SOURCE_FOLDER_PASSWORD_REQUIRED);
                    }

                    if (matchPassword(expectPassword, inputPassword)) {
                        if (log.isDebugEnabled()) {
                            log.debug("密码规则匹配, 密码校验通过；" +
                                            "表达式: {}, 请求路径: {}, 存储源 ID: {}, 预期密码：{}, 输入密码: {}",
                                    expression, path, storageId, expectPassword, inputPassword);
                        }
                        return VerifyResultDTO.success(expression);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("密码规则匹配, 但输入密码与预期密码不同；" +
                                        "表达式: {}, 请求路径: {}, 存储源 ID: {}, 预期密码：{}, 输入密码: {}",
                                expression, path, storageId, expectPassword, inputPassword);
                    }
                    return VerifyResultDTO.fail(ErrorCode.BIZ_STORAGE_SOURCE_FOLDER_PASSWORD_ERROR);
                }
            } catch (Exception e) {
                log.error("密码规则匹配出现异常，表达式: {}, 请求路径: {}, 存储源 ID: {}, 预期密码：{}, 输入密码: {}, 解析错误, 跳过此规则.",
                        expression, path, storageId, expectPassword, inputPassword, e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("校验文件夹密码 path: {}, 没有匹配的表达式, 不进行密码校验.", path);
        }

        return VerifyResultDTO.success();
    }


    /**
     * 校验两个密码是否相同, 忽略空白字符
     *
     * @param   expectedPasswordContent
     *          预期密码
     *
     * @param   password
     *          实际输入密码
     *
     * @return  是否匹配
     */
    private boolean matchPassword(String expectedPasswordContent, String password) {
        if (Objects.equals(expectedPasswordContent, password)) {
            return true;
        }

        // 如果预期密码或输入密码为空, 则不匹配
        if (ObjectUtil.hasNull(expectedPasswordContent, password)) {
            return false;
        }

        expectedPasswordContent = StringUtils.removeAllLineBreaksAndTrim(expectedPasswordContent);
        password = StringUtils.removeAllLineBreaksAndTrim(password);
        return Objects.equals(expectedPasswordContent, password);
    }


    /**
     * 监听存储源复制事件, 复制存储源时, 复制存储源密码设置
     *
     * @param   storageSourceCopyEvent
     *          存储源复制事件
     */
    @EventListener
    public void onStorageSourceCopy(StorageSourceCopyEvent storageSourceCopyEvent) {
        Integer fromId = storageSourceCopyEvent.getFromId();
        Integer newId = storageSourceCopyEvent.getNewId();

        List<PasswordConfig> passwordConfigList = ((PasswordConfigService) AopContext.currentProxy()).findByStorageId(fromId);

        passwordConfigList.forEach(passwordConfig -> {
            PasswordConfig newPasswordConfig = new PasswordConfig();
            BeanUtils.copyProperties(passwordConfig, newPasswordConfig);
            newPasswordConfig.setId(null);
            newPasswordConfig.setStorageId(newId);
            passwordConfigMapper.insert(newPasswordConfig);
        });

        log.info("复制存储源 ID 为 {} 的存储源密码设置到存储源 ID 为 {} 成功, 共 {} 条", fromId, newId, passwordConfigList.size());
    }

}