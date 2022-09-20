package im.zhaojun.zfile.module.password.model.dto;

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


    public static VerifyResultDTO fail(String msg, Integer code) {
        VerifyResultDTO verifyResultDTO = new VerifyResultDTO();
        verifyResultDTO.setPassed(false);
        verifyResultDTO.setMsg(msg);
        verifyResultDTO.setCode(code);
        return verifyResultDTO;
    }

}