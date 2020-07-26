package im.zhaojun.zfile.controller.admin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ZipUtil;
import im.zhaojun.zfile.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;

/**
 * 日志相关 Controller
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class LogController {

    /**
     * 系统日志下载
     */
    @GetMapping("/log")
    public ResponseEntity<Object> downloadLog(HttpServletResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("下载诊断日志");
        }
        String userHome = System.getProperty("user.home");
        File fileZip = ZipUtil.zip(userHome + "/.zfile/logs");
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        return FileUtil.export(fileZip, "ZFile 诊断日志 - " + currentDate + ".zip");
    }

}