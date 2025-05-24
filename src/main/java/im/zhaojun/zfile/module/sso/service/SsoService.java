package im.zhaojun.zfile.module.sso.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.ErrorPageBizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.sso.mapper.SsoConfigMapper;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import im.zhaojun.zfile.module.sso.model.response.SsoLoginItemResponse;
import im.zhaojun.zfile.module.sso.model.response.TokenResponse;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.request.CopyUserRequest;
import im.zhaojun.zfile.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static im.zhaojun.zfile.module.sso.service.SsoService.SSO_CONFIG_CACHE_KEY;

/**
 * 单点登录服务
 *
 * @author OnEvent
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = SSO_CONFIG_CACHE_KEY)
public class SsoService {

    public static final String SSO_CONFIG_CACHE_KEY = "ssoConfig";

    private static final String REDIRECT_URI = "/sso/{}/login/callback";

    private final SsoConfigMapper ssoConfigMapper;

    private final SystemConfigService systemConfigService;

    private final UserService userService;

    public List<SsoConfig> list() {
        return ssoConfigMapper.findAll();
    }

    public List<SsoLoginItemResponse> listAllLoginItems() {
        return ssoConfigMapper.findAllLoginItems();
    }

    @Cacheable(key = "#provider", unless = "#result == null", condition = "#provider != null")
    public SsoConfig getProvider(String provider) {
        return ssoConfigMapper.findByProvider(provider);
    }

    @CacheEvict(key = "#result.provider")
    public SsoConfig saveOrUpdate(SsoConfig ssoConfig) {
        boolean providerIsDuplicate = checkDuplicateProvider(ssoConfig.getId(), ssoConfig.getProvider());
        if (providerIsDuplicate) {
            throw new BizException(ErrorCode.BIZ_SSO_PROVIDER_EXIST);
        }

        if (ssoConfig.getId() == null) {
            ssoConfigMapper.insert(ssoConfig);
        } else {
            ssoConfigMapper.updateById(ssoConfig);
        }
        return ssoConfig;
    }

    @CacheEvict(key = "#provider")
    public void deleteProvider(String provider) {
        ssoConfigMapper.deleteById(provider);
    }

    public boolean checkDuplicateProvider(Integer ignoreId, String provider) {
        return ssoConfigMapper.countByProvider(provider, ignoreId) > 0;
    }

    /**
     * 获取 OIDC/OAuth2 的授权地址<br/>
     * 并由后端控制页面重定向到 OIDC/OAuth2 服务器的授权页面
     *
     * @param state 状态值
     * @return 授权地址
     */
    public String getAuthRedirectUrl(String provider, String state) {
        SsoConfig config = ((SsoService) AopContext.currentProxy()).getProvider(provider);
        if (ObjectUtil.isNull(config)) {
            throw new ErrorPageBizException("供应商: [" + provider + "] 不存在, 请检查配置");
        }

        if (BooleanUtil.isFalse(config.getEnabled())) {
            throw new ErrorPageBizException(ErrorCode.BIZ_SSO_PROVIDER_DISABLED);
        }

        Map<String, String> authParamsMap = new HashMap<>() {{
            put("response_type", "code");
            put("client_id", config.getClientId());
            put("redirect_uri", systemConfigService.getAxiosFromDomainOrSetting() + StrUtil.format(REDIRECT_URI, provider));
            put("state", state);
            put("scope", config.getScope());
        }};
        String authParamsStr = HttpUtil.toParams(authParamsMap);
        return config.getAuthUrl() + "?" + authParamsStr;
    }

    /**
     * 处理 OIDC/OAuth2 的回调，并同时获取用户信息，利用用户信息中的邮箱完成登录<br/>
     * 若一切顺利则返回到成功页面<br/>
     * 当在获取 AK 和用户信息时发生错误时，返回错误页面
     *
     * @param code 授权码
     * @return 重定向的页面路径
     */
    public String callbackHandler(String provider, String code) {
        SsoConfig config = ((SsoService) AopContext.currentProxy()).getProvider(provider);
        if (log.isDebugEnabled()) {
            log.debug("[Callback] 单点登录厂商 {} 配置信息: {}", provider, config);
        }

        if (BooleanUtil.isFalse(config.getEnabled())) {
            throw new ErrorPageBizException(ErrorCode.BIZ_SSO_PROVIDER_DISABLED);
        }

        // 获取 Access Token
        Map<String, String> tokenParamsMap = new HashMap<>() {{
            put("code", code);
            put("client_id", config.getClientId());
            put("client_secret", config.getClientSecret());
            put("redirect_uri", systemConfigService.getAxiosFromDomainOrSetting() + StrUtil.format(REDIRECT_URI, provider));
            put("grant_type", "authorization_code");
        }};

        HttpResponse getTokenResponse = HttpUtil.createPost(config.getTokenUrl())
                .header(Header.ACCEPT, "application/json")
                .body(HttpUtil.toParams(tokenParamsMap))
                .execute();
        String tokenStr = getTokenResponse.body();
        if (log.isDebugEnabled()) {
            log.debug("[Token] 单点登录厂商返回的 Token 信息: {}", tokenStr);
        }
        if (!getTokenResponse.isOk()) {
            log.error("单点登录厂商 {} 返回错误: {}, 错误信息: {}", provider, getTokenResponse.getStatus(), tokenStr);
            throw new ErrorPageBizException("单点登录失败: " + getTokenResponse.getStatus() + ", " + tokenStr);
        }

        TokenResponse token = JSON.parseObject(tokenStr, TokenResponse.class, JSONReader.Feature.SupportSmartMatch);
        if (!"bearer".equalsIgnoreCase(token.getTokenType())) {
            throw new ErrorPageBizException("Access Token 类型错误, 需要 Bearer 类型, 请检查配置");
        }


        // 获取用户信息
        HttpResponse userInfoResponse = HttpUtil
                .createGet(config.getUserInfoUrl())
                .bearerAuth(token.getAccessToken())
                .execute();
        String userInfoStr = userInfoResponse.body();
        if (log.isDebugEnabled()) {
            log.debug("[UserInfo] 单点登录服务商处请求 {} 的用户信息: {}，将尝试通过 {} 表达式获取字段", config.getUserInfoUrl(), userInfoStr, config.getBindingField());
        }
        if (!userInfoResponse.isOk()) {
            log.error("单点登录服务商 {} 返回错误: {}, 错误信息: {}", provider, userInfoResponse.getStatus(), userInfoStr);
            throw new ErrorPageBizException("从单点登录服务商获取用户信息失败: " + userInfoResponse.getStatus() + ", " + userInfoStr);
        }

        Object bindingField = JSON.parseObject(userInfoStr, JSONReader.Feature.SupportSmartMatch).getByPath(config.getBindingField());
        if (log.isDebugEnabled()) {
            log.debug("[UserInfo] 通过表达式 [{}] 获取到字段: {}", config.getBindingField(), bindingField);
        }

        if (StrUtil.isBlankIfStr(bindingField)) {
            throw new ErrorPageBizException("解析用户信息失败, 请检查配置");
        }


        String bindingFieldStr = Convert.toStr(bindingField);
        User user = userService.getByUsername(bindingFieldStr);
        if (user == null) {
            User templateUser = userService.getById(UserConstant.NEW_USER_TEMPLATE_ID);
            if (!BooleanUtil.isTrue(templateUser.getEnable())) {
                throw new ErrorPageBizException("当前系统未启用新用户注册, 请联系管理员");
            }

            CopyUserRequest copyUserRequest = new CopyUserRequest();
            copyUserRequest.setFromId(UserConstant.NEW_USER_TEMPLATE_ID);
            copyUserRequest.setToNickname(bindingFieldStr);
            copyUserRequest.setToUsername(bindingFieldStr);
            Integer newUserId = userService.copy(copyUserRequest);

            log.info("新用户 {} 通过单点登录注册成功, ID: {}", bindingFieldStr, newUserId);

            user = new User();
            user.setId(newUserId);
        }

        StpUtil.login(user.getId());
        String axiosFromDomainOrSetting = systemConfigService.getAxiosFromDomainOrSetting();
        String frontDomain = systemConfigService.getFrontDomain();

        String redirectUrl;
        if (StringUtils.isBlank(frontDomain)) {
            redirectUrl = axiosFromDomainOrSetting;
        } else {
            // 如果配置了前端域名，则跳转到前端域名
            redirectUrl = frontDomain + "/sso?token=" + StpUtil.getTokenValue();
        }

        if (log.isDebugEnabled()) {
            log.debug("用户 {} 通过单点登录登录成功, ID: {}，将跳转到 {}, token 为 {}", bindingFieldStr, user.getId(), redirectUrl, StpUtil.getTokenValue());
        }
        return redirectUrl;
    }

}