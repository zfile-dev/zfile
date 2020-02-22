package im.zhaojun.onedrive.common.controller;

import im.zhaojun.onedrive.china.service.OneDriveChinaServiceImpl;
import im.zhaojun.onedrive.common.model.OneDriveToken;
import im.zhaojun.onedrive.international.service.OneDriveServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@Controller
@RequestMapping("/onedrive")
public class OneDriveController {

    @Resource
    private OneDriveServiceImpl oneDriveServiceImpl;

    @Resource
    private OneDriveChinaServiceImpl oneDriveChinaServiceImpl;

    @GetMapping("/callback")
    public String onedriveCallback(String code, Model model) {
        OneDriveToken oneDriveToken = oneDriveServiceImpl.getToken(code);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }


    @GetMapping("/china-callback")
    public String onedriveChinaCallback(String code, Model model) {
        OneDriveToken oneDriveToken = oneDriveChinaServiceImpl.getToken(code);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }

}
