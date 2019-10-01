package im.zhaojun.common.service;

import im.zhaojun.common.constant.ZfileConstant;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.SiteConfig;
import im.zhaojun.common.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemService {

    @Resource
    private ViewConfigService viewConfigService;

    /**
     * 构建指定路径下标题, 页头, 页尾
     * @param path          路径
     */
    public SiteConfig getConfig(String path) throws Exception {

        SiteConfig siteConfig = new SiteConfig();
        FileService fileService = viewConfigService.getCurrentFileService();

        List<FileItem> fileItemList = fileService.fileList(path);
        path = StringUtils.removeLastSeparator(path);
        for (FileItem fileItem : fileItemList) {
            if (ZfileConstant.README_FILE_NAME.equalsIgnoreCase(fileItem.getName())) {
                siteConfig.setFooter(fileService.getTextContent(path + "/" + fileItem.getName()));
            } else if (ZfileConstant.HEADER_FILE_NAME.equalsIgnoreCase(fileItem.getName())) {
                siteConfig.setHeader(fileService.getTextContent(path + "/" + fileItem.getName()));
            }
        }
        return siteConfig;
    }

}
