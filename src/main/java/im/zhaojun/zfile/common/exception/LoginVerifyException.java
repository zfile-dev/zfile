package im.zhaojun.zfile.common.exception;

/**
 * 登陆验证码验证异常
 *
 * @author zhaojun
 */
public class LoginVerifyException extends RuntimeException {

	public LoginVerifyException(String message) {
		super(message);
	}

}