package im.zhaojun.zfile.module.storage.controller.callback;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import im.zhaojun.zfile.module.storage.service.impl.OneDriveChinaServiceImpl;
import im.zhaojun.zfile.module.storage.service.impl.OneDriveServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("onedrive 国际版生成授权链接参数信息： clientId: {}, clientSecret: {}, redirectUri: {}", clientId, clientSecret, redirectUri);
        
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
        
        log.info("onedrive 国际版生成授权链接结果: {}", authorizeUrl);
        
        return "redirect:" + authorizeUrl;
    }
    
    
    @GetMapping("/callback")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "OAuth2 回调地址", notes = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveCallback(String code, String state, Model model) {
        log.info("onedrive 国际版授权回调参数信息： code: {}, state: {}", code, state);
    
        String clientId, clientSecret, redirectUri;
        
        if (StrUtil.isEmpty(state)) {
            clientId = oneDriveServiceImpl.getClientId();
            clientSecret = oneDriveServiceImpl.getClientSecret();
            redirectUri = oneDriveServiceImpl.getRedirectUri();
        } else {
            String stateDecode = Base64.decodeStr(state);
            String[] stateArr = stateDecode.split("::");
            clientId = stateArr[0];
            clientSecret = stateArr[1];
            redirectUri = stateArr[2];
        }
        
        OAuth2TokenDTO Oauth2TokenDTO = oneDriveServiceImpl.getToken(code, clientId, clientSecret, redirectUri);
        log.info("onedrive 国际版授权回调获取令牌结果: {}", Oauth2TokenDTO);
        
        model.addAttribute("oauth2Token", Oauth2TokenDTO);
        model.addAttribute("type", "OneDrive 国际版");
        return "callback";
    }
    
    
    @GetMapping("/china-authorize")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "生成 OAuth2 登陆 URL(世纪互联)", notes = "生成 OneDrive OAuth2 登陆 URL，用于世纪互联版本.")
    public String authorizeChina(String clientId, String clientSecret, String redirectUri) {
        log.info("onedrive 世纪互联版生成授权链接参数信息： clientId: {}, clientSecret: {}, redirectUri: {}", clientId, clientSecret, redirectUri);
        
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
        
        log.info("onedrive 世纪互联版生成授权链接结果: {}", authorizeUrl);
        
        return "redirect:" + authorizeUrl;
    }
    
    
    @GetMapping("/china-callback")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "OAuth2 回调地址(世纪互联)", notes = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveChinaCallback(String code, String state, Model model) {
        log.info("onedrive 世纪互联版授权回调参数信息： code: {}, state: {}", code, state);
        
        String clientId, clientSecret, redirectUri;
        
        if (StrUtil.isEmpty(state)) {
            clientId = oneDriveChinaServiceImpl.getClientId();
            clientSecret = oneDriveChinaServiceImpl.getClientSecret();
            redirectUri = oneDriveChinaServiceImpl.getRedirectUri();
        } else {
            String stateDecode = Base64.decodeStr(state);
            String[] stateArr = stateDecode.split("::");
            clientId = stateArr[0];
            clientSecret = stateArr[1];
            redirectUri = stateArr[2];
        }
        
        OAuth2TokenDTO OAuth2TokenDTO = oneDriveChinaServiceImpl.getToken(code, clientId, clientSecret, redirectUri);
        log.info("onedrive 世纪互联版授权回调获取令牌结果: {}", OAuth2TokenDTO);
        
        model.addAttribute("oauth2Token", OAuth2TokenDTO);
        model.addAttribute("type", "OneDrive 世纪互联");
        return "callback";
    }
    
}