package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.storage.model.result.Open115AuthDeviceCodeResult;
import im.zhaojun.zfile.module.storage.model.result.Open115GetStatusResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "115 工具辅助模块")
@Controller
@RequestMapping("/115")
public class Open115HelperController {

    @GetMapping("/qrcode")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取二维码")
    public AjaxJson<Open115AuthDeviceCodeResult> generateQrCode(String appId) {
        String codeVerifier = RandomUtil.randomString(128);
        String codeChallenge = Base64.encode(SecureUtil.md5().digest(codeVerifier));

        // https://www.yuque.com/115yun/open/shtpzfhewv5nag11
        HttpRequest httpRequest = HttpUtil.createPost("https://passportapi.115.com/open/authDeviceCode")
                .form("client_id", appId)
                .form("code_challenge", codeChallenge)
                .form("code_challenge_method", "md5");

        HttpResponse execute = httpRequest.execute();
        String body = execute.body();

        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.getInteger("state") == 0) {
            throw new SystemException(jsonObject.getString("error"));
        }

        Open115AuthDeviceCodeResult open115AuthDeviceCodeResult = JSON.parseObject(body).getObject("data", Open115AuthDeviceCodeResult.class);
        open115AuthDeviceCodeResult.setCodeVerifier(codeVerifier);
        return AjaxJson.getSuccessData(open115AuthDeviceCodeResult);
    }

    @PostMapping("/qrCodeStatus")
    @ResponseBody
    @ApiOperationSupport(order = 2)
    @Operation(summary = "获取二维码状态")
    public AjaxJson<Open115GetStatusResult> getQrCodeStatus(@RequestBody Open115AuthDeviceCodeResult open115AuthDeviceCodeResult) {

        // https://www.yuque.com/115yun/open/shtpzfhewv5nag11#6d33298a
        HttpRequest httpRequest = HttpUtil.createGet("https://qrcodeapi.115.com/get/status/")
                .form("uid", open115AuthDeviceCodeResult.getUid())
                .form("time", open115AuthDeviceCodeResult.getTime())
                .form("sign", open115AuthDeviceCodeResult.getSign());

        httpRequest.setReadTimeout(0);
        HttpResponse execute = httpRequest.execute();
        String body = execute.body();

        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.getInteger("state") == 0) {
            return AjaxJson.getSuccessData(Open115GetStatusResult.error(jsonObject.getString("error")));
        }

        if (jsonObject.getInteger("state") == 1 && !jsonObject.getJSONObject("data").containsKey("status")) {
            return AjaxJson.getSuccessData(Open115GetStatusResult.waiting());
        }

        if (jsonObject.getInteger("state") == 1 && jsonObject.getJSONObject("data").getInteger("status") == 1) {
            return AjaxJson.getSuccessData(Open115GetStatusResult.scanning(jsonObject.getJSONObject("data").getString("msg")));
        }


        // https://www.yuque.com/115yun/open/shtpzfhewv5nag11#QCCVQ
        HttpRequest deviceCodeToTokenHttpRequest = HttpUtil.createPost("https://passportapi.115.com/open/deviceCodeToToken")
                .form("uid", open115AuthDeviceCodeResult.getUid())
                .form("code_verifier", open115AuthDeviceCodeResult.getCodeVerifier());
        String deviceCodeToTokenBody = deviceCodeToTokenHttpRequest.execute().body();
        JSONObject deviceCodeToTokenJsonObject = JSON.parseObject(deviceCodeToTokenBody).getJSONObject("data");
        String accessToken = deviceCodeToTokenJsonObject.getString("access_token");
        String refreshToken = deviceCodeToTokenJsonObject.getString("refresh_token");
        Integer expiresIn = deviceCodeToTokenJsonObject.getInteger("expires_in");

            // 否则认为 expiredAt 是过期时间(单位: 秒)
        Integer expiredAt = expiresIn + (int) (System.currentTimeMillis() / 1000);

        return AjaxJson.getSuccessData(Open115GetStatusResult.success(accessToken, refreshToken, expiredAt));
    }
}
