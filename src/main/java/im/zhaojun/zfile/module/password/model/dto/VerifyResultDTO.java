package im.zhaojun.zfile.module.password.model.dto;

import im.zhaojun.zfile.core.exception.ErrorCode;
import lombok.Data;

/**
 * 用于表示校验结果的类
 *
 * @author zhaojun
 */
@Data
public class VerifyResultDTO {

    /**
     * 是否成功
     */
    private boolean passed;

    /**
     * 表达式
     */
    private String pattern;

    /**
     * 错误消息
     */
    private ErrorCode errorCode;

    public static VerifyResultDTO success() {
        VerifyResultDTO verifyResultDTO = new VerifyResultDTO();
        verifyResultDTO.setPassed(true);
        return verifyResultDTO;
    }


    public static VerifyResultDTO success(String pattern) {
        VerifyResultDTO verifyResultDTO = new VerifyResultDTO();
        verifyResultDTO.setPassed(true);
        verifyResultDTO.setPattern(pattern);
        return verifyResultDTO;
    }


    public static VerifyResultDTO fail(ErrorCode errorCode) {
        VerifyResultDTO verifyResultDTO = new VerifyResultDTO();
        verifyResultDTO.setPassed(false);
        verifyResultDTO.setErrorCode(errorCode);
        return verifyResultDTO;
    }

}