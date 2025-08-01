package im.zhaojun.zfile.module.user.service.login;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.secret.SecretGenerator;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.status.ForbiddenAccessException;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.request.VerifyLoginTwoFactorAuthenticatorRequest;
import im.zhaojun.zfile.module.user.model.result.LoginTwoFactorAuthenticatorResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 2FA 双因素认证 Service
 *
 * @author zhaojun
 */
@Service
public class TwoFactorAuthenticatorVerifyService {

	@Resource
	private SecretGenerator secretGenerator;

	@Resource
	private QrDataFactory qrDataFactory;

	@Resource
	private CodeVerifier verifier;

	@Resource
	private SystemConfigService systemConfigService;


	/**
	 * 生成 2FA 双因素认证二维码和密钥
	 *
	 * @return  2FA 双因素认证二维码和密钥
     */
	public LoginTwoFactorAuthenticatorResult setupDevice() {
		// 生成 2FA 密钥
		String secret = secretGenerator.generate();

		// 将生成的 2FA 密钥转换为 Base64 图像字符串
		User currentUser = ZFileAuthUtil.getCurrentUser();
		QrData data = qrDataFactory.newBuilder().label("ZFile:" + currentUser.getUsername()).secret(secret).issuer("ZFile").build();

		return new LoginTwoFactorAuthenticatorResult(data.getUri(), secret);
	}


	/**
	 * 验证 2FA 双因素认证是否正确，正确则进行绑定.
	 *
	 * @param   verifyLoginTwoFactorAuthenticatorRequest
	 *          2FA 双因素认证请求参数
	 */
	public void deviceVerify(VerifyLoginTwoFactorAuthenticatorRequest verifyLoginTwoFactorAuthenticatorRequest) {
		String secret = verifyLoginTwoFactorAuthenticatorRequest.getSecret();
		String code = verifyLoginTwoFactorAuthenticatorRequest.getCode();

		checkCode(secret, code);

		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
//			systemConfig.setLoginVerifyMode(LoginVerifyModeEnum.TWO_FACTOR_AUTHENTICATION_MODE);
		systemConfig.setAdminTwoFactorVerify(true);
		systemConfig.setLoginVerifySecret(secret);
		systemConfigService.updateSystemConfig(systemConfig);
	}


	/**
	 * 验证 2FA 双因素认证.
	 *
	 * @param   loginVerifySecret
	 *          2FA 双因素认证密钥
	 *
	 * @param   verifyCode
	 *          2FA 双因素认证验证码
	 *
	 * @throws 	ForbiddenAccessException 	2FA 双因素认证失败会抛出此异常
	 */
	public void checkCode(String loginVerifySecret, String verifyCode) {
		if (!verifier.isValidCode(loginVerifySecret, verifyCode)) {
			throw new ForbiddenAccessException(ErrorCode.BIZ_2FA_CODE_ERROR);
		}
	}

}