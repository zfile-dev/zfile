package im.zhaojun.common.service;

import im.zhaojun.common.model.constant.ZFileConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SiteConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.HttpUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
public class SystemService {

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 构建指定路径下标题, 页面文档信息
     * @param path          路径
     */
    public SiteConfigDTO getConfig(String path) throws Exception {

        SiteConfigDTO siteConfigDTO = new SiteConfigDTO();
        AbstractFileService fileService = systemConfigService.getCurrentFileService();

        List<FileItemDTO> fileItemList;

        if (Objects.equals(systemConfigService.getSystemConfig().getStorageStrategy(), StorageTypeEnum.FTP)) {
            fileItemList = new ArrayList<>();
        } else {
            fileItemList = fileService.fileList(path);
        }

        for (FileItemDTO fileItemDTO : fileItemList) {
            if (ZFileConstant.README_FILE_NAME.equalsIgnoreCase(fileItemDTO.getName())) {
                siteConfigDTO.setReadme(HttpUtil.getTextContent(fileItemDTO.getUrl()));
            }
        }
        return siteConfigDTO;
    }

}
