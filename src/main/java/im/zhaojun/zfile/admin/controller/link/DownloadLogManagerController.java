package im.zhaojun.zfile.admin.controller.link;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.admin.convert.DownloadLogConvert;
import im.zhaojun.zfile.admin.model.entity.DownloadLog;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.request.link.BatchDeleteRequest;
import im.zhaojun.zfile.admin.model.request.link.QueryDownloadLogRequest;
import im.zhaojun.zfile.admin.model.result.link.DownloadLogResult;
import im.zhaojun.zfile.admin.service.DownloadLogService;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.common.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 直链下载日志接口
 *
 * @author zhaojun
 */
@Api(tags = "直链日志管理")
@ApiSort(7)
@Controller
@RequestMapping("/admin/download/log")
public class DownloadLogManagerController {

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private DownloadLogConvert downloadLogConvert;

    @Resource
    private DownloadLogService downloadLogService;

    @ApiOperationSupport(order = 1)
    @GetMapping("/list")
    @ApiOperation(value = "直链下载日志")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "key", value = "直链 key"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "分页页数"),
            @ApiImplicitParam(paramType = "query", name = "limit", value = "每页条数"),
            @ApiImplicitParam(paramType = "query", name = "orderBy", defaultValue = "createDate", value = "排序字段"),
            @ApiImplicitParam(paramType = "query", name = "orderDirection", defaultValue = "desc", value = "排序顺序")
    })
    public AjaxJson<?> list(QueryDownloadLogRequest queryDownloadLogRequest,
                            @RequestParam(required = false, defaultValue = "create_time") String orderBy,
                            @RequestParam(required = false, defaultValue = "desc") String orderDirection) {
        Page<DownloadLog> pages = Page.of(queryDownloadLogRequest.getPage(), queryDownloadLogRequest.getLimit());
        boolean asc = Objects.equals(orderDirection, "asc");
        pages.addOrder(new OrderItem(orderBy, asc));

        DownloadLog downloadLog = new DownloadLog();
        QueryWrapper<DownloadLog> queryWrapper = new QueryWrapper<>(downloadLog);

        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getStorageKey())) {
            queryWrapper.eq("storage_key", queryDownloadLogRequest.getStorageKey());
        }
        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getPath())) {
            queryWrapper.like("path", queryDownloadLogRequest.getPath());
        }
        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getShortKey())) {
            queryWrapper.like("short_key", queryDownloadLogRequest.getShortKey());
        }
        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getIp())) {
            queryWrapper.like("ip", queryDownloadLogRequest.getIp());
        }
        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getReferer())) {
            queryWrapper.like("referer", queryDownloadLogRequest.getReferer());
        }
        if (StrUtil.isNotEmpty(queryDownloadLogRequest.getUserAgent())) {
            queryWrapper.like("user_agent", queryDownloadLogRequest.getUserAgent());
        }
        if (ObjectUtil.isNotEmpty(queryDownloadLogRequest.getDateFrom())) {
            queryWrapper.ge("create_time", queryDownloadLogRequest.getDateFrom());
        }
        if (ObjectUtil.isNotEmpty(queryDownloadLogRequest.getDateTo())) {
            queryWrapper.le("create_time", queryDownloadLogRequest.getDateFrom());
        }
        Page<DownloadLog> selectResult = downloadLogService.page(pages, queryWrapper);

        long total = selectResult.getTotal();
        List<DownloadLog> records = selectResult.getRecords();

        Map<String, StorageSource> cache = new HashMap<>();

        Stream<DownloadLogResult> shortLinkResultList = records.stream().map(model -> {
            String storageKey = model.getStorageKey();
            StorageSource storageSource;
            if (cache.containsKey(storageKey)) {
                storageSource = cache.get(storageKey);
            } else {
                storageSource = storageSourceService.findByStorageKey(storageKey);
                cache.put(storageKey, storageSource);
            }
            return downloadLogConvert.entityToResultList(model, storageSource);
        });
        return AjaxJson.getPageData(total, shortLinkResultList);
    }


    @ApiOperationSupport(order = 2)
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除直链")
    @ApiImplicitParam(paramType = "path", name = "id", value = "直链 id", required = true)
    @ResponseBody
    public AjaxJson<Void> deleteById(@PathVariable Integer id) {
        downloadLogService.removeById(id);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @PostMapping("/delete/batch")
    @ResponseBody
    @ApiImplicitParam(paramType = "query", name = "ids", value = "直链 id", required = true)
    @ApiOperation(value = "批量删除直链")
    public AjaxJson<Void> batchDelete(@RequestBody BatchDeleteRequest batchDeleteRequest) {
        List<Integer> ids = batchDeleteRequest.getIds();
        downloadLogService.removeBatchByIds(ids);
        return AjaxJson.getSuccess();
    }


}