package im.zhaojun.zfile.admin.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.home.model.dto.CacheInfoDTO;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.common.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 存储源缓存维护接口
 *
 * @author zhaojun
 */
@RestController
@Api(tags = "存储源模块-缓存")
@ApiSort(5)
@RequestMapping("/admin/cache")
public class CacheController {

    @Resource
    private StorageSourceService storageSourceService;

    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "启用存储源缓存", notes = "开启缓存后，N 秒内重复请求相同文件夹，不会重复调用 API。" +
            "参数 N 在配置文件中设置 {zfile.cache.timeout}，默认为 1800 秒。")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/{storageId}/enable")
    public AjaxJson<Void> enableCache(@PathVariable("storageId") Integer storageId) {
        storageSourceService.updateCacheStatus(storageId, true);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "禁用存储源缓存")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/{storageId}/disable")
    public AjaxJson<Void> disableCache(@PathVariable("storageId") Integer storageId) {
        storageSourceService.updateCacheStatus(storageId, false);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "查看存储源缓存", notes = "可查看存储源缓存的所有目录路径")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @GetMapping("/{storageId}/info")
    public AjaxJson<CacheInfoDTO> cacheInfo(@PathVariable("storageId") Integer storageId) {
        CacheInfoDTO cacheInfo = storageSourceService.findCacheInfo(storageId);
        return AjaxJson.getSuccessData(cacheInfo);
    }


    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "刷新存储源缓存", notes = "刷新存储源缓存路径，系统会重新预热此路径的内容")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true),
            @ApiImplicitParam(paramType = "body", name = "key", value = "缓存 key", required = true)
    })
    @PostMapping("/{storageId}/refresh")
    public AjaxJson<Void> refreshCache(@PathVariable("storageId") Integer storageId, String key) throws Exception {
        storageSourceService.refreshCache(storageId, key);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "开启缓存自动刷新", notes = "开启后每隔 N 秒检测到期的缓存, 对于过期缓存尝试调用 API, 重新写入缓存." +
            "参数 N 在配置文件中设置 {zfile.cache.auto-refresh-interval}，默认为 5 秒。")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/{storageId}/auto-refresh/start")
    public AjaxJson<Void> enableAutoRefresh(@PathVariable("storageId") Integer storageId) {
        storageSourceService.startAutoCacheRefresh(storageId);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "关闭缓存自动刷新")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/{storageId}/auto-refresh/stop")
    public AjaxJson<Void> disableAutoRefresh(@PathVariable("storageId") Integer storageId) {
        storageSourceService.stopAutoCacheRefresh(storageId);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "清空缓存")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/{storageId}/clear")
    public AjaxJson<Void> clearCache(@PathVariable("storageId") Integer storageId) {
        storageSourceService.clearCache(storageId);
        return AjaxJson.getSuccess();
    }

}