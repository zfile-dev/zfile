package im.zhaojun.zfile.module.sso.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import im.zhaojun.zfile.module.sso.mapper.SsoConfigMapper;
import im.zhaojun.zfile.module.sso.model.response.TokenResponse;
import im.zhaojun.zfile.module.sso.model.response.UserInfoResponse;
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
    // TODO 同时考虑是否需要利用 Well-Known 来获取相关信息
    private static final String HOST = "http://localhost:8080"; // TODO 这里需要一个环境变量用来配置重定向地址
    private static final String REDIRECT_URI = "/sso/{}/login/callback";
    private final SsoConfigMapper ssoConfigMapper;

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
            return "/sso/login/error?err=" + URLUtil.encode("获取用户信息失败, 请检查配置"); // TODO 同上一个错误处理
        }

        // 调用 Sa Token 的登录方法
        // TODO 这里要处理一下，如果没有对应用户，则创建用户
        StpUtil.login(bindingField.toString(), new SaLoginModel().setToken(token.getAccessToken()));

        return "/sso/login/success";
    }
}
