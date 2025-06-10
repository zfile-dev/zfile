package im.zhaojun.zfile.module.log.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.model.request.BatchDeleteRequest;
import im.zhaojun.zfile.module.link.model.request.QueryDownloadLogRequest;
import im.zhaojun.zfile.module.log.convert.DownloadLogConvert;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import im.zhaojun.zfile.module.log.model.result.DownloadLogResult;
import im.zhaojun.zfile.module.log.service.DownloadLogService;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "直链日志管理")
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

    @Resource
    private SystemConfigService systemConfigService;

    @ApiOperationSupport(order = 1)
    @GetMapping("/list")
    @Operation(summary = "直链下载日志")
    @ResponseBody
    public AjaxJson<Stream<DownloadLogResult>> list(QueryDownloadLogRequest queryDownloadLogRequest) {
        // 分页和排序
        boolean asc = Objects.equals(queryDownloadLogRequest.getOrderDirection(), "asc");
        OrderItem orderItem = asc ? OrderItem.asc(queryDownloadLogRequest.getOrderBy()) : OrderItem.desc(queryDownloadLogRequest.getOrderBy());
        Page<DownloadLog> pages = new Page<DownloadLog>(queryDownloadLogRequest.getPage(), queryDownloadLogRequest.getLimit())
                .addOrder(orderItem);

        LambdaQueryWrapper<DownloadLog> queryWrapper = new LambdaQueryWrapper<DownloadLog>()
                .eq(StringUtils.isNotEmpty(queryDownloadLogRequest.getStorageKey()), DownloadLog::getStorageKey, queryDownloadLogRequest.getStorageKey())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getPath()), DownloadLog::getPath, queryDownloadLogRequest.getPath())
                .isNotNull("shortLink".equals(queryDownloadLogRequest.getLinkType()), DownloadLog::getShortKey)
                .isNull("directLink".equals(queryDownloadLogRequest.getLinkType()), DownloadLog::getShortKey)
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getShortKey()), DownloadLog::getShortKey, queryDownloadLogRequest.getShortKey())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getIp()), DownloadLog::getIp, queryDownloadLogRequest.getIp())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getReferer()), DownloadLog::getReferer, queryDownloadLogRequest.getReferer())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getUserAgent()), DownloadLog::getUserAgent, queryDownloadLogRequest.getUserAgent())
                .ge(ObjUtil.isNotEmpty(queryDownloadLogRequest.getDateFrom()), DownloadLog::getCreateTime, queryDownloadLogRequest.getDateFrom())
                .le(ObjUtil.isNotEmpty(queryDownloadLogRequest.getDateTo()), DownloadLog::getCreateTime, queryDownloadLogRequest.getDateTo());

        Page<DownloadLog> selectResult = downloadLogService.selectPage(pages, queryWrapper);

        Map<String, StorageSource> cache = new HashMap<>();

        String serverAddress = systemConfigService.getAxiosFromDomainOrSetting();
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String directLinkPrefix = systemConfig.getDirectLinkPrefix();

        Stream<DownloadLogResult> shortLinkResultList = selectResult.getRecords().stream().map(model -> {
            String storageKey = model.getStorageKey();

            StorageSource storageSource = cache.computeIfAbsent(storageKey, (key) -> storageSourceService.findByStorageKey(key));
            DownloadLogResult downloadLogResult = downloadLogConvert.entityToResultList(model, storageSource);

            if (StringUtils.isNotBlank(downloadLogResult.getShortKey())) {
                downloadLogResult.setShortLink(StringUtils.concat(serverAddress, "s", downloadLogResult.getShortKey()));
            } else {
                downloadLogResult.setPathLink(StringUtils.concat(serverAddress, directLinkPrefix, downloadLogResult.getStorageKey(), downloadLogResult.getPath()));
            }

            return downloadLogResult;
        });
        return AjaxJson.getPageData(selectResult.getTotal(), shortLinkResultList);
    }


    @ApiOperationSupport(order = 2)
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除直链")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "直链 id", required = true, schema = @Schema(type = "integer"))
    @ResponseBody
    @DemoDisable
    public AjaxJson<Void> deleteById(@PathVariable Integer id) {
        downloadLogService.removeById(id);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @PostMapping("/delete/batch")
    @ResponseBody
    @Operation(summary = "批量删除直链")
    @DemoDisable
    public AjaxJson<Void> batchDelete(@RequestBody BatchDeleteRequest batchDeleteRequest) {
        List<Integer> ids = batchDeleteRequest.getIds();
        downloadLogService.removeBatchByIds(ids);
        return AjaxJson.getSuccess();
    }

    @ApiOperationSupport(order = 4)
    @PostMapping("/delete/batch/query")
    @ResponseBody
    @Operation(summary = "根据查询条件批量删除直链")
    @DemoDisable
    public AjaxJson<Void> batchDeleteBySearchParams(@RequestBody QueryDownloadLogRequest queryDownloadLogRequest) {

        LambdaQueryWrapper<DownloadLog> queryWrapper = new LambdaQueryWrapper<DownloadLog>()
                .eq(StringUtils.isNotEmpty(queryDownloadLogRequest.getStorageKey()), DownloadLog::getStorageKey, queryDownloadLogRequest.getStorageKey())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getPath()), DownloadLog::getPath, queryDownloadLogRequest.getPath())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getShortKey()), DownloadLog::getShortKey, queryDownloadLogRequest.getShortKey())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getIp()), DownloadLog::getIp, queryDownloadLogRequest.getIp())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getReferer()), DownloadLog::getReferer, queryDownloadLogRequest.getReferer())
                .like(StringUtils.isNotEmpty(queryDownloadLogRequest.getUserAgent()), DownloadLog::getUserAgent, queryDownloadLogRequest.getUserAgent())
                .ge(ObjUtil.isNotEmpty(queryDownloadLogRequest.getDateFrom()), DownloadLog::getCreateTime, queryDownloadLogRequest.getDateFrom())
                .le(ObjUtil.isNotEmpty(queryDownloadLogRequest.getDateTo()), DownloadLog::getCreateTime, queryDownloadLogRequest.getDateTo());

        downloadLogService.deleteByQueryWrapper(queryWrapper);
        return AjaxJson.getSuccess();
    }

}