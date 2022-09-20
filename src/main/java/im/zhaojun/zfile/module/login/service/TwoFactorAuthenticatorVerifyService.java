package im.zhaojun.zfile.module.login.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import im.zhaojun.zfile.core.exception.LoginVerifyException;
import im.zhaojun.zfile.module.login.model.request.VerifyLoginTwoFactorAuthenticatorRequest;
import im.zhaojun.zfile.module.login.model.result.LoginTwoFactorAuthenticatorResult;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.login.model.enums.LoginVerifyModeEnum;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

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
	private QrGenerator qrGenerator;

	@Resource
	private CodeVerifier verifier;

	@Resource
	private SystemConfigService systemConfigService;


	/**
	 * 生成 2FA 双因素认证二维码和密钥
	 *
	 * @return  2FA 双因素认证二维码和密钥
	 * @throws  QrGenerationException   二维码生成异常
	 */
	public LoginTwoFactorAuthenticatorResult setupDevice() throws QrGenerationException {
		// 生成 2FA 密钥
		String secret = secretGenerator.generate();
		QrData data = qrDataFactory.newBuilder().secret(secret).issuer("ZFile").build();

		// 将生成的 2FA 密钥转换为 Base64 图像字符串
		String qrCodeImage = getDataUriForImage(
				qrGenerator.generate(data),
				qrGenerator.getImageMimeType());

		return new LoginTwoFactorAuthenticatorResult(qrCodeImage, secret);
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

		if (verifier.isValidCode(secret, code)) {
			SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
			systemConfig.setLoginVerifyMode(LoginVerifyModeEnum.TWO_FACTOR_AUTHENTICATION_MODE);
			systemConfig.setLoginVerifySecret(secret);
			systemConfigService.updateSystemConfig(systemConfig);
		} else {
			throw new LoginVerifyException("验证码不正确");
		}
	}


	/**
	 * 验证 2FA 双因素认证.
	 *
	 * @param   loginVerifySecret
	 *          2FA 双因素认证密钥
	 *
	 * @param   verifyCode
	 *          2FA 双因素认证验证码
	 */
	public void checkCode(String loginVerifySecret, String verifyCode) {
		if (!verifier.isValidCode(loginVerifySecret, verifyCode)) {
			throw new LoginVerifyException("验证码错误或已失效");
		}
	}

}