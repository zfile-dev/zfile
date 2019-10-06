package im.zhaojun.common.controller;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.constant.ZfileConstant;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.exception.SearchDisableException;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ResultBean;
import im.zhaojun.common.model.SiteConfig;
import im.zhaojun.common.model.ViewConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.SystemService;
import im.zhaojun.common.service.ViewConfigService;
import im.zhaojun.common.util.FileComparator;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api")
@RestController
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private FileService fileService;

    @Resource
    private SystemService systemService;

    @Resource
    private ViewConfigService viewConfigService;

    public static final Integer PAGE_SIZE = 20;

    @GetMapping("/list")
    public ResultBean list(@RequestParam(defaultValue = "/") String path,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String order,
                           @RequestParam(required = false) String password,
                           @RequestParam(defaultValue = "1") Integer page) throws Exception {
        List<FileItem> fileItems = fileService.fileList(StringUtils.removeDuplicateSeparator("/" + URLUtil.decode(path)));

        for (FileItem fileItem : fileItems) {
            if (ZfileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())) {
                if (!fileService.getTextContent(fileItem.getUrl()).equals(password)) {
                    if (password != null && !"".equals(password)) {
                        return ResultBean.error("密码错误.");
                    }
                    return ResultBean.error("此文件夹需要密码.", ResultBean.REQUIRED_PASSWORD);
                }
            }
        }

        // 排序, 先按照文件类型比较, 文件夹在前, 文件在后, 然后根据 sortBy 字段排序, 默认为升序;
        fileItems.sort(new FileComparator(sortBy, order));
        filterFileList(fileItems);

        Integer total = fileItems.size();
        Integer totalPage = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        if (page > totalPage) {
            return ResultBean.successData(new ArrayList<>());
        }

        Integer start = (page - 1) * PAGE_SIZE;
        Integer end = page * PAGE_SIZE;
        end = end > total ? total : end;
        List<FileItem> fileSubItem = fileItems.subList(start, end);
        return ResultBean.successData(fileSubItem);
    }

    /**
     * 获取文件类容, 仅限用于, txt, md, ini 等普通文本文件.
     * @param url   文件路径
     * @return       文件内容
     */
    @GetMapping("/content")
    public ResultBean getContent(String url) throws Exception {
        return ResultBean.successData(fileService.getTextContent(url));
    }

    /**
     * 获取系统配置信息和当前页的标题, 文件头, 文件尾信息
     * @param path          路径
     */
    @GetMapping("/config")
    public ResultBean getConfig(String path) throws Exception {
        SiteConfig config = systemService.getConfig(URLUtil.decode(StringUtils.removeDuplicateSeparator("/" + path + "/")));
        config.setViewConfig(viewConfigService.getViewConfig());
        return ResultBean.successData(config);
    }

    /**
     * 更新存储策略, 使用 @PostConstruct 注解, 以用于第一次启动时, 根据数据库的配置值, 读取默认的存储策略.
     */
    @PostConstruct
    @GetMapping("/updateStorageStrategy")
    public ResultBean updateConfig() {
        ViewConfig viewConfig = viewConfigService.getViewConfig();
        StorageTypeEnum storageStrategy = viewConfig.getStorageStrategy();
        fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);
        log.info("当前启用存储类型: {}", storageStrategy.getDescription());
//        new Thread(() -> {
//            log.info("缓存 {} 所有文件开始", storageStrategy.getDescription());
//            long startTime = System.currentTimeMillis();
//            try {
//                fileService.selectAllFileList();
//            } catch (Exception e) {
//                log.error("缓存所有文件失败", e);
//                e.printStackTrace();
//            }
//            long endTime = System.currentTimeMillis();
//            log.info("缓存 {} 所有文件结束, 用时: {} 秒", storageStrategy.getDescription(), ( (endTime - startTime) / 1000 ));
//        }).start();
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
    public ResultBean search(@RequestParam(value = "name", defaultValue = "/") String name) throws Exception {
        ViewConfig viewConfig = viewConfigService.getViewConfig();
        if (!viewConfig.getSearchEnable()) {
            throw new SearchDisableException("搜索功能未开启");
        }
        return ResultBean.success(fileService.search(URLUtil.decode(name)));
    }


    /**
     * 过滤文件列表, 不显示密码, 头部和尾部文件.
     */
    private void filterFileList(List<FileItem> fileItemList) {
        if (fileItemList == null) {
            return;
        }

        fileItemList.removeIf(fileItem -> ZfileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())
                || ZfileConstant.FOOTER_FILE_NAME.equals(fileItem.getName())
                || ZfileConstant.HEADER_FILE_NAME.equals(fileItem.getName()));
    }
}
