package im.zhaojun.zfile.core.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ZipUtil;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.FileResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Date;

/**
 * 获取系统日志接口
 *
 * @author zhaojun
 */
@Api(tags = "日志")
@ApiSort(8)
@Slf4j
@RestController
@RequestMapping("/admin")
public class LogController {

    @Value("${zfile.log.path}")
    private String zfileLogPath;

    @GetMapping("/log/download")
    @ApiOperation(value = "下载系统日志")
    public ResponseEntity<Resource> downloadLog() {
        if (log.isDebugEnabled()) {
            log.debug("下载诊断日志");
        }

        File fileZip = ZipUtil.zip(zfileLogPath);
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        return FileResponseUtil.exportSingleThread(fileZip, "ZFile 诊断日志 - " + currentDate + ".zip");
    }

}