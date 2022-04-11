package im.zhaojun.zfile.model.entity.webdav;

import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.util.StringUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * WebDav实体
 *
 * @author me
 * @date 2022/4/9
 */
@Data
public class WebDavEntity {
    /**
     * 唯一ID
     */
    private UUID id;
    /**
     * 驱动器ID
     */
    private Integer driveId;
    /**
     * 名称
     */
    private String name;
    /**
     * 创建时间
     */
    private Date createdDate;
    /**
     * 修改时间
     */
    private Date modifiedDate;
    /**
     * 是否是目录
     */
    private boolean isDirectory;
    /**
     * 父文件夹
     */
    private WebDavFolder parent;

    public WebDavEntity() {
    }

    public WebDavEntity(String name, WebDavFolder parent) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.parent = parent;
        this.createdDate = new Date();
        this.modifiedDate = new Date();
        this.isDirectory = true;
    }

    public WebDavEntity(UUID id, String name, Date createdDate, Date modifiedDate,
                        WebDavFolder parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.isDirectory = true;
    }

    /**
     * 获取全路径
     */
    public String getFullPath() {
        if (this.getParent() != null) {
            final String parentFullPath = this.getParent().getFullPath();
            return StringUtils.removeDuplicateSeparator(parentFullPath + ZFileConstant.PATH_SEPARATOR + this.getName());
        } else {
            return ZFileConstant.PATH_SEPARATOR;
        }
    }

    public static List<WebDavEntity> convertFromFileItemDTO(List<FileItemDTO> fileItemList, WebDavFolder parent) {
        List<WebDavEntity> result = new ArrayList<>();
        if (fileItemList == null || fileItemList.size() == 0) {
            return result;
        }
        for (FileItemDTO each : fileItemList) {
            WebDavEntity entity = convertFromFileItemDTO(each, parent);
            result.add(entity);
        }
        return result;
    }

    public static WebDavEntity convertFromFileItemDTO(FileItemDTO fileItemDTO, WebDavFolder parent) {
        if (fileItemDTO == null) {
            return null;
        }
        WebDavEntity entity;
        if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
            entity = new WebDavFolder(fileItemDTO.getName(), parent);
        } else {
            entity = new WebDavFile(fileItemDTO.getName(), fileItemDTO.getSize(), parent);
        }
        entity.setModifiedDate(fileItemDTO.getTime());
        entity.setDriveId(parent.getDriveId());
        return entity;
    }

}
