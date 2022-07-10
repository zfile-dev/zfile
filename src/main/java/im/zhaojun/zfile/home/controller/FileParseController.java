package im.zhaojun.zfile.home.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.common.util.HttpUtil;
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
    @ApiImplicitParam(paramType = "query", name = "url", value = "文本文件下载地址", required = true)
    @DynamicResponseParameters(name = "AjaxJson",properties = {
            @DynamicParameter(name = "msg", value = "响应消息", example = "ok"),
            @DynamicParameter(name = "code", value = "业务状态码，0 为正常，其他值均为异常，异常情况下见响应消息", example = "0"),
            @DynamicParameter(name = "data", value = "文本内容", example = "这是一段 txt 中的文字")
    })
    public AjaxJson<String> getContent(String url) {
        return AjaxJson.getSuccessData(HttpUtil.getTextContent(url));
    }

}