package im.zhaojun.zfile.module.link.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.link.convert.ShortLinkConvert;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.link.model.request.BatchDeleteRequest;
import im.zhaojun.zfile.module.link.model.request.QueryShortLinkLogRequest;
import im.zhaojun.zfile.module.link.model.request.ShortLinkResult;
import im.zhaojun.zfile.module.link.service.ShortLinkService;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 直链管理接口
 *
 * @author zhaojun
 */
@Api(tags = "直链管理")
@ApiSort(7)
@Controller
@RequestMapping("/admin")
public class ShortLinkManagerController {

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private ShortLinkConvert shortLinkConvert;
    
    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriveClassName;

    @ApiOperationSupport(order = 1)
    @GetMapping("/link/list")
    @ApiOperation(value = "搜索短链")
    @ResponseBody
    public AjaxJson<?> list(QueryShortLinkLogRequest queryObj) {
        // 分页和排序
        boolean asc = Objects.equals(queryObj.getOrderDirection(), "asc");
        Page<ShortLink> pages = new Page<ShortLink>(queryObj.getPage(), queryObj.getLimit())
                                .addOrder(new OrderItem(queryObj.getOrderBy(), asc));
    
    
        String dateFrom = queryObj.getDateFrom();
        String dateTo = queryObj.getDateTo();
        
        boolean isSqlite = StrUtil.equals(datasourceDriveClassName, "org.sqlite.JDBC");
        if (isSqlite) {
            if (StrUtil.isNotEmpty(queryObj.getDateFrom())) {
                dateFrom = Convert.toStr(DateUtil.parseDateTime(dateFrom).getTime());
            }
            if (StrUtil.isNotEmpty(queryObj.getDateTo())) {
                dateTo = Convert.toStr(DateUtil.parseDateTime(dateTo).getTime());
            }
        }
    
        // 搜索条件
        QueryWrapper<ShortLink> queryWrapper =
                new QueryWrapper<>(new ShortLink())
                .eq(StrUtil.isNotEmpty(queryObj.getStorageId()), "storage_id", queryObj.getStorageId())
                .like(StrUtil.isNotEmpty(queryObj.getKey()), "short_key", queryObj.getKey())
                .like(StrUtil.isNotEmpty(queryObj.getUrl()), "url", queryObj.getUrl())
                .ge(StrUtil.isNotEmpty(queryObj.getDateFrom()), "create_date", dateFrom)
                .le(StrUtil.isNotEmpty(queryObj.getDateTo()), "create_date", dateTo);
    
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
        return AjaxJson.getPageData(selectResult.getTotal(), shortLinkResultList);
    }


    @ApiOperationSupport(order = 2)
    @DeleteMapping("/link/delete/{id}")
    @ApiOperation(value = "删除短链")
    @ApiImplicitParam(paramType = "path", name = "id", value = "短链 id", required = true, dataTypeClass = Integer.class)
    @ResponseBody
    public AjaxJson<Void> deleteById(@PathVariable Integer id) {
        shortLinkService.removeById(id);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @PostMapping("/link/delete/batch")
    @ResponseBody
    @ApiOperation(value = "批量删除短链")
    public AjaxJson<Void> batchDelete(@RequestBody BatchDeleteRequest batchDeleteRequest) {
        shortLinkService.removeBatchByIds(batchDeleteRequest.getIds());
        return AjaxJson.getSuccess();
    }

}