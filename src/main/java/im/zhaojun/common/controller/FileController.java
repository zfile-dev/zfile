package im.zhaojun.common.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ResultBean;
import im.zhaojun.common.model.SiteConfig;
import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.SystemConfigService;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestMapping("/api")
@RestController
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private FileService fileService;

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping("/list")
    public ResultBean list(String path, String sortBy, boolean descending) throws Exception {
        List<FileItem> fileItems = fileService.fileList(StringUtils.removeDuplicateSeparator("/" + URLUtil.decode(path)));

        // 排序, 先按照文件类型比较, 文件夹在前, 文件在后, 然后根据 sortBy 字段排序, 默认为升序;
        fileItems.sort((o1, o2) -> {
            FileTypeEnum o1Type = o1.getType();
            FileTypeEnum o2Type = o2.getType();

            if (o1Type.equals(o2Type)) {
                switch (sortBy) {
                    case "name": return o1.getName().compareTo(o2.getName());
                    case "time": return o1.getTime().compareTo(o2.getTime());
                    case "size": return o1.getSize().compareTo(o2.getSize());
                    default: return o1.getName().compareTo(o2.getName());
                }
            }

            if (o1Type.equals(FileTypeEnum.FOLDER)) {
                return -1;
            } else {
                return 1;
            }
        });

        if (descending) {
            Collections.reverse(fileItems);
        }

        return ResultBean.successData(fileItems);
    }

    /**
     * 获取下载链接
     * @param path          路径
     * @return              下载链接
     */
    @GetMapping("/downloadUrl")
    public ResultBean getDownloadUrl(String path) throws Exception {
        return ResultBean.successData(fileService.getDownloadUrl(URLUtil.decode(path)));
    }

    /**
     * 获取文件类容, 仅限用于, txt, md, ini 等普通文本文件.
     * @param path   文件路径
     * @return       文件内容
     */
    @GetMapping("/getContent")
    public ResultBean getContent(String path) throws Exception {
        return ResultBean.successData(fileService.getTextContent(path));
    }

    /**
     * 获取系统配置信息和当前页的标题, 文件头, 文件尾信息
     * @param path          路径
     */
    @GetMapping("/getConfig")
    public ResultBean getConfig(String path) throws Exception {
        SiteConfig config = fileService.getConfig(URLUtil.decode(StringUtils.removeDuplicateSeparator("/" + path + "/")));
        config.setSystemConfig(systemConfigService.getSystemConfig());
        return ResultBean.successData(config);
    }

    /**
     * 更新存储策略, 使用 @PostConstruct 注解, 以用于第一次启动时, 根据数据库的配置值, 读取默认的存储策略.
     */
    @PostConstruct
    @GetMapping("/updateStorageStrategy")
    public ResultBean updateConfig() {
        SystemConfig systemConfig = systemConfigService.getSystemConfig();
        StorageTypeEnum storageStrategy = systemConfig.getStorageStrategy();
        fileService = StorageTypeFactory.getTrafficMode(storageStrategy);
        log.info("当前启用存储类型: {}", storageStrategy.getDescription());
        initSearchCache();
        return ResultBean.success();
    }

    @GetMapping("/clearCache")
    public ResultBean clearCache() throws Exception {
        fileService.clearCache();
        return ResultBean.success();
    }

    @GetMapping("/getImageInfo")
    public ResultBean getImageInfo(String url) throws Exception {
        return ResultBean.success(fileService.getImageInfo(url));
    }

    @GetMapping("/audioInfo")
    public ResultBean getAudioInfo(String url) throws Exception {
        return ResultBean.success(fileService.getAudioInfo(url));
    }

    @GetMapping("/search")
    public ResultBean search(@RequestParam("path") String name) throws Exception {
        return ResultBean.success(fileService.search(name));
    }

    private void initSearchCache() {
        StorageTypeEnum storageStrategy = systemConfigService.getSystemConfig().getStorageStrategy();
        FileService fileService = StorageTypeFactory.getTrafficMode(storageStrategy);

        ThreadUtil.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                log.info("初始化 {} 文件列表", storageStrategy.getDescription());
                List<FileItem> fileItemList = fileService.selectAllFileList();
                long endTime = System.currentTimeMillis();
                log.info("完成 {} 缓存, 共缓存了 {} 个文件夹, 使用时间 {} 秒",
                        storageStrategy.getDescription(),
                        fileItemList.size(),
                        (endTime - startTime) / 1000);
            } catch (Exception e) {
                log.info("初始化 " + storageStrategy.getDescription() + " 文件列表异常", e);
            }
        });
    }
}
