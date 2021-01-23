package im.zhaojun.zfile.model.support;

import lombok.Data;

/**
 * 用于表示校验结果的类
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

    public static VerifyResult success() {
        VerifyResult verifyResult = new VerifyResult();
        verifyResult.setPassed(true);
        return verifyResult;
    }

    public static VerifyResult fail(String msg) {
        VerifyResult verifyResult = new VerifyResult();
        verifyResult.setPassed(false);
        verifyResult.setMsg(msg);
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