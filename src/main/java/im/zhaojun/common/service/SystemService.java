package im.zhaojun.common.service;

import im.zhaojun.common.model.constant.ZFileConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SiteConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.HttpUtil;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Slf4j
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
                String textContent = null;
                try {
                    textContent = HttpUtil.getTextContent(fileItemDTO.getUrl());
                } catch (HttpClientErrorException httpClientErrorException) {
                    log.debug("尝试重新获取文档区缓存中链接后仍失败", httpClientErrorException);
                    try {
                        String fullPath = StringUtils.removeDuplicateSeparator(fileItemDTO.getPath() + "/" + fileItemDTO.getName());
                        FileItemDTO fileItem = fileService.getFileItem(fullPath);
                        textContent = HttpUtil.getTextContent(fileItem.getUrl());
                    } catch (Exception e) {
                        log.debug("尝试重新获取文档区链接后仍失败, 已置为空", e);
                    }
                }
                siteConfigDTO.setReadme(textContent);
            }
        }
        return siteConfigDTO;
    }

}
