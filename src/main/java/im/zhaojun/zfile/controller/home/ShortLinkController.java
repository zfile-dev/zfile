package im.zhaojun.zfile.controller.home;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.entity.ShortLinkConfig;
import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.ShortLinkConfigService;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 短链 Controller
 * @author zhao
 */
@Controller
public class ShortLinkController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ShortLinkConfigService shortLinkConfigService;

    @GetMapping("/api/short-link")
    @ResponseBody
    public ResultBean shortLink(String driveId, String path) {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String domain = systemConfig.getDomain();
        // 拼接直链地址.
        String fullPath = StringUtils.concatUrl(StringUtils.DELIMITER_STR, ZFileConstant.DIRECT_LINK_PREFIX, driveId, path);
        ShortLinkConfig shortLinkConfig = shortLinkConfigService.findByUrl(fullPath);

        if (shortLinkConfig == null) {

            String randomKey;
            do {
                // 获取短链
                randomKey = RandomUtil.randomString(6);
                shortLinkConfig = shortLinkConfigService.findByKey(randomKey);
            } while (shortLinkConfig != null);

            shortLinkConfig = new ShortLinkConfig();
            shortLinkConfig.setKey(randomKey);
            shortLinkConfig.setUrl(fullPath);
            shortLinkConfigService.save(shortLinkConfig);
        }


        String shortUrl = StringUtils.removeDuplicateSeparator(domain + "/s/" + shortLinkConfig.getKey());
        return ResultBean.successData(shortUrl);
    }

    @GetMapping("/s/{key}")
    public String parseShortKey(@PathVariable String key) {
        ShortLinkConfig shortLinkConfig = shortLinkConfigService.findByKey(key);
        if (shortLinkConfig == null) {
            throw new RuntimeException("此直链不存在或已失效.");
        }

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String domain = systemConfig.getDomain();

        String url = URLUtil.encode(StringUtils.removeDuplicateSeparator(domain + shortLinkConfig.getUrl()));
        return "redirect:" + url;
    }


    @GetMapping("admin/api/short-link/key")
    @ResponseBody
    public ResultBean updateShortKey(Integer id, String newKey) {
        ShortLinkConfig newShortLinkConfig = shortLinkConfigService.findByKey(newKey);
        if (newShortLinkConfig != null) {
            throw new RuntimeException("您输入的 Key 已存在，请重新输入");
        }

        ShortLinkConfig shortLinkConfig = shortLinkConfigService.findById(id);
        if (shortLinkConfig == null) {
            throw new RuntimeException("此直链不存在或已失效.");
        }

        shortLinkConfig.setKey(newKey);
        shortLinkConfigService.save(shortLinkConfig);
        return ResultBean.success();
    }

    /**
     * 批量删除直链
     */
    @DeleteMapping("admin/api/short-link")
    @ResponseBody
    public ResultBean batchDelete(@RequestParam("id[]") Integer[] ids) {
        for (Integer id : ids) {
            shortLinkConfigService.deleteById(id);
        }
        return ResultBean.success();
    }
}
