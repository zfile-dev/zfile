package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.storage.model.request.SharePointSearchSitesRequest;
import im.zhaojun.zfile.module.storage.model.request.SharePointSiteListsRequest;
import im.zhaojun.zfile.module.storage.model.result.SharepointSiteResult;
import im.zhaojun.zfile.module.storage.model.result.SharepointSiteListResult;
import im.zhaojun.zfile.module.storage.model.request.SharePointInfoRequest;
import im.zhaojun.zfile.core.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * SharePoint 工具类
 *
 * @author zhaojun
 */
@Api(tags = "SharePoint 工具辅助模块")
@Controller
@RequestMapping("/sharepoint")
public class SharePointHelperController {

    private static final String SHAREPOINT_LIST_TYPE_EVENT = "事件";

    private static final String SHAREPOINT_LIST_TYPE_DOCUMENT = "文档";


    @PostMapping("/getSites")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取网站列表")
    public AjaxJson<List<SharepointSiteResult>> getSites(@Valid @RequestBody SharePointSearchSitesRequest searchSitesRequest) {
        List<SharepointSiteResult> sites = new ArrayList<>();

        String requestUrl = getSearchSiteUrlByType(searchSitesRequest.getType());

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", searchSitesRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        getRequest.form("search", " ");
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new RuntimeException("自动获取网站列表失败：" + body);
        }

        // 解析前缀
        JSONObject rootObject = JSONObject.parseObject(body);
        JSONArray valueArray = rootObject.getJSONArray("value");
        for (int i = 0; i < valueArray.size(); i++) {
            SharepointSiteResult sharepointSiteResult = valueArray.getObject(i, SharepointSiteResult.class);
            sites.add(sharepointSiteResult);
        }

        return AjaxJson.getSuccessData(sites);
    }


    @PostMapping("/getSiteLists")
    @ResponseBody
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "获取网站下的子目录")
    public AjaxJson<List<SharepointSiteListResult>> getSites(@Valid @RequestBody SharePointSiteListsRequest sharePointSiteListsRequest) {
        List<SharepointSiteListResult> sites = new ArrayList<>();

        String siteId = sharePointSiteListsRequest.getSiteId();

        String[] siteIdSplit = siteId.split(",");
        if (siteIdSplit.length > 1) {
            siteId = siteIdSplit[1];
        }

        String requestUrl = getSiteListsUrlByType(sharePointSiteListsRequest.getType(), siteId);

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", sharePointSiteListsRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new RuntimeException("自动获取网站子目录列表失败：" + body);
        }

        // 解析前缀
        JSONObject rootObject = JSONObject.parseObject(body);
        JSONArray valueArray = rootObject.getJSONArray("value");
        for (int i = 0; i < valueArray.size(); i++) {
            SharepointSiteListResult sharepointSiteListResult = valueArray.getObject(i, SharepointSiteListResult.class);
            // 如果是事件目录，则跳过
            if (Objects.equals(SHAREPOINT_LIST_TYPE_EVENT, sharepointSiteListResult.getDisplayName())) {
                continue;
            }

            // 如果是文档类型，则改名为"默认文档"
            if (Objects.equals(SHAREPOINT_LIST_TYPE_DOCUMENT, sharepointSiteListResult.getDisplayName())) {
                sharepointSiteListResult.setDisplayName("默认文档");
            }
            sites.add(sharepointSiteListResult);
        }
        sites.sort(Comparator.comparing(SharepointSiteListResult::getCreatedDateTime));
        return AjaxJson.getSuccessData(sites);
    }


    @PostMapping("/getDomainPrefix")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "获取域名前缀")
    @ResponseBody
    public AjaxJson<String> getDomainPrefix(@RequestBody SharePointInfoRequest sharePointInfoRequest) {
        String host = "";

        // 判断是标准版还是世纪互联版
        if (Objects.equals(sharePointInfoRequest.getType(), "Standard")) {
            host = "graph.microsoft.com";
        } else if (Objects.equals(sharePointInfoRequest.getType(), "China")) {
            host = "microsoftgraph.chinacloudapi.cn";
        }

        // 请求 URL
        String requestUrl = StrUtil.format("https://{}/v1.0/sites/root", host);

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", sharePointInfoRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            return AjaxJson.getError(body);
        }

        // 解析前缀
        JSONObject jsonObject = JSONObject.parseObject(body);
        String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
        String domainPrefix = StrUtil.subBefore(hostname, ".sharepoint", false);
        return AjaxJson.getSuccessData(domainPrefix);
    }


    @PostMapping("/getSiteId")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "获取 SiteId")
    @ResponseBody
    public AjaxJson<String> getSiteId(@RequestBody SharePointInfoRequest sharePointInfoRequest) {

        // 判断必填参数
        if (sharePointInfoRequest == null || sharePointInfoRequest.getAccessToken() == null || sharePointInfoRequest.getSiteName() == null) {
            return AjaxJson.getError("参数不全");
        }

        String host = "";

        // 判断是标准版还是世纪互联版
        if (Objects.equals(sharePointInfoRequest.getType(), "Standard")) {
            host = "graph.microsoft.com";
            sharePointInfoRequest.setDomainType("com");
        } else if (Objects.equals(sharePointInfoRequest.getType(), "China")) {
            host = "microsoftgraph.chinacloudapi.cn";
            sharePointInfoRequest.setDomainType("cn");
        } else {
            return AjaxJson.getError("参数不全");
        }

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", sharePointInfoRequest.getAccessToken());
        HashMap<String, String> authorizationHeaders = new HashMap<>();
        authorizationHeaders.put("Authorization", tokenValue);


        // 如果没有域名前缀, 则先获取
        if (sharePointInfoRequest.getDomainPrefix() == null || sharePointInfoRequest.getDomainType() == null) {
            String requestUrl = StrUtil.format("https://{}/v1.0/sites/root", host);
            HttpRequest getRequest = HttpUtil.createGet(requestUrl);
            HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
            String body = execute.body();
            if (execute.getStatus() != HttpStatus.OK.value()) {
                return AjaxJson.getError(body);
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
            String domainPrefix = StrUtil.subBefore(hostname, ".sharepoint", false);
            sharePointInfoRequest.setDomainPrefix(domainPrefix);
        }


        if (StrUtil.isEmpty(sharePointInfoRequest.getSiteType())) {
            sharePointInfoRequest.setSiteType("/sites/");
        }

        // 请求接口
        String requestUrl = StrUtil.format("https://{}/v1.0/sites/{}.sharepoint.{}:/{}/{}", host,
                sharePointInfoRequest.getDomainPrefix(),
                sharePointInfoRequest.getDomainType(),
                sharePointInfoRequest.getSiteType(),
                sharePointInfoRequest.getSiteName());
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
        String body = execute.body();

        // 解析数据
        if (execute.getStatus() != HttpStatus.OK.value()) {
            return AjaxJson.getError(body);
        }
        JSONObject jsonObject = JSONObject.parseObject(body);
        return AjaxJson.getSuccessData(jsonObject.getString("id"));
    }


    /**
     * 根据类型获取 API 地址
     *
     * @param   type
     *          网站类型：
     *              Standard：标准版
     *              China：世纪互联版
     *
     * @return  API 地址
     */
    private String getHostByType(String type) {
        // 判断是标准版还是世纪互联版
        if (Objects.equals(type, "Standard")) {
            return "graph.microsoft.com";
        } else if (Objects.equals(type, "China")) {
            return "microsoftgraph.chinacloudapi.cn";
        } else {
            throw new RuntimeException("不支持的类型");
        }
    }


    /**
     * 获取搜索网站请求 URL
     *
     * @param   type
     *          网站类型：
     *              Standard：标准版
     *              China：世纪互联版
     *
     * @return  搜索网站请求 URL
     */
    private String getSearchSiteUrlByType(String type) {
        String hostByType = getHostByType(type);
        return StrUtil.format("https://{}/v1.0/sites", hostByType);
    }


    /**
     * 获取搜索网站请求 URL
     *
     * @param   type
     *          网站类型：
     *              Standard：标准版
     *              China：世纪互联版
     *
     * @return  搜索网站请求 URL
     */
    private String getSiteListsUrlByType(String type, String siteId) {
        String hostByType = getHostByType(type);
        return StrUtil.format("https://{}/v1.0/sites/{}/lists",hostByType, siteId);
    }

}