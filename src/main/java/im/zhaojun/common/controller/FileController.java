package im.zhaojun.common.controller;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.annotation.CheckStorageStrategyInit;
import im.zhaojun.common.exception.SearchDisableException;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.ZFileConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.ResultBean;
import im.zhaojun.common.model.dto.SiteConfigDTO;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileAsyncCacheService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.service.SystemConfigService;
import im.zhaojun.common.service.SystemService;
import im.zhaojun.common.util.AudioHelper;
import im.zhaojun.common.util.FileComparator;
import im.zhaojun.common.util.HttpUtil;
import im.zhaojun.common.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 前台文件管理
 * @author zhaojun
 */
@RequestMapping("/api")
@RestController
public class FileController {

    @Resource
    private SystemService systemService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    /**
     * 滚动加载每页条数.
     */
    private static final Integer PAGE_SIZE = 30;

    @CheckStorageStrategyInit
    @GetMapping("/list")
    public ResultBean list(@RequestParam(defaultValue = "/") String path,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String order,
                           @RequestParam(required = false) String password,
                           @RequestParam(defaultValue = "1") Integer page) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        List<FileItemDTO> fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator("/" + URLUtil.decode(path) + "/"));
        for (FileItemDTO fileItemDTO : fileItemList) {
            if (ZFileConstant.PASSWORD_FILE_NAME.equals(fileItemDTO.getName())
                && !HttpUtil.getTextContent(fileItemDTO.getUrl()).equals(password)) {
                if (password != null && !"".equals(password)) {
                    return ResultBean.error("密码错误.");
                }
                return ResultBean.error("此文件夹需要密码.", ResultBean.REQUIRED_PASSWORD);
            }
        }

        // 排序, 先按照文件类型比较, 文件夹在前, 文件在后, 然后根据 sortBy 字段排序, 默认为升序;
        fileItemList.sort(new FileComparator(sortBy, order));
        filterFileList(fileItemList);

        int total = fileItemList.size();
        int totalPage = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        if (page > totalPage) {
            return ResultBean.successData(new ArrayList<>());
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = page * PAGE_SIZE;
        end = Math.min(end, total);
        List<FileItemDTO> fileSubItem = new ArrayList<>(fileItemList.subList(start, end));
        return ResultBean.successData(fileSubItem);
    }

    /**
     * 获取文件类容, 仅限用于, txt, md, ini 等普通文本文件.
     * @param url   文件路径
     * @return       文件内容
     */
    @CheckStorageStrategyInit
    @GetMapping("/content")
    public ResultBean getContent(String url) {
        return ResultBean.successData(HttpUtil.getTextContent(url));
    }

    /**
     * 获取系统配置信息和当前页的标题, 文件头, 文件尾信息
     * @param path          路径
     */
    @CheckStorageStrategyInit
    @GetMapping("/config")
    public ResultBean getConfig(String path) throws Exception {
        SiteConfigDTO config = systemService.getConfig(URLUtil.decode(StringUtils.removeDuplicateSeparator("/" + path + "/")));
        config.setSystemConfigDTO(systemConfigService.getSystemConfig());
        return ResultBean.successData(config);
    }

    @CheckStorageStrategyInit
    @GetMapping("/clearCache")
    public ResultBean clearCache() throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        if (fileService != null) {
            fileService.clearCache();
        }
        return ResultBean.success();
    }

    @CheckStorageStrategyInit
    @GetMapping("/audioInfo")
    public ResultBean getAudioInfo(String url) throws Exception {
        return ResultBean.success(AudioHelper.getAudioInfo(url));
    }

    @CheckStorageStrategyInit
    @GetMapping("/search")
    public ResultBean search(@RequestParam(value = "name", defaultValue = "/") String name) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if (!systemConfigDTO.getSearchEnable()) {
            throw new SearchDisableException("搜索功能未开启");
        }
        if (!fileAsyncCacheService.isCacheFinish()) {
            throw new SearchDisableException("搜索功能缓存预热中, 请稍后再试");
        }
        return ResultBean.success(fileService.search(URLUtil.decode(name)));
    }

    @GetMapping("/form")
    public ResultBean getFormByStorageType(String storageType) {
        StorageTypeEnum storageTypeEnum = StorageTypeEnum.getEnum(storageType);
        List<StorageConfig> storageConfigList = storageConfigService.selectStorageConfigByType(storageTypeEnum);
        storageConfigList.forEach(storageConfig -> storageConfig.setValue(null));
        return ResultBean.success(storageConfigList);
    }

    /**
     * 过滤文件列表, 不显示密码, 头部和尾部文件.
     */
    private void filterFileList(List<FileItemDTO> fileItemList) {
        if (fileItemList == null) {
            return;
        }

        fileItemList.removeIf(fileItem -> ZFileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())
                || ZFileConstant.FOOTER_FILE_NAME.equals(fileItem.getName())
                || ZFileConstant.HEADER_FILE_NAME.equals(fileItem.getName()));
    }

}
