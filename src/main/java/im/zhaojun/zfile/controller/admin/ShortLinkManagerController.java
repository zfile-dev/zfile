package im.zhaojun.zfile.controller.admin;

import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.ShortLinkConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 直链管理 Controller
 *
 * @author zhaojun
 */
@Controller
@RequestMapping("/admin")
public class ShortLinkManagerController {

    @Resource
    private ShortLinkConfigService shortLinkConfigService;

    @GetMapping("/link/list")
    @ResponseBody
    public ResultBean list(String key,
                           String url,
                           String dateFrom,
                           String dateTo,
                           Integer page,
                           Integer limit,
                           @RequestParam(required = false, defaultValue = "createDate") String orderBy,
                           @RequestParam(required = false, defaultValue = "desc") String orderDirection) {
        return ResultBean.success(shortLinkConfigService.find(key, url, dateFrom, dateTo, page, limit, orderBy, orderDirection));
    }

    @GetMapping("/link/delete/{id}")
    @ResponseBody
    public ResultBean deleteById(@PathVariable Integer id) {
        shortLinkConfigService.deleteById(id);
        return ResultBean.success();
    }
}