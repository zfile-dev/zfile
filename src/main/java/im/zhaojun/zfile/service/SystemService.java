// package im.zhaojun.zfile.service;
//
// import im.zhaojun.zfile.model.constant.ZFileConstant;
// import im.zhaojun.zfile.model.dto.FileItemDTO;
// import im.zhaojun.zfile.model.dto.SiteConfigDTO;
// import im.zhaojun.zfile.model.enums.StorageTypeEnum;
// import im.zhaojun.zfile.service.base.AbstractBaseFileService;
// import im.zhaojun.zfile.context.DriveContext;
// import im.zhaojun.zfile.util.HttpUtil;
// import im.zhaojun.zfile.util.StringUtils;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.HttpClientErrorException;
//
// import javax.annotation.Resource;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;
//
// /**
//  * @author zhaojun
//  */
// @Slf4j
// @Service
// public class SystemService {
//
//     @Resource
//     private DriveContext driveContext;
//
//     /**
//      * 构建指定路径下标题, 页面文档信息
//      * @param path          路径
//      */
//     public SiteConfigDTO getConfig(Integer driveId, String path) throws Exception {
//
//         SiteConfigDTO siteConfigDTO = new SiteConfigDTO();
//
//         AbstractBaseFileService fileService = driveContext.getDriveService(driveId);
//
//         List<FileItemDTO> fileItemList;
//
//         if (Objects.equals(fileService.getStorageTypeEnum(), StorageTypeEnum.FTP)) {
//             fileItemList = new ArrayList<>();
//         } else {
//             fileItemList = fileService.fileList(path);
//         }
//
//         for (FileItemDTO fileItemDTO : fileItemList) {
//             if (ZFileConstant.README_FILE_NAME.equalsIgnoreCase(fileItemDTO.getName())) {
//                 String textContent = null;
//                 try {
//                     textContent = HttpUtil.getTextContent(fileItemDTO.getUrl());
//                 } catch (HttpClientErrorException httpClientErrorException) {
//                     log.debug("尝试重新获取文档区缓存中链接后仍失败", httpClientErrorException);
//                     try {
//                         String fullPath = StringUtils.removeDuplicateSeparator(fileItemDTO.getPath() + "/" + fileItemDTO.getName());
//                         FileItemDTO fileItem = fileService.getFileItem(fullPath);
//                         textContent = HttpUtil.getTextContent(fileItem.getUrl());
//                     } catch (Exception e) {
//                         log.debug("尝试重新获取文档区链接后仍失败, 已置为空", e);
//                     }
//                 }
//                 siteConfigDTO.setReadme(textContent);
//             }
//         }
//         return siteConfigDTO;
//     }
//
// }
