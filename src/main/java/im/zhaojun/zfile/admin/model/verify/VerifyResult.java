package im.zhaojun.zfile.admin.model.verify;

import lombok.Data;

/**
 * 用于表示校验结果的类
 *
 * @author zhaojun
 */
@Data
public class VerifyResult {

    /**
     * 是否成功
     */
    private boolean passed;

    /**
     * 消息
     */
    private String msg;

    /**
     * 代码
     */
    private Integer code;

    /**
     * 表达式
     */
    private String pattern;

    public static VerifyResult success() {
        VerifyResult verifyResult = new VerifyResult();
        verifyResult.setPassed(true);
        return verifyResult;
    }


    public static VerifyResult success(String pattern) {
        VerifyResult verifyResult = new VerifyResult();
        verifyResult.setPassed(true);
        verifyResult.setPattern(pattern);
        return verifyResult;
    }


    public static VerifyResult fail(String msg, Integer code) {
        VerifyResult verifyResult = new VerifyResult();
        verifyResult.setPassed(false);
        verifyResult.setMsg(msg);
        verifyResult.setCode(code);
        return verifyResult;
    }

}