package im.zhaojun.zfile.common.controller.callback;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.admin.model.dto.OneDriveToken;
import im.zhaojun.zfile.home.service.impl.OneDriveChinaServiceImpl;
import im.zhaojun.zfile.home.service.impl.OneDriveServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * OneDrive 授权回调
 *
 * @author zhaojun
 */
@Api(tags = "OneDrive 认证回调模块")
@Controller
@RequestMapping(value = {"/onedrive", "/onedirve"})
public class OneDriveCallbackController {

    @Resource
    private OneDriveServiceImpl oneDriveServiceImpl;

    @Resource
    private OneDriveChinaServiceImpl oneDriveChinaServiceImpl;


    @GetMapping("/authorize")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "生成 OAuth2 登陆 URL", notes = "生成 OneDrive OAuth2 登陆 URL，用户国际版，家庭版等非世纪互联运营的 OneDrive.")
    public String authorize(String clientId, String clientSecret, String redirectUri) {
        if (StrUtil.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = oneDriveServiceImpl.getClientId();
            redirectUri = oneDriveServiceImpl.getRedirectUri();
            clientSecret = oneDriveServiceImpl.getClientSecret();
        }
        
        String stateStr = "&state=" + Base64.encodeUrlSafe(StrUtil.join("::", clientId, clientSecret, redirectUri));
    
    
        String authorizeUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=" + clientId
                                                                + "&response_type=code&redirect_uri=" + redirectUri
                                                                + "&scope=" + oneDriveServiceImpl.getScope()
                                                                + stateStr;
        return "redirect:" + authorizeUrl;
    }


    @GetMapping("/callback")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "OAuth2 回调地址", notes = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveCallback(String code, String state, Model model) {
        String stateDecode = Base64.decodeStr(state);
        String[] stateArr = stateDecode.split("::");
        OneDriveToken oneDriveToken = oneDriveServiceImpl.getToken(code, stateArr[0], stateArr[1], stateArr[2]);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }


    @GetMapping("/china-authorize")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "生成 OAuth2 登陆 URL(世纪互联)", notes = "生成 OneDrive OAuth2 登陆 URL，用于世纪互联版本.")
    public String authorizeChina(String clientId, String clientSecret, String redirectUri) {
        if (StrUtil.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = oneDriveChinaServiceImpl.getClientId();
            redirectUri = oneDriveChinaServiceImpl.getRedirectUri();
            clientSecret = oneDriveChinaServiceImpl.getClientSecret();
        }
    
        String stateStr = "&state=" + Base64.encodeUrlSafe(StrUtil.join("::", clientId, clientSecret, redirectUri));
    
    
        String authorizeUrl = "https://login.chinacloudapi.cn/common/oauth2/v2.0/authorize?client_id=" + clientId
                + "&response_type=code&redirect_uri=" + redirectUri
                + "&scope=" + oneDriveChinaServiceImpl.getScope()
                + stateStr;
        return "redirect:" + authorizeUrl;
    }


    @GetMapping("/china-callback")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "OAuth2 回调地址(世纪互联)", notes = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveChinaCallback(String code, String state, Model model) {
        String stateDecode = Base64.decodeStr(state);
        String[] stateArr = stateDecode.split("::");
        OneDriveToken oneDriveToken = oneDriveChinaServiceImpl.getToken(code, stateArr[0], stateArr[1], stateArr[2]);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }

}