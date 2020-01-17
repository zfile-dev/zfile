package im.zhaojun.common.controller;

import im.zhaojun.common.model.dto.ResultBean;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.AudioHelper;
import im.zhaojun.common.util.HttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaojun
 * @date 2020/1/13 21:40
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @GetMapping("/support-strategy")
    public ResultBean supportStrategy() {
        return ResultBean.successData(StorageTypeEnum.values());
    }

    /**
     * 获取文件内容, 仅限用于, txt, md, ini 等普通文本文件.
     * @param url       文件路径
     * @return          文件内容
     */
    @GetMapping("/content")
    public ResultBean getContent(String url) {
        return ResultBean.successData(HttpUtil.getTextContent(url));
    }


    /**
     * 获取文件内容, 仅限用于, txt, md, ini 等普通文本文件.
     * @param url       文件路径
     * @return          文件内容
     */
    @GetMapping("/content/origin")
    public String getContentOrigin(String url) {
        return HttpUtil.getTextContent(url);
    }


    /**
     * 检测文件是否存在
     * @param url       文件路径
     * @return          是否存在
     */
    @GetMapping("/content/exist")
    public boolean checkFileExist(String url) {
        return HttpUtil.checkUrlExist(url);
    }


    /**
     * 获取音频文件信息
     * @param url       文件 URL
     * @return          音频信息, 标题封面等信息
     */
    @GetMapping("/audio-info")
    public ResultBean getAudioInfo(String url) throws Exception {
        return ResultBean.success(AudioHelper.getAudioInfo(url));
    }
}
