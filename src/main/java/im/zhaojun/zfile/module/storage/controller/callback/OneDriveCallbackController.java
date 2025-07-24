package im.zhaojun.zfile.module.storage.controller.callback;

import cn.hutool.core.codec.Base64;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import im.zhaojun.zfile.module.storage.oauth2.service.OneDriveChinaOAuth2ServiceImpl;
import im.zhaojun.zfile.module.storage.oauth2.service.OneDriveOAuth2ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OneDrive 授权回调
 *
 * @author zhaojun
 */
@Tag(name = "OneDrive 认证回调模块")
@Controller
@Slf4j
@RequestMapping(value = {"/onedrive", "/onedirve"})
public class OneDriveCallbackController {
    
    @Resource
    private OneDriveOAuth2ServiceImpl oneDriveOAuth2Service;

    @Resource
    private OneDriveChinaOAuth2ServiceImpl oneDriveChinaOAuth2Service;

    @GetMapping("/authorize")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "生成 OAuth2 登陆 URL", description = "生成 OneDrive OAuth2 登陆 URL，用户国际版，家庭版等非世纪互联运营的 OneDrive.")
    public String authorize(String clientId, String clientSecret, String redirectUri) {
        String authorizeUrl = oneDriveOAuth2Service.generateAuthorizationUrl(clientId, clientSecret, redirectUri);
        return "redirect:" + authorizeUrl;
    }
    
    
    @GetMapping("/callback")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "OAuth2 回调地址", description = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveCallback(String code, String state, Model model) {
        if (log.isDebugEnabled()) {
            log.debug("onedrive 国际版授权回调参数信息： code: {}, state: {}", code, state);
        }

        String clientId = null, clientSecret = null, redirectUri = null;
        if (StringUtils.isNotEmpty(state)) {
            String stateDecode = Base64.decodeStr(state);
            String[] stateArr = stateDecode.split("::");
            clientId = stateArr[0];
            clientSecret = stateArr[1];
            redirectUri = stateArr[2];
        }

        OAuth2TokenDTO oAuth2TokenDTO = oneDriveOAuth2Service.getTokenByCode(code, clientId, clientSecret, redirectUri);
        model.addAttribute("oauth2Token", oAuth2TokenDTO);
        model.addAttribute("type", "OneDrive 国际版");
        return "callback";
    }
    
    
    @GetMapping("/china-authorize")
    @ApiOperationSupport(order = 3)
    @Operation(summary = "生成 OAuth2 登陆 URL(世纪互联)", description = "生成 OneDrive OAuth2 登陆 URL，用于世纪互联版本.")
    public String authorizeChina(String clientId, String clientSecret, String redirectUri) {
        String authorizeUrl = oneDriveChinaOAuth2Service.generateAuthorizationUrl(clientId, clientSecret, redirectUri);
        return "redirect:" + authorizeUrl;
    }
    
    
    @GetMapping("/china-callback")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "OAuth2 回调地址(世纪互联)", description = "根据 OAuth2 协议，登录成功后，会返回给网站一个 code，用此 code 去换取 accessToken 和 refreshToken.（oneDrive 会回调此接口）")
    public String oneDriveChinaCallback(String code, String state, Model model) {
        if (log.isDebugEnabled()) {
            log.debug("onedrive 世纪互联授权回调参数信息： code: {}, state: {}", code, state);
        }

        String clientId = null, clientSecret = null, redirectUri = null;
        if (StringUtils.isNotEmpty(state)) {
            String stateDecode = Base64.decodeStr(state);
            String[] stateArr = stateDecode.split("::");
            clientId = stateArr[0];
            clientSecret = stateArr[1];
            redirectUri = stateArr[2];
        }

        OAuth2TokenDTO OAuth2TokenDTO = oneDriveChinaOAuth2Service.getTokenByCode(code, clientId, clientSecret, redirectUri);
        model.addAttribute("oauth2Token", OAuth2TokenDTO);
        model.addAttribute("type", "OneDrive 世纪互联");
        return "callback";
    }

}