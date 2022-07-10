package im.zhaojun.zfile.admin.controller.link;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.admin.convert.ShortLinkConvert;
import im.zhaojun.zfile.admin.model.entity.ShortLink;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.result.link.ShortLinkResult;
import im.zhaojun.zfile.admin.service.ShortLinkService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    @ApiOperationSupport(order = 1)
    @GetMapping("/link/list")
    @ApiOperation(value = "搜索短链")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "key", value = "短链 key"),
            @ApiImplicitParam(paramType = "query", name = "storageId", value = "存储源 ID"),
            @ApiImplicitParam(paramType = "query", name = "url", value = "短链 url"),
            @ApiImplicitParam(paramType = "query", name = "dateFrom", value = "短链创建时间从"),
            @ApiImplicitParam(paramType = "query", name = "dateTo", value = "短链创建时间至"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "分页页数"),
            @ApiImplicitParam(paramType = "query", name = "limit", value = "每页条数"),
            @ApiImplicitParam(paramType = "query", name = "orderBy", defaultValue = "createDate", value = "排序字段"),
            @ApiImplicitParam(paramType = "query", name = "orderDirection", defaultValue = "desc", value = "排序顺序")
    })
    public AjaxJson<?> list(String key, String storageId,
                                          String url,
                                          String dateFrom,
                                          String dateTo,
                                          Integer page,
                                          Integer limit,
                                          @RequestParam(required = false, defaultValue = "create_date") String orderBy,
                                          @RequestParam(required = false, defaultValue = "desc") String orderDirection) {
        Page<ShortLink> pages = Page.of(page, limit);
        boolean asc = Objects.equals(orderDirection, "asc");
        pages.addOrder(new OrderItem(orderBy, asc));
        QueryWrapper<ShortLink> queryWrapper = new QueryWrapper<>(new ShortLink());
        if (StrUtil.isNotEmpty(storageId)) {
            queryWrapper.eq("storage_id", storageId);
        }
        if (StrUtil.isNotEmpty(key)) {
            queryWrapper.like("short_key", key);
        }
        if (StrUtil.isNotEmpty(url)) {
            queryWrapper.like("url", url);
        }
        if (StrUtil.isNotEmpty(dateFrom)) {
            queryWrapper.ge("create_date", dateFrom);
        }
        if (StrUtil.isNotEmpty(dateTo)) {
            queryWrapper.le("create_date", dateTo);
        }
        Page<ShortLink> selectResult = shortLinkService.page(pages, queryWrapper);

        long total = selectResult.getTotal();
        List<ShortLink> records = selectResult.getRecords();

        Map<Integer, StorageSource> cache = new HashMap<>();

        Stream<ShortLinkResult> shortLinkResultList = records.stream().map(shortLink -> {
            Integer shortLinkStorageId = shortLink.getStorageId();
            StorageSource storageSource;
            if (cache.containsKey(shortLinkStorageId)) {
                storageSource = cache.get(shortLinkStorageId);
            } else {
                storageSource = storageSourceService.findById(shortLinkStorageId);
                cache.put(shortLinkStorageId, storageSource);
            }
            return shortLinkConvert.entityToResultList(shortLink, storageSource);
        });
        return AjaxJson.getPageData(total, shortLinkResultList);
    }


    @ApiOperationSupport(order = 2)
    @DeleteMapping("/link/delete/{id}")
    @ApiOperation(value = "删除短链")
    @ApiImplicitParam(paramType = "path", name = "id", value = "短链 id", required = true)
    @ResponseBody
    public AjaxJson<Void> deleteById(@PathVariable Integer id) {
        shortLinkService.removeById(id);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @DeleteMapping("/link/delete/batch")
    @ResponseBody
    @ApiImplicitParam(paramType = "query", name = "ids", value = "短链 id", required = true)
    @ApiOperation(value = "批量删除短链")
    public AjaxJson<Void> batchDelete(@RequestParam("id[]") Integer[] ids) {
        shortLinkService.removeBatchByIds(Arrays.asList(ids));
        return AjaxJson.getSuccess();
    }

}