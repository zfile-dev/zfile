package im.zhaojun.zfile.core.exception;


/**
 * 密码校验失败异常
 *
 * @author zhaojun
 */
public class PasswordVerifyException extends RuntimeException {

    private final Integer code;

    public PasswordVerifyException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}