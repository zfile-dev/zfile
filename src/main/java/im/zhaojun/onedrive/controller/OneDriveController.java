package im.zhaojun.onedrive.controller;

import im.zhaojun.onedrive.service.OneDriveService;
import im.zhaojun.onedrive.service.OneDriveToken;
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

    @GetMapping("/callback")
    public String onedriveCallback(String code, Model model) {
        OneDriveToken oneDriveToken = oneDriveService.getToken(code);
        model.addAttribute("accessToken", oneDriveToken.getAccessToken());
        model.addAttribute("refreshToken", oneDriveToken.getRefreshToken());
        return "callback";
    }

}
