package im.zhaojun.common.exception;

import im.zhaojun.common.model.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 */
@ControllerAdvice
@ResponseBody
public class GlobleExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobleExceptionHandler.class);

    @ExceptionHandler(SearchDisableException.class)
    @ResponseStatus(code= HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultBean searchDisableExceptionHandler(SearchDisableException e) {
        if (log.isDebugEnabled()) {
            log.debug(e.getMessage(), e);
        }
        return ResultBean.error(e.getMessage());
    }

}
