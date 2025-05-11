package im.zhaojun.zfile.module.sso.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import im.zhaojun.zfile.module.sso.model.response.TokenResponse;
import im.zhaojun.zfile.module.sso.model.response.UserInfoResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 单点登录服务
 *
 * @author OnEvent
 */
@Service
public class SsoService
{
    // TODO 这里需要修改为从数据库或配置文件中读取
    // TODO 同时考虑是否需要利用 Well-Known 来获取相关信息
    private static final String CLIENT_ID = "<Client ID>";
    private static final String CLIENT_SECRET = "<Client Secret>";
    private static final String AUTH_URL = "<Auth URL>";
    private static final String TOKEN_URL = "<Token URL>";
    private static final String USER_INFO_URL = "<User Info URL>";
    private static final String REDIRECT_URI = "http://localhost:8080/sso/callback";
    private static final String SCOPE = "openid profile email";

    /**
     * 获取 OIDC/OAuth2 的授权地址<br/>
     * 并由后端控制页面重定向到 OIDC/OAuth2 服务器的授权页面
     *
     * @param state 状态值
     * @return 授权地址
     */
    public String getAuthRedirectUrl(String state)
    {
        var authParamsMap = new HashMap<String, String>()
        {{
            put("response_type", "code");
            put("client_id", CLIENT_ID);
            put("redirect_uri", REDIRECT_URI);
            put("state", state);
            put("scope", SCOPE);
        }};
        var authParamsStr = HttpUtil.toParams(authParamsMap);
        return AUTH_URL + "?" + authParamsStr;
    }

    /**
     * 处理 OIDC/OAuth2 的回调，并同时获取用户信息，利用用户信息中的邮箱完成登录<br/>
     * 若一切顺利则返回到成功页面<br/>
     * 当在获取 AK 和用户信息时发生错误时，返回错误页面
     *
     * @param code 授权码
     * @return 重定向的页面路径
     */
    public String callbackHandler(String code)
    {
        // 获取 Access Token
        var tokenParamsMap = new HashMap<String, String>()
        {{
            put("code", code);
            put("client_id", CLIENT_ID);
            put("client_secret", CLIENT_SECRET);
            put("redirect_uri", REDIRECT_URI);
            put("grant_type", "authorization_code");
        }};
        var authParamsStr = HttpUtil.toParams(tokenParamsMap);
        var result = HttpUtil.post(TOKEN_URL, authParamsStr);

        var token = JSONUtil.toBean(result, TokenResponse.class);

        if (ObjectUtil.isNull(token) || StrUtil.isEmpty(token.getAccessToken()))
        {
            return URLUtil.encode( "/sso/error?err=获取 Access Token 失败"); // TODO 这里要确认，是否需要从后端控制跳转到错误页面，全部由后端控制可能会更简单一些
        }

        // 获取用户信息
        var userInfoStr = HttpUtil.createGet(USER_INFO_URL).bearerAuth(token.getAccessToken()).execute().body();
        var userInfo = JSONUtil.toBean(userInfoStr, UserInfoResponse.class);

        if (ObjectUtil.isNull(userInfo) || StrUtil.isEmpty(userInfo.getEmail()))
        {
            return URLUtil.encode("/sso/error?err=获取用户信息失败"); // TODO 同上一个错误处理
        }

        // 调用 Sa Token 的登录方法
        // TODO 这里要处理一下，如果没有对应用户，则创建用户
        StpUtil.login(userInfo.getEmail(), new SaLoginModel().setToken(token.getAccessToken()));

        return "/sso/success";
    }
}
