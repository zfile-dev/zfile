package im.zhaojun.zfile.module.sso.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.sso.mapper.SsoConfigMapper;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import im.zhaojun.zfile.module.sso.model.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 单点登录服务
 *
 * @author OnEvent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoService
{
    private static final String HOST = "http://localhost:8080"; // TODO 这里需要一个环境变量用来配置重定向地址
    private static final String REDIRECT_URI = "/sso/{}/login/callback";
    private final SsoConfigMapper ssoConfigMapper;

    /**
     * 在系统中插入新的单点登录服务商配置<br/>
     * 如果配置中提供了 "Well-Known" URL，系统会尝试自动获取并解析 SSO 端点数据<br/>
     * 注意: <strong>通过 Well-Known 获取到的配置会将手动填写的配置信息覆盖</strong><br/>
     * 如果系统自动获取的配置无法解析，则会失败<br/>
     *
     * @param provider 要插入的单点登录（SSO）提供程序配置对象
     * @return 表示操作结果的字符串
     */
    public AjaxJson<Void> insertProvider(SsoConfig provider)
    {
        if (!StrUtil.isEmpty(provider.getWellKnownUrl()))
        {
            var wellKnownStr = HttpUtil.get(provider.getWellKnownUrl());
            var wellKnown = JSONUtil.parseObj(wellKnownStr);

            var authUrl = wellKnown.getStr("authorization_endpoint");
            var tokenUrl = wellKnown.getStr("token_endpoint");
            var userInfoUrl = wellKnown.getStr("userinfo_endpoint");
            if (StrUtil.isEmpty(authUrl) || StrUtil.isEmpty(tokenUrl) || StrUtil.isEmpty(userInfoUrl))
            {
                return AjaxJson.getError("Well-Known 信息错误, 自动发现配置失败, 请检查配置或直接填写全部配置");
            }

            provider.setAuthUrl(authUrl);
            provider.setTokenUrl(tokenUrl);
            provider.setUserInfoUrl(userInfoUrl);
        }
        provider.setEnabled(true);
        var result = ssoConfigMapper.insert(provider);
        return result > 0 ? AjaxJson.getSuccess() : AjaxJson.getError("插入失败, 请检查配置");
    }

    public AjaxJson<Void> deleteProvider(String provider)
    {
        var result = ssoConfigMapper.deleteById(provider);
        return result > 0 ? AjaxJson.getSuccess() : AjaxJson.getError("删除失败, 请检查配置");
    }

    public AjaxJson<Void> modifyProvider(SsoConfig provider)
    {
        var result = ssoConfigMapper.updateById(provider);
        return result > 0 ? AjaxJson.getSuccess() : AjaxJson.getError("修改失败, 请检查配置");
    }

    /**
     * 读取指定单点登录服务商的配置<br/>
     * 安全起见，配置中的 Client Secret 会被部分隐藏
     *
     * @param provider 要获取的单点登录提供商的名称
     * @return 指定的单点登录服务商配置信息
     */
    public AjaxJson<?> getProvider(String provider)
    {
        var result = ssoConfigMapper.findByProvider(provider);
        if (ObjectUtil.isNull(result))
        {
            return AjaxJson.getError("单点登录厂商配置不存在, 请检查配置");
        }
        result.setClientSecret(StrUtil.hide(result.getClientSecret(), 5, result.getClientSecret().length() - 5));
        return AjaxJson.getSuccessData(result);
    }

    /**
     * 获取 OIDC/OAuth2 的授权地址<br/>
     * 并由后端控制页面重定向到 OIDC/OAuth2 服务器的授权页面
     *
     * @param state 状态值
     * @return 授权地址
     */
    public String getAuthRedirectUrl(String provider, String state)
    {
        var config = ssoConfigMapper.findByProvider(provider);
        if (ObjectUtil.isNull(config))
        {
            return "/sso/login/error?err=" + URLUtil.encode("供应商: [" + provider + "] 不存在, 请检查配置");
        }
        log.info("[Authorization] 单点登录厂商配置信息: {}", JSONUtil.toJsonPrettyStr(config));

        var authParamsMap = new HashMap<String, String>()
        {{
            put("response_type", "code");
            put("client_id", config.getClientId());
            put("redirect_uri", HOST + StrUtil.format(REDIRECT_URI, provider));
            put("state", state);
            put("scope", config.getScope());
        }};
        var authParamsStr = HttpUtil.toParams(authParamsMap);
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
    public String callbackHandler(String provider, String code)
    {
        var config = ssoConfigMapper.findByProvider(provider);
        log.info("[Callback] 单点登录厂商配置信息: {}", JSONUtil.toJsonPrettyStr(config));

        // 获取 Access Token
        var tokenParamsMap = new HashMap<String, String>()
        {{
            put("code", code);
            put("client_id", config.getClientId());
            put("client_secret", config.getClientSecret());
            put("redirect_uri", HOST + StrUtil.format(REDIRECT_URI, provider));
            put("grant_type", "authorization_code");
        }};

        var tokenStr = HttpUtil
                .createPost(config.getTokenUrl())
                .header(Header.ACCEPT, "application/json")
                .body(HttpUtil.toParams(tokenParamsMap))
                .execute()
                .body();
        log.info("[Token] 单点登录厂商返回的 Token 信息: {}", JSONUtil.toJsonPrettyStr(tokenStr));
        var token = JSONUtil.toBean(tokenStr, TokenResponse.class);

        if ("bearer".equalsIgnoreCase(token.getTokenType()))
        {
            return "/sso/login/error?err=" + URLUtil.encode("Access Token 类型错误, 需要 Bearer 类型, 请检查配置");
        }

        if (ObjectUtil.isNull(token) || StrUtil.isEmpty(token.getAccessToken()))
        {
            return "/sso/login/error?err=" + URLUtil.encode("获取 Access Token 失败, 请检查配置");
        }

        // 获取用户信息
        var userInfoStr = HttpUtil
                .createGet(config.getUserInfoUrl())
                .bearerAuth(token.getAccessToken())
                .execute()
                .body();
        log.info("[UserInfo] 单点登录服务商处的用户信息: {}", JSONUtil.toJsonPrettyStr(userInfoStr));

        var bindingField = JSONUtil.parse(userInfoStr).getByPath(config.getBindingField());
        log.info("[UserInfo] 绑定字段 [{}]: {}", config.getBindingField(), bindingField);

        if (ObjectUtil.isNull(userInfoStr) || ObjectUtil.isEmpty(bindingField))
        {
            return "/sso/login/error?err=" + URLUtil.encode("获取用户信息失败, 请检查配置");
        }

        // 调用 Sa Token 的登录方法
        // TODO 这里要处理一下，如果没有对应用户，则创建用户
        StpUtil.login(bindingField.toString(), new SaLoginModel().setToken(token.getAccessToken()));

        return "/sso/login/success";
    }
}
