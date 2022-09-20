package im.zhaojun.zfile.module.storage.controller.callback;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author zhaojun
 */
@Api(tags = "Google Drive 认证回调模块")
@Controller
@Slf4j
@RequestMapping(value = {"/gd"})
public class GoogleDriveCallbackController {
	
	@Value("${zfile.gd.clientId}")
	private String clientId;
	
	@Value("${zfile.gd.redirectUri}")
	private String redirectUri;
	
	@Value("${zfile.gd.clientSecret}")
	private String clientSecret;
	
	@Value("${zfile.gd.scope}")
	private String scope;
	
	@GetMapping("/authorize")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "生成 OAuth2 登陆 URL", notes = "生成 OneDrive OAuth2 登陆 URL，用户国际版，家庭版等非世纪互联运营的 OneDrive.")
	public String authorize(String clientId, String clientSecret, String redirectUri) {
		log.info("gd 生成授权链接参数信息： clientId: {}, clientSecret: {}, redirectUri: {}", clientId, clientSecret, redirectUri);
		
		if (StrUtil.isAllEmpty(clientId, clientSecret, redirectUri)) {
			clientId = this.clientId;
			redirectUri = this.redirectUri;
			clientSecret = this.clientSecret;
		}
		
		String stateStr = "&state=" + Base64.encodeUrlSafe(StrUtil.join("::", clientId, clientSecret, redirectUri));
		
		String authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + clientId
				+ "&response_type=code&redirect_uri=" + redirectUri
				+ "&scope=" + this.scope
				+ "&access_type=offline"
				+ stateStr;
		
		log.info("gd 生成授权链接结果: {}", authorizeUrl);
		
		return "redirect:" + authorizeUrl;
	}
	
	@GetMapping("/callback")
	public String googleDriveCallback(String code, String state, Model model) {
		log.info("gd 授权回调参数信息： code: {}, state: {}", code, state);
		
		String clientId, clientSecret, redirectUri;
		
		if (StrUtil.isEmpty(state)) {
			clientId = this.clientId;
			clientSecret = this.clientSecret;
			redirectUri = this.redirectUri;
		} else {
			String stateDecode = Base64.decodeStr(state);
			String[] stateArr = stateDecode.split("::");
			clientId = stateArr[0];
			clientSecret = stateArr[1];
			redirectUri = stateArr[2];
		}
		
		final String uri = "https://accounts.google.com/o/oauth2/token";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String clientCredentials = Base64.encodeUrlSafe(clientId + ":" + clientSecret);
		headers.add("Authorization", "Basic " + clientCredentials);
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("code", code);
		requestBody.add("grant_type", "authorization_code");
		requestBody.add("redirect_uri", redirectUri);
		requestBody.add("scope", scope);
		
		HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<>(requestBody, headers);
		
		NoRedirectClientHttpRequestFactory noRedirectClientHttpRequestFactory = new NoRedirectClientHttpRequestFactory();
		
		ResponseEntity<String> response = new RestTemplate(noRedirectClientHttpRequestFactory).exchange(uri, HttpMethod.POST, formEntity, String.class);
		
		String body = response.getBody();
		log.info("{} 根据授权回调 code 获取令牌结果：body: {}", this, body);
		
		OAuth2TokenDTO oAuth2TokenDTO;
		if (response.getStatusCode() != HttpStatus.OK) {
			oAuth2TokenDTO = OAuth2TokenDTO.fail(clientId, clientSecret, redirectUri, body);
		} else {
			JSONObject jsonBody = JSONObject.parseObject(body);
			String accessToken = jsonBody.getString("access_token");
			String refreshToken = jsonBody.getString("refresh_token");
			oAuth2TokenDTO =
					OAuth2TokenDTO.success(clientId, clientSecret, redirectUri, accessToken, refreshToken, body);
		}
		
		model.addAttribute("oauth2Token", oAuth2TokenDTO);
		model.addAttribute("type", "Google Drive");
		return "callback";
	}
	
	static class NoRedirectClientHttpRequestFactory extends
			SimpleClientHttpRequestFactory {
		
		@Override
		protected void prepareConnection(HttpURLConnection connection,
										 String httpMethod) throws IOException {
			super.prepareConnection(connection, httpMethod);
			connection.setInstanceFollowRedirects(true);
		}
	}
	
}