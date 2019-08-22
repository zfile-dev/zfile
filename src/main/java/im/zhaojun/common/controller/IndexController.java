package im.zhaojun.common.controller;

import im.zhaojun.common.service.SystemConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping("/")
    public String index() {
        return "redirect:/file/";
    }

    @GetMapping("/file/**")
    public ModelAndView index(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setViewName("index");
        modelAndView.addObject("systemConfig", systemConfigService.getSystemConfig());
        return modelAndView;
    }

    @GetMapping("/admin")
    public ModelAndView admin(ModelAndView modelAndView) {
        modelAndView.setViewName("admin");
        modelAndView.addObject("systemConfig", systemConfigService.getSystemConfig());
        return modelAndView;
    }



}
