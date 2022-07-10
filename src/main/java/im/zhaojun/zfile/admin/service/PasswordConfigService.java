package im.zhaojun.zfile.admin.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.PasswordConfigMapper;
import im.zhaojun.zfile.admin.model.entity.PasswordConfig;
import im.zhaojun.zfile.admin.model.verify.VerifyResult;
import im.zhaojun.zfile.common.util.AjaxJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 存储源密码配置 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
public class PasswordConfigService extends ServiceImpl<PasswordConfigMapper, PasswordConfig> {

    @Resource
    private PasswordConfigMapper passwordConfigMapper;


    /**
     * 存储源 ID -> 密码规则列表(全部)缓存.
     */
    private final Map<Integer, List<PasswordConfig>> baseCache = new HashMap<>();


    /**
     * 根据存储源 ID 查询密码规则列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  密码规则列表
     */
    public List<PasswordConfig> findByStorageId(Integer storageId) {
        if (baseCache.get(storageId) != null) {
            return baseCache.get(storageId);
        } else {
            List<PasswordConfig> dbResult = passwordConfigMapper.findByStorageId(storageId);
            baseCache.put(storageId, dbResult);
            return dbResult;
        }
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
        passwordConfigMapper.deleteByStorageId(storageId);
        super.saveBatch(passwordConfigList);
        baseCache.put(storageId, passwordConfigList);
    }


    /**
     * 校验密码
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   path
     *          请求路径
     *
     * @param   inputPassword
     *          用户输入的密码
     *
     * @return  是否校验通过
     */
    public VerifyResult verifyPassword(Integer storageId, String path, String inputPassword) {
        List<PasswordConfig> passwordConfigList = findByStorageId(storageId);

        for (PasswordConfig passwordConfig : passwordConfigList) {
            String expression = passwordConfig.getExpression();
            if (StrUtil.isEmpty(expression)) {
                continue;
            }

            try {
                PathMatcher pathMatcher = FileSystems.getDefault()
                                            .getPathMatcher("glob:" + expression);
                // 判断当前请求路径是否和规则路径表达式匹配
                boolean match = pathMatcher.matches(Paths.get(path));
                // 如果匹配且输入了密码则校验
                if (match) {
                    if (StrUtil.isEmpty(inputPassword)) {
                        return VerifyResult.fail("此文件夹需要密码.", AjaxJson.REQUIRED_PASSWORD);
                    }

                    String expectedPassword = passwordConfig.getPassword();
                    if (matchPassword(expectedPassword, inputPassword)) {
                        log.debug("匹配文件夹密码 path: {}, expression: {}, 用户输入: {}, 匹配成功", path, expression, inputPassword);
                        return VerifyResult.success(expression);
                    }
                    log.debug("匹配文件夹密码 path: {}, expression: {}, 用户输入: {}, 不匹配.", path, expression, inputPassword);
                    return VerifyResult.fail("此文件夹密码错误.", AjaxJson.INVALID_PASSWORD);
                }
            } catch (Exception e) {
                log.warn("匹配文件夹密码 path: {}, expression: {}, 用户输入: {}, 解析错误, 跳过此规则.",
                        path, expression, inputPassword, e);
            }
        }

        log.debug("校验文件夹密码 path: {}, 没有匹配的表达式, 不进行密码校验.", path);
        return VerifyResult.success();
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

        if (expectedPasswordContent == null) {
            return false;
        }

        if (password == null) {
            return false;
        }

        expectedPasswordContent = expectedPasswordContent.replace("\n", "").trim();
        password = password.replace("\n", "").trim();
        return Objects.equals(expectedPasswordContent, password);
    }

}