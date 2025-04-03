package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.core.BizException;
import lombok.Getter;

/**
 * @author zhaojun
 */
@Getter
public class CorsBizException extends BizException {

    public CorsBizException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean printExceptionStackTrace() {
        return true;
    }

}