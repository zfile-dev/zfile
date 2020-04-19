package im.zhaojun.zfile.controller;

import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.util.AudioHelper;
import im.zhaojun.zfile.util.HttpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共 Controller
 * @author zhaojun
 */
@RestController
@RequestMapping("/common")
public class CommonController {


    /**
     * 返回系统支持的所有存储策略
     *
     * @return 存储策略
     */
    @GetMapping("/support-strategy")
    public ResultBean supportStrategy() {
        return ResultBean.successData(StorageTypeEnum.values());
    }


    /**
     * 获取文件内容, 仅限用于 txt, md, ini 等普通文本文件.
     *
     * @param   url
     *          文件路径
     *
     * @return  文件内容
     */
    @GetMapping("/content")
    public ResultBean getContent(String url) {
        return ResultBean.successData(HttpUtil.getTextContent(url));
    }


    /**
     * 获取音频文件信息
     *
     * @param   url
     *          文件 URL
     *
     * @return 音频信息, 标题封面等信息
     */
    @GetMapping("/audio-info")
    public ResultBean getAudioInfo(String url) throws Exception {
        return ResultBean.success(AudioHelper.getAudioInfo(url));
    }

}