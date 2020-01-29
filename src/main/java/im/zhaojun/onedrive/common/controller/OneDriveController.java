package im.zhaojun.onedrive.common.controller;

import im.zhaojun.onedrive.china.service.OneDriveChinaService;
import im.zhaojun.onedrive.common.model.OneDriveToken;
import im.zhaojun.onedrive.international.service.OneDriveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@Controller
@RequestMapping("/onedirve")
public class OneDriveController {

    @Resource
    private OneDriveService oneDriveService;

    @Resource
    private OneDriveChinaService oneDriveChinaService;

    @GetMapping("/callback")
    public String onedriveCallback(String code, Model model) {
        OneDriveToken oneDriveToken = oneDriveService.getToken(code);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }


    @GetMapping("/china-callback")
    public String onedriveChinaCallback(String code, Model model) {
        OneDriveToken oneDriveToken = oneDriveChinaService.getToken(code);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }

}
