package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.APIHttpRequestBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.request.SharePointInfoRequest;
import im.zhaojun.zfile.module.storage.model.request.SharePointSearchSitesRequest;
import im.zhaojun.zfile.module.storage.model.request.SharePointSiteListsRequest;
import im.zhaojun.zfile.module.storage.model.result.SharepointSiteListResult;
import im.zhaojun.zfile.module.storage.model.result.SharepointSiteResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * SharePoint 工具类
 *
 * @author zhaojun
 */
@Tag(name = "SharePoint 工具辅助模块")
@Controller
@RequestMapping("/sharepoint")
public class SharePointHelperController {

    private static final String SHAREPOINT_LIST_TYPE_EVENT = "事件";

    private static final String SHAREPOINT_LIST_TYPE_DOCUMENT = "文档";


    @PostMapping("/getSites")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取网站列表")
    public AjaxJson<List<SharepointSiteResult>> getSites(@Valid @RequestBody SharePointSearchSitesRequest searchSitesRequest) {
        List<SharepointSiteResult> sites = new ArrayList<>();

        String requestUrl = getSearchSiteUrlByType(searchSitesRequest.getType());

        // 构建请求认证 Token 信息
        String tokenValue = getBearer(searchSitesRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        getRequest.form("search", " ");
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new APIHttpRequestBizException(ErrorCode.BIZ_AUTO_GET_SHARE_POINT_SITES_ERROR, requestUrl, execute.getStatus(), body);
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
    @Operation(summary = "获取网站下的子目录")
    public AjaxJson<List<SharepointSiteListResult>> getSites(@Valid @RequestBody SharePointSiteListsRequest sharePointSiteListsRequest) {
        List<SharepointSiteListResult> sites = new ArrayList<>();

        String siteId = sharePointSiteListsRequest.getSiteId();

        String[] siteIdSplit = siteId.split(",");
        if (siteIdSplit.length > 1) {
            siteId = siteIdSplit[1];
        }

        String requestUrl = getSiteListsUrlByType(sharePointSiteListsRequest.getType(), siteId);

        // 构建请求认证 Token 信息
        String tokenValue = getBearer(sharePointSiteListsRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new APIHttpRequestBizException(ErrorCode.BIZ_AUTO_GET_SHARE_POINT_SITES_ERROR, requestUrl, execute.getStatus(), body);
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
    @Operation(summary = "获取域名前缀")
    @ResponseBody
    public AjaxJson<String> getDomainPrefix(@RequestBody SharePointInfoRequest sharePointInfoRequest) {
        // 请求 URL
        String requestUrl = getSiteRootUrlByType(sharePointInfoRequest.getType());

        // 构建请求认证 Token 信息
        String tokenValue = getBearer(sharePointInfoRequest.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new BizException(body);
        }

        // 解析前缀
        JSONObject jsonObject = JSONObject.parseObject(body);
        String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
        String domainPrefix = StringUtils.subBefore(hostname, ".sharepoint", false);
        return AjaxJson.getSuccessData(domainPrefix);
    }


    @PostMapping("/getSiteId")
    @ApiOperationSupport(order = 4)
    @Operation(summary = "获取 SiteId")
    @ResponseBody
    public AjaxJson<String> getSiteId(@RequestBody SharePointInfoRequest sharePointInfoRequest) {

        // 判断必填参数
        if (sharePointInfoRequest == null || sharePointInfoRequest.getAccessToken() == null || sharePointInfoRequest.getSiteName() == null) {
            throw new BizException(ErrorCode.BIZ_BAD_REQUEST);
        }

        // 构建请求认证 Token 信息
        String tokenValue = getBearer(sharePointInfoRequest.getAccessToken());
        HashMap<String, String> authorizationHeaders = new HashMap<>();
        authorizationHeaders.put("Authorization", tokenValue);


        // 如果没有域名前缀, 则先获取
        if (sharePointInfoRequest.getDomainPrefix() == null || sharePointInfoRequest.getDomainType() == null) {
            String requestUrl = getSiteRootUrlByType(sharePointInfoRequest.getType());
            HttpRequest getRequest = HttpUtil.createGet(requestUrl);
            HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
            String body = execute.body();
            if (execute.getStatus() != HttpStatus.OK.value()) {
                throw new BizException(body);
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
            String domainPrefix = StringUtils.subBefore(hostname, ".sharepoint", false);
            sharePointInfoRequest.setDomainPrefix(domainPrefix);
        }


        if (StringUtils.isEmpty(sharePointInfoRequest.getSiteType())) {
            sharePointInfoRequest.setSiteType("/sites/");
        }

        // 请求接口
        String host = getHostByType(sharePointInfoRequest.getType());
        String requestUrl = String.format("https://%s/v1.0/sites/%s.sharepoint.%s:/%s/%s", host,
                sharePointInfoRequest.getDomainPrefix(),
                sharePointInfoRequest.getDomainType(),
                sharePointInfoRequest.getSiteType(),
                sharePointInfoRequest.getSiteName());
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
        String body = execute.body();

        // 解析数据
        if (execute.getStatus() != HttpStatus.OK.value()) {
            throw new BizException(body);
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
            throw new BizException(ErrorCode.BIZ_UNSUPPORTED_STORAGE_TYPE);
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
        return String.format("https://%s/v1.0/sites", hostByType);
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
        return String.format("https://%s/v1.0/sites/%s/lists",hostByType, siteId);
    }


    /**
     * 获取网站根目录请求 URL
     *
     * @param   type
     *          网站类型：
     *              Standard：标准版
     *              China：世纪互联版
     *
     * @return  搜索网站请求 URL
     */
    private String getSiteRootUrlByType(String type) {
        String hostByType = getHostByType(type);
        return String.format("https://%s/v1.0/sites/root", hostByType);
    }


    /**
     * 获取 Bearer 格式的 Token
     *
     * @param   accessToken
     *          访问令牌
     *
     * @return  Bearer 格式的 Token
     */
    private static String getBearer(String accessToken) {
        return String.format("%s %s", "Bearer", accessToken);
    }

}