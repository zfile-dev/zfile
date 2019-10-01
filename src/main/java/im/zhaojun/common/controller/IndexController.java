package im.zhaojun.common.controller;

import im.zhaojun.common.service.ViewConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller
public class IndexController {

    @Resource
    private ViewConfigService viewConfigService;

    @GetMapping("/")
    public String index() {
        return "redirect:/file/";
    }

    @GetMapping("/file/**")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.setViewName("index");
        modelAndView.addObject("viewConfig", viewConfigService.getViewConfig());
        return modelAndView;
    }

    @GetMapping("/admin")
    public ModelAndView admin(ModelAndView modelAndView) {
        modelAndView.setViewName("admin");
        modelAndView.addObject("viewConfig", viewConfigService.getViewConfig());
        return modelAndView;
    }

    @GetMapping("/install")
    public ModelAndView install(ModelAndView modelAndView) {
        modelAndView.setViewName("install");
        return modelAndView;
    }
}
