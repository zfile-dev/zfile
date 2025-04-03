package im.zhaojun.zfile.core.exception.core;

import im.zhaojun.zfile.core.exception.ErrorCode;
import lombok.Getter;

/**
 * 业务异常，该类异常用户可自行处理，无需记录日志，属于正常业务流程中的异常. 如: 用户名密码错误, 未登录等.
 *
 * @author zhaojun
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 8312907182931723379L;

    /**
     * 错误码
     */
    private String code;

    /**
     * 是否打印堆栈信息，业务异常默认不打印堆栈信息，如果需要打印堆栈信息，可以通过子类覆盖该方法修改返回值为 true.
     */
    public boolean printExceptionStackTrace() {
        return false;
    }

    /**
     * 构造一个没有错误信息的 <code>SystemException</code>
     */
    public BizException() {
        super();
    }

    /**
     * 使用指定的 Throwable 和 Throwable.toString() 作为异常信息来构造 SystemException
     *
     * @param cause 错误原因， 通过 Throwable.getCause() 方法可以获取传入的 cause信息
     */
    public BizException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用错误信息 message 构造 SystemException
     *
     * @param message 错误信息
     */
    public BizException(String message) {
        super(message);
    }

    /**
     * 使用错误码和错误信息构造 SystemException
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用错误信息和 Throwable 构造 SystemException
     *
     * @param message 错误信息
     * @param cause   错误原因
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param code    错误码
     * @param message 错误信息
     * @param cause   错误原因
     */
    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @param errorCode ErrorCode
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * @param errorCode ErrorCode
     * @param cause     错误原因
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

}