package im.zhaojun.zfile.module.link.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.cache.LinkRateLimiterCache;
import im.zhaojun.zfile.module.link.convert.ShortLinkConvert;
import im.zhaojun.zfile.module.link.model.dto.CacheInfo;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.link.model.request.BatchDeleteRequest;
import im.zhaojun.zfile.module.link.model.request.QueryShortLinkLogRequest;
import im.zhaojun.zfile.module.link.model.request.ShortLinkResult;
import im.zhaojun.zfile.module.link.service.ShortLinkService;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直链管理接口
 *
 * @author zhaojun
 */
@Tag(name = "直链管理")
@ApiSort(7)
@Controller
@RequestMapping("/admin")
public class ShortLinkManagerController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private ShortLinkConvert shortLinkConvert;

    @Resource
    private LinkRateLimiterCache linkRateLimiterCache;


    @ApiOperationSupport(order = 1)
    @GetMapping("/link/list")
    @Operation(summary = "搜索短链")
    @ResponseBody
    public AjaxJson<List<ShortLinkResult>> list(QueryShortLinkLogRequest queryObj) {
        Page<ShortLinkResult> resultPage = getShortLinkResultPage(queryObj);

        String serverAddress = systemConfigService.getAxiosFromDomainOrSetting();

        resultPage.getRecords().forEach(shortLinkResult -> {
            shortLinkResult.setShortLink(StringUtils.concat(serverAddress, "s", shortLinkResult.getShortKey()));
        });

        return AjaxJson.getPageData(resultPage.getTotal(), resultPage.getRecords());
    }

    @ApiOperationSupport(order = 2)
    @DeleteMapping("/link/delete/{id}")
    @Operation(summary = "删除短链")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "短链 id", required = true, schema = @Schema(type = "integer"))
    @ResponseBody
    @DemoDisable
    public AjaxJson<Void> deleteById(@PathVariable Integer id) {
        shortLinkService.removeById(id);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @PostMapping("/link/delete/batch")
    @ResponseBody
    @Operation(summary = "批量删除短链")
    @DemoDisable
    public AjaxJson<Void> batchDelete(@RequestBody BatchDeleteRequest batchDeleteRequest) {
        shortLinkService.removeBatchByIds(batchDeleteRequest.getIds());
        return AjaxJson.getSuccess();
    }

    @ApiOperationSupport(order = 4)
    @GetMapping("/link/export")
    @ResponseBody
    @Operation(summary = "导出短链")
    public void exportExcel(QueryShortLinkLogRequest queryObj, HttpServletResponse response) throws IOException {
        Page<ShortLinkResult> shortLinkResultPage = getShortLinkResultPage(queryObj);

        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("id", "ID");
        writer.addHeaderAlias("storageName", "存储源名称");
        writer.addHeaderAlias("storageTypeStr", "存储源类型");
        writer.addHeaderAlias("shortKey", "短链 key");
        writer.addHeaderAlias("url", "文件路径");
        writer.addHeaderAlias("createDate", "创建时间");
        writer.addHeaderAlias("expireDate", "过期时间");
        writer.setOnlyAlias(true);

        writer.write(shortLinkResultPage.getRecords(), true);
        writer.setColumnWidth(0, 8);
        writer.setColumnWidth(1, 30);
        writer.setColumnWidth(2, 15);
        writer.setColumnWidth(3, 15);
        writer.setColumnWidth(4, 50);
        writer.setColumnWidth(5, 15);
        writer.setColumnWidth(6, 15);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        ServletOutputStream out=response.getOutputStream();

        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }


    @ApiOperationSupport(order = 5)
    @GetMapping("/link/limit/info")
    @ResponseBody
    @Operation(summary = "获取直链访问限制信息")
    public AjaxJson<List<CacheInfo<String, AtomicInteger>>> getLinkLimitInfo() {
        return AjaxJson.getSuccessData(linkRateLimiterCache.getCacheInfo());
    }

    @NotNull
    private Page<ShortLinkResult> getShortLinkResultPage(QueryShortLinkLogRequest queryObj) {
        // 分页和排序
        boolean asc = Objects.equals(queryObj.getOrderDirection(), "asc");
        OrderItem orderItem = asc ? OrderItem.asc(queryObj.getOrderBy()) : OrderItem.desc(queryObj.getOrderBy());
        Page<ShortLink> pages = new Page<ShortLink>(queryObj.getPage(), queryObj.getLimit())
                .addOrder(orderItem);

        // 搜索条件
        LambdaQueryWrapper<ShortLink> queryWrapper = new LambdaQueryWrapper<ShortLink>()
                .eq(StringUtils.isNotEmpty(queryObj.getStorageId()), ShortLink::getStorageId, queryObj.getStorageId())
                .like(StringUtils.isNotEmpty(queryObj.getKey()), ShortLink::getShortKey, queryObj.getKey())
                .like(StringUtils.isNotEmpty(queryObj.getUrl()), ShortLink::getUrl, queryObj.getUrl())
                .ge(ObjUtil.isNotEmpty(queryObj.getDateFrom()), ShortLink::getCreateDate, queryObj.getDateFrom())
                .le(ObjUtil.isNotEmpty(queryObj.getDateTo()), ShortLink::getCreateDate, queryObj.getDateTo());

        // 执行查询
        Page<ShortLink> selectResult = shortLinkService.selectPage(pages, queryWrapper);

        // 转换为结果集
        Map<Integer, StorageSource> cache = new HashMap<>();
        Stream<ShortLinkResult> shortLinkResultList = selectResult.getRecords().stream().map(shortLink -> {
            Integer shortLinkStorageId = shortLink.getStorageId();

            StorageSource storageSource = cache.getOrDefault(shortLinkStorageId, storageSourceService.findById(shortLinkStorageId));
            cache.put(shortLinkStorageId, storageSource);
            return shortLinkConvert.entityToResultList(shortLink, storageSource);
        });

        Page<ShortLinkResult> resultPage = new Page<>();
        resultPage.setTotal(selectResult.getTotal());
        resultPage.setRecords(shortLinkResultList.collect(Collectors.toList()));
        return resultPage;
    }

    @ApiOperationSupport(order = 6)
    @DeleteMapping("/link/deleteExpireLink")
    @Operation(summary = "删除过期短链")
    @ResponseBody
    @DemoDisable
    public AjaxJson<Integer> deleteExpireLink() {
        int updateRows = shortLinkService.deleteExpireLink();
        return AjaxJson.getSuccessData(updateRows);
    }

}