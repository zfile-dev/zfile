package im.zhaojun.zfile.module.storage.controller.callback;

import cn.hutool.core.codec.Base64;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import im.zhaojun.zfile.module.storage.oauth2.service.IOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhaojun
 */
@Tag(name = "Google Drive 认证回调模块")
@Controller
@Slf4j
@RequestMapping(value = {"/gd"})
public class GoogleDriveCallbackController {
	
	@Resource
	private IOAuth2Service googleDriveOAuth2ServiceImpl;
	
	@GetMapping("/authorize")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "生成 OAuth2 登陆 URL", description = "生成 OneDrive OAuth2 登陆 URL，用户国际版，家庭版等非世纪互联运营的 OneDrive.")
	public String authorize(String clientId, String clientSecret, String redirectUri) {
		String authorizeUrl = googleDriveOAuth2ServiceImpl.generateAuthorizationUrl(clientId, clientSecret, redirectUri);
		return "redirect:" + authorizeUrl;
	}
	
	@GetMapping("/callback")
	public String googleDriveCallback(String code, String state, Model model) {
		if (log.isDebugEnabled()) {
			log.debug("Google Drive 授权回调参数信息： code: {}, state: {}", code, state);
		}

		String clientId = null, clientSecret = null, redirectUri = null;
		if (StringUtils.isNotEmpty(state)) {
			String stateDecode = Base64.decodeStr(state);
			String[] stateArr = stateDecode.split("::");
			clientId = stateArr[0];
			clientSecret = stateArr[1];
			redirectUri = stateArr[2];
		}

		OAuth2TokenDTO oAuth2TokenDTO = googleDriveOAuth2ServiceImpl.getTokenByCode(code, clientId, clientSecret, redirectUri);
		model.addAttribute("oauth2Token", oAuth2TokenDTO);
		model.addAttribute("type", "Google Drive");
		return "callback";
	}
}