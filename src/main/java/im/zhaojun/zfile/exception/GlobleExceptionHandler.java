package im.zhaojun.zfile.exception;

import im.zhaojun.zfile.model.support.ResultBean;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 * @author zhaojun
 */
@ControllerAdvice
public class GlobleExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobleExceptionHandler.class);

    /**
     * 不存在的文件异常
     */
    @ExceptionHandler({NotExistFileException.class})
    @ResponseBody
    public ResultBean notExistFile(Exception ex) {
        return ResultBean.error("文件不存在");
    }


    /**
     * 捕获 ClientAbortException 异常, 不做任何处理, 防止出现大量堆栈日志输出, 此异常不影响功能.
     */
    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class, ClientAbortException.class})
    @ResponseBody
    @ResponseStatus
    public void clientAbortException(Exception ex) {
        // if (log.isDebugEnabled()) {
        //     log.debug("出现了断开异常:", ex);
        // }
    }

    /**
     * 文件预览异常
     */
    @ExceptionHandler({PasswordVerifyException.class})
    @ResponseBody
    @ResponseStatus
    public ResultBean passwordVerifyException(PasswordVerifyException ex) {
        return ResultBean.error(ex.getMessage());
    }


    /**
     * 无效的驱动器异常
     */
    @ExceptionHandler({InvalidDriveException.class})
    @ResponseBody
    @ResponseStatus
    public ResultBean invalidDriveException(InvalidDriveException ex) {
        return ResultBean.error(ex.getMessage());
    }


    /**
     * 文件预览异常
     */
    @ExceptionHandler({PreviewException.class})
    @ResponseBody
    @ResponseStatus
    public ResultBean previewException(PreviewException ex) {
        return ResultBean.error(ex.getMessage());
    }


    /**
     * 初始化异常
     */
    @ExceptionHandler({InitializeDriveException.class})
    @ResponseBody
    @ResponseStatus
    public ResultBean initializeException(InitializeDriveException ex) {
        return ResultBean.error(ex.getMessage());
    }


    @ExceptionHandler
    @ResponseBody
    @ResponseStatus
    public ResultBean extraExceptionHandler(Exception e) {
        log.error(e.getMessage(), e);

        if (e.getClass() == Exception.class) {
            return ResultBean.error("系统异常, 请联系管理员");
        } else {
            return ResultBean.error(e.getMessage());
        }
    }

}
