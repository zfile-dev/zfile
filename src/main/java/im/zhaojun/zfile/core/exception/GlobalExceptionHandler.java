package im.zhaojun.zfile.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import im.zhaojun.zfile.core.controller.FrontIndexController;
import im.zhaojun.zfile.core.exception.biz.*;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.ErrorPageBizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.exception.status.*;
import im.zhaojun.zfile.core.exception.system.UploadFileFailSystemException;
import im.zhaojun.zfile.core.exception.system.ZFileAuthorizationSystemException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.sqlite.SQLiteException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 全局异常处理
 *
 * @author zhaojun
 */
@ControllerAdvice
@Slf4j
@Order(1)
public class GlobalExceptionHandler {

    private static final ThreadLocal<String> exceptionMessage = new ThreadLocal<>();

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private FrontIndexController frontIndexController;


    private static final int MAX_FIND_CAUSE_EXCEPTION_DEPTH = 10;

    // ---------------------- status exception start ----------------------

    @ExceptionHandler(value = UnauthorizedAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AjaxJson<?> unauthorizedAccessException() {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getUnauthorizedResult();
        }
        try {
            String unauthorizedUrl = systemConfigService.getUnauthorizedUrl();
            RequestHolder.getResponse().sendRedirect(unauthorizedUrl);
        } catch (IOException ex) {
            return AjaxJson.getUnauthorizedResult();
        }

        return null;
    }

    @ExceptionHandler(value = {
            NotRoleException.class
    })
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AjaxJson<?> forbiddenAccessException() {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getForbiddenResult();
        }
        try {
            String forbiddenUrl = systemConfigService.getForbiddenUrl();
            RequestHolder.getResponse().sendRedirect(forbiddenUrl);
        } catch (IOException ex) {
            return AjaxJson.getForbiddenResult();
        }

        return null;
    }

    @ExceptionHandler(value = ForbiddenAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public AjaxJson<?> forbiddenAccessException(ForbiddenAccessException e) {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }
        try {
            String forbiddenUrl = systemConfigService.getForbiddenUrl(e.getCode(), e.getMessage());
            RequestHolder.getResponse().sendRedirect(forbiddenUrl);
        } catch (IOException ex) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }

        return null;
    }

    @ExceptionHandler(value = NotFoundAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AjaxJson<?> notFoundAccessException(NotFoundAccessException e) {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }
        try {
            String notFoundUrl = systemConfigService.getNotFoundUrl(e.getCode(), e.getMessage());
            RequestHolder.getResponse().sendRedirect(notFoundUrl);
        } catch (IOException ex) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }

        return null;
    }


    /**
     * 所有未找到的页面都跳转到首页, 用户解决 vue history 直接访问 404 的问题
     * 同时, 读取 index.html 文件, 修改 title 和 favicon 后返回.
     *
     * @return  转发到 /index.html
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    @ResponseBody
    public String notFoundAccessException() {
        return frontIndexController.redirect().getBody();
    }

    @ExceptionHandler(value = MethodNotAllowedAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public AjaxJson<String> methodNotAllowedAccessException(MethodNotAllowedAccessException e) {
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = BadRequestAccessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxJson<String> badRequestAccessException(BadRequestAccessException e) {
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    // ---------------------- status exception end ----------------------




    // ---------------------- biz exception start ----------------------

    @ExceptionHandler(value = APIHttpRequestBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> apiHttpRequestBizException(APIHttpRequestBizException e) {
        log.warn("请求第三方 API 异常, 请求地址: {}, 响应码: {}, 响应体: {}", e.getUrl(), e.getResponseCode(), e.getResponseBody());
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = FilePathSecurityBizException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxJson<String> filePathSecurityBizException(FilePathSecurityBizException e) {
        log.warn("获取文件路径存在安全风险, 文件路径: {}", e.getPath());
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = GetPreviewTextContentBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> getPreviewTextContentBizException(GetPreviewTextContentBizException e) {
        log.warn("获取预览文件内容失败, 文件 url: {}", e.getUrl(), e);
        return new AjaxJson<>(e.getCode(), "预览文件内容失败, 请联系管理员.");
    }

    @ExceptionHandler(value = InitializeStorageSourceBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> initializeStorageSourceBizException(InitializeStorageSourceBizException e) {
        log.error("存储源初始化失败, 存储源 ID: {}.", e.getStorageId(), e);
        return new AjaxJson<>(e.getCode(), "存储源初始化失败：" + e.getMessage());
    }

    @ExceptionHandler(value = StorageSourceFileForbiddenAccessBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> storageSourceFileForbiddenAccessBizException(StorageSourceFileForbiddenAccessBizException e) {
        log.warn("尝试访问不被授权的文件/目录, 存储源 ID: {}: 目录: {}", e.getStorageId(), e.getPath());
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = StorageSourceIllegalOperationBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> storageSourceIllegalOperationBizException(StorageSourceIllegalOperationBizException e) {
        log.warn("存储源非法或未授权的操作, 存储源 ID: {}, 操作类型: {}", e.getStorageId(), e.getAction());
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = CorsBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> corsBizException(CorsBizException e) {
        log.warn("跨域异常:", e);
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = ErrorPageBizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<?> errorPageBizException(ErrorPageBizException e) {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }
        try {
            String errorPageUrl = systemConfigService.getErrorPageUrl(e.getCode(), e.getMessage());
            RequestHolder.getResponse().sendRedirect(errorPageUrl);
        } catch (IOException ex) {
            return AjaxJson.getError(e.getCode(), e.getMessage());
        }

        return null;
    }


    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> bizException(BizException e) {
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    // ---------------------- biz exception end ----------------------


    // ---------------------- system exception end ----------------------

    @ExceptionHandler(value = UploadFileFailSystemException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<String> uploadFileFailSystemException(UploadFileFailSystemException e) {
        log.warn("上传文件失败, 存储类型: {}, 上传路径: {}, 输入流可用字节数: {}, 响应码: {}, 响应体: {}",
                e.getStorageTypeEnum(), e.getUploadPath(), e.getInputStreamAvailable(), e.getResponseCode(), e.getResponseBody());
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = ZFileAuthorizationSystemException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<?> zfileAuthorizationSystemException(ZFileAuthorizationSystemException e) {
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    @ResponseBody
    @ResponseStatus
    public AjaxJson<?> systemException(SystemException e) {
        return new AjaxJson<>(e.getCode(), e.getMessage());
    }



    // ---------------------- system exception end ----------------------



    // ---------------------- common exception end ----------------------

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public AjaxJson<Map<String, String>> handleValidException(Exception e) {
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }
        Map<String, String> errorMap = new HashMap<>(16);

        Optional.ofNullable(bindingResult)
                .map(BindingResult::getFieldErrors)
                .ifPresent(fieldErrors -> {
                    for (FieldError fieldError : fieldErrors) {
                        errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                });
        return new AjaxJson<>(ErrorCode.BIZ_BAD_REQUEST.getCode(), ErrorCode.BIZ_BAD_REQUEST.getMessage(), errorMap);

    }

    @ExceptionHandler({FileNotFoundException.class})
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AjaxJson<Void> fileNotFound() {
        return AjaxJson.getError("文件不存在");
    }


    /**
     * 登录异常拦截器
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public AjaxJson<?> handlerNotLoginException(NotLoginException e) {
        if (RequestHolder.isAxiosRequest()) {
            return AjaxJson.getUnauthorizedResult();
        }
        try {
            String domain = systemConfigService.getRealFrontDomain();
            if (StringUtils.isBlank(domain)) {
                domain = "";
            }
            String loginUrl = StringUtils.concat(domain, "/login");
            RequestHolder.getResponse().sendRedirect(loginUrl);
        } catch (IOException ex) {
            return AjaxJson.getUnauthorizedResult();
        }

        return null;
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus
    public AjaxJson<?> extraExceptionHandler(Exception e) {
        ExceptionType exceptionType = getExceptionType(e);
        if (exceptionType == ExceptionType.IGNORE_PRINT_STACK_TRACE_EXCEPTION) {
            log.warn(e.getMessage());
        } else if (exceptionType == ExceptionType.OTHER) {
            log.error(e.getMessage(), e);
        } else if (exceptionType == ExceptionType.SPECIFY_MESSAGE_EXCEPTION) {
            if (exceptionMessage.get() != null) {
                String message = exceptionMessage.get();
                log.error("发生异常: {}", message,e );
                exceptionMessage.remove();
                return AjaxJson.getError(message);
            }
        } else if (exceptionType == ExceptionType.IGNORE_EXCEPTION) {
            // 忽略异常
            return null;
        }

        if (e.getClass() == Exception.class) {
            return AjaxJson.getError("系统异常, 请联系管理员");
        } else {
            return AjaxJson.getError(e.getMessage());
        }
    }


    private static ExceptionType getExceptionType(Exception e) {
        int findCauseCount = 0;
        do {
            if (e instanceof BizException) {
                return ExceptionType.IGNORE_PRINT_STACK_TRACE_EXCEPTION;
            } else if (e instanceof ClientAbortException) {
                return ExceptionType.IGNORE_EXCEPTION;
            } else if (e instanceof SQLiteException && e.getMessage().contains("database is locked")) {
                exceptionMessage.set("数据库繁忙，请稍后再试");
                return ExceptionType.SPECIFY_MESSAGE_EXCEPTION;
            }
            e = (Exception) e.getCause();
            findCauseCount++;
        } while (e != null && findCauseCount < MAX_FIND_CAUSE_EXCEPTION_DEPTH);

        return ExceptionType.OTHER;
    }

    enum ExceptionType {
        /**
         * 忽略打印异常信息和堆栈信息
         */
        IGNORE_EXCEPTION,

        /**
         * 仅打印异常信息, 不打印堆栈信息
         */
        IGNORE_PRINT_STACK_TRACE_EXCEPTION,

        /**
         * 不打印堆栈信息，但指定异常信息
         */
        SPECIFY_MESSAGE_EXCEPTION,

        /**
         * 其他异常, 打印异常信息和堆栈信息
         */
        OTHER;
    }
}