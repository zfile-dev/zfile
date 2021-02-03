package im.zhaojun.zfile.controller.onedrive;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.model.dto.SharePointInfoVO;
import im.zhaojun.zfile.model.support.ResultBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author zhaojun
 * SharePoint 工具类
 */
@Controller
@RequestMapping("/sharepoint")
public class SharePointHelperController {


    /**
     * 根据 AccessToken 获取域名前缀
     */
    @PostMapping("/getDomainPrefix")
    @ResponseBody
    public ResultBean getDomainPrefix(@RequestBody SharePointInfoVO sharePointInfoVO) {

        String host = "";

        // 判断是标准版还是世纪互联版
        if (Objects.equals(sharePointInfoVO.getType(), "Standard")) {
            host = "graph.microsoft.com";
        } else if (Objects.equals(sharePointInfoVO.getType(), "China")) {
            host = "microsoftgraph.chinacloudapi.cn";
        }

        // 请求 URL
        String requestUrl = StrUtil.format("https://{}/v1.0/sites/root", host);

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", sharePointInfoVO.getAccessToken());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", tokenValue);

        // 请求接口
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(headers).execute();
        String body = execute.body();
        if (execute.getStatus() != HttpStatus.OK.value()) {
            return ResultBean.error(body);
        }

        // 解析前缀
        JSONObject jsonObject = JSONObject.parseObject(body);
        String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
        String domainPrefix = StrUtil.subBefore(hostname, ".sharepoint", false);
        return ResultBean.successData(domainPrefix);
    }

    @PostMapping("/getSiteId")
    @ResponseBody
    public ResultBean getSiteId(@RequestBody SharePointInfoVO sharePointInfoVO) {

        // 判断必填参数
        if (sharePointInfoVO == null || sharePointInfoVO.getAccessToken() == null || sharePointInfoVO.getSiteName() == null) {
            return ResultBean.error("参数不全");
        }

        String host = "";

        // 判断是标准版还是世纪互联版
        if (Objects.equals(sharePointInfoVO.getType(), "Standard")) {
            host = "graph.microsoft.com";
            sharePointInfoVO.setDomainType("com");
        } else if (Objects.equals(sharePointInfoVO.getType(), "China")) {
            host = "microsoftgraph.chinacloudapi.cn";
            sharePointInfoVO.setDomainType("cn");
        } else {
            return ResultBean.error("参数不全");
        }

        // 构建请求认证 Token 信息
        String tokenValue = String.format("%s %s", "Bearer", sharePointInfoVO.getAccessToken());
        HashMap<String, String> authorizationHeaders = new HashMap<>();
        authorizationHeaders.put("Authorization", tokenValue);


        // 如果没有域名前缀, 则先获取
        if (sharePointInfoVO.getDomainPrefix() == null || sharePointInfoVO.getDomainType() == null) {
            String requestUrl = StrUtil.format("https://{}/v1.0/sites/root", host);
            HttpRequest getRequest = HttpUtil.createGet(requestUrl);
            HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
            String body = execute.body();
            if (execute.getStatus() != HttpStatus.OK.value()) {
                return ResultBean.error(body);
            }
            JSONObject jsonObject = JSONObject.parseObject(body);
            String hostname = jsonObject.getJSONObject("siteCollection").getString("hostname");
            String domainPrefix = StrUtil.subBefore(hostname, ".sharepoint", false);
            sharePointInfoVO.setDomainPrefix(domainPrefix);
        }


        if (StrUtil.isEmpty(sharePointInfoVO.getSiteType())) {
            sharePointInfoVO.setSiteType("/sites/");
        }

        // 请求接口
        String requestUrl = StrUtil.format("https://{}/v1.0/sites/{}.sharepoint.{}:/{}/{}", host,
                sharePointInfoVO.getDomainPrefix(),
                sharePointInfoVO.getDomainType(),
                sharePointInfoVO.getSiteType(),
                sharePointInfoVO.getSiteName());
        HttpRequest getRequest = HttpUtil.createGet(requestUrl);
        HttpResponse execute = getRequest.addHeaders(authorizationHeaders).execute();
        String body = execute.body();

        // 解析数据
        if (execute.getStatus() != HttpStatus.OK.value()) {
            return ResultBean.error(body);
        }
        JSONObject jsonObject = JSONObject.parseObject(body);
        return ResultBean.successData(jsonObject.getString("id"));
    }

}
