package im.zhaojun.zfile.controller.home;

import com.alibaba.fastjson.JSON;
import im.zhaojun.zfile.config.ApplicationContextConfigure;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.entity.webdav.WebDavEntity;
import im.zhaojun.zfile.model.entity.webdav.WebDavFile;
import im.zhaojun.zfile.model.entity.webdav.WebDavFolder;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.RegexMatchUtils;
import io.milton.annotations.*;
import io.milton.http.HttpManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * WebDav控制器
 *
 * @author me
 * @date 2022/4/9
 */
@Slf4j
@ResourceController
@ConditionalOnProperty(value = "webdav.enable", havingValue = "true")
public class WebDavController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDavController.class);


    /**
     * 获取根目录文件夹
     *
     * @return {@link WebDavFolder} WebDav文件夹
     */
    @Root
    public WebDavFolder getRootFolder() {
        return new WebDavFolder(ZFileConstant.PATH_SEPARATOR, getDriveId());
    }

    /**
     * 获取根目录子文件/文件夹(控制器)
     *
     * @param rootFolder 根文件夹
     * @return {@link WebDavController} 根控制器
     */
    @ChildrenOf
    public WebDavController getChildren(WebDavController rootFolder) {
        return this;
    }

    /**
     * 获取子文件/文件夹
     *
     * @param parent 父文件夹
     * @return {@link List}<{@link WebDavEntity}> WebDav实体
     */
    @ChildrenOf
    public List<WebDavEntity> getChildren(WebDavFolder parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
        try {
            // 获取驱动器文件服务
            AbstractBaseFileService fileService = ApplicationContextConfigure.getBean(DriveContext.class).get(parent.getDriveId());
            if (fileService == null) {
                return Collections.emptyList();
            }
            // 获取文件列表
            List<FileItemDTO> fileItemList = fileService.fileList(parent.getFullPath());
            // 转换FileItemDTO为WebDavEntity
            return WebDavEntity.convertFromFileItemDTO(fileItemList, parent);
        } catch (Exception e) {
            LOGGER.warn("get webDav children failed,parent:{},msg:{}", JSON.toJSONString(parent), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取子文件内容
     *
     * @param webDavFile WebDav文件
     * @return {@link String} ViewResolver模板名称
     */
    @Get
    public String getChild(WebDavFile webDavFile) {
        return JSON.toJSONString(webDavFile);
    }

    /**
     * 获取WebDav实体文件名
     */
    @Name
    public String getWebDavFile(WebDavEntity webDavEntity) {
        return webDavEntity.getName();
    }

    /**
     * 获取WebDav实体展示名称
     */
    @DisplayName
    public String getDisplayName(WebDavEntity webDavEntity) {
        return webDavEntity.getName();
    }

    /**
     * 获取WebDav实体唯一id
     */
    @UniqueId
    public String getUniqueId(WebDavEntity entity) {
        return entity.getId().toString();
    }

    /**
     * 获取WebDav实体修改日期
     */
    @ModifiedDate
    public Date getModifiedDate(WebDavEntity webDavEntity) {
        return webDavEntity.getModifiedDate();
    }

    /**
     * 获取WebDav实体创建日期
     */
    @CreatedDate
    public Date getCreatedDate(WebDavEntity webDavEntity) {
        return webDavEntity.getCreatedDate();
    }

    /**
     * 获取WebDav实体大小
     */
    @ContentLength
    public Long getContentLength(WebDavEntity entity) {
        if (entity instanceof WebDavFile) {
            return ((WebDavFile) entity).getSize();
        }
        // 性能考虑,文件夹暂不进行大小统计
        return null;
    }


    /**
     * 获取驱动器id
     *
     * @return {@link Integer}
     */
    private Integer getDriveId() {
        String requestUrl = HttpManager.decodeUrl(HttpManager.request().getAbsolutePath());
        final String driveId = RegexMatchUtils.matchByIndex("^" + ZFileConstant.WEB_DAV_PREFIX + "/(\\d+)(.*)", requestUrl, 1);
        return driveId != null ? Integer.valueOf(driveId) : null;
    }
}