package im.zhaojun.onedrive.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zhaojun
 */
@Controller
public class OneDriveController {

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/onedirve/callback")
    @ResponseBody
    public String onedriveCallback(String code, HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        String json = "client_id=04a73532-6c16-4fe4-92e5-f2cd125ed553&redirect_uri=http://localhost:8080/onedirve/callback&client_secret=2gY/t?*Eff6i36TgKTtiG*08/k]@.I4[&code=" + code + "&grant_type=authorization_code";

        HttpRequest post = HttpUtil.createPost("https://login.microsoftonline.com/common/oauth2/v2.0/token");
        post.body(json, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();

        System.out.println(response.body());
        return response.body();
    }


}
