package im.zhaojun.zfile.module.storage.controller.file;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.HttpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件解析接口
 *
 * @author zhaojun
 */
@Api(tags = "文件解析模块")
@ApiSort(4)
@RestController
@RequestMapping("/api/parse")
public class FileParseController {

    @GetMapping("/content")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取文本内容", notes = "获取文本文件的文件内容，一般用于 txt, md, ini 等普通文本文件")
    @ApiImplicitParam(paramType = "query", name = "url", value = "文本文件下载地址", required = true, dataTypeClass = String.class)
    public AjaxJson<String> getContent(String url) {
        return AjaxJson.getSuccessData(HttpUtil.getTextContent(url));
    }

}