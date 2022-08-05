package im.zhaojun.zfile.home.controller;

import cn.hutool.core.util.BooleanUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import im.zhaojun.zfile.admin.model.entity.ShortLink;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.service.ShortLinkService;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.admin.service.SystemConfigService;
import im.zhaojun.zfile.common.exception.IllegalDownloadLinkException;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 短链接口
 *
 * @author zhaojun
 */
@Api(tags = "直短链模块")
@ApiSort(5)
@Controller
public class ShortLinkController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private StorageSourceService storageSourceService;


    @GetMapping("/api/short-link")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "生成短链", notes = "对指定存储源的某文件路径生成短链")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "storageKey", value = "存储源 id", required = true),
            @ApiImplicitParam(paramType = "query", name = "path", value = "文件路径", required = true)
    })
    @DynamicResponseParameters(name = "AjaxJson",properties = {
            @DynamicParameter(name = "msg", value = "响应消息", example = "ok"),
            @DynamicParameter(name = "code", value = "业务状态码，0 为正常，其他值均为异常，异常情况下见响应消息", example = "0"),
            @DynamicParameter(name = "data", value = "短链地址", example = "https://zfile.vip/s/btz4tu")
    })
    public AjaxJson<String> generatorShortLink(String storageKey, String path) {
        // 获取站点域名
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

        // 是否允许使用短链和短链，如果都不允许，则提示禁止生成.
        Boolean showShortLink = systemConfig.getShowShortLink();
        Boolean showPathLink = systemConfig.getShowPathLink();
        if ( BooleanUtil.isFalse(showShortLink) && BooleanUtil.isFalse(showPathLink)) {
            throw new IllegalDownloadLinkException("当前系统不允许使用直链和短链.");
        }

        String domain = systemConfig.getDomain();

        // 拼接直链地址.
        String fullPath = StringUtils.concat(path);
        ShortLink shortLink = shortLinkService.findByStorageKeyAndUrl(storageKey, fullPath);
        // 如果没有短链，则生成短链
        if (shortLink == null) {
            Integer storageId = storageSourceService.findIdByKey(storageKey);
            shortLink = shortLinkService.generatorShortLink(storageId, fullPath);
        }

        String shortUrl = StringUtils.removeDuplicateSlashes(domain + "/s/" + shortLink.getShortKey());
        return AjaxJson.getSuccessData(shortUrl);
    }


    @GetMapping("/s/{key}")
    @ResponseStatus(HttpStatus.FOUND)
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "跳转短链", notes = "根据短链 key 跳转（302 重定向）到对应的直链.")
    @ApiImplicitParam(paramType = "path", name = "key", value = "短链 key", required = true)
    public String parseShortKey(@PathVariable String key) throws IOException {
        ShortLink shortLink = shortLinkService.findByKey(key);
        if (shortLink == null) {
            throw new RuntimeException("此直链不存在或已失效.");
        }

        // 获取站点域名
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

        // 判断是否允许生成短链.
        Boolean showShortLink = systemConfig.getShowShortLink();
        if ( BooleanUtil.isFalse(showShortLink)) {
            throw new IllegalDownloadLinkException("当前系统不允许使用短链.");
        }

        Integer storageId = shortLink.getStorageId();
        StorageSource storageSource = storageSourceService.findById(storageId);
        String storageKey = storageSource.getKey();
        String filePath = shortLink.getUrl();

        shortLinkService.handlerDownload(storageKey, filePath, shortLink.getShortKey());
        return null;
    }

}