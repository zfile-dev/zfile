package im.zhaojun.zfile.model.entity.webdav;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * WebDav文件夹实体
 *
 * @author me
 * @date 2022/4/9
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WebDavFolder extends WebDavEntity {
    public WebDavFolder(String folderName, WebDavFolder parent) {
        super(folderName, parent);
    }

    public WebDavFolder(String folderName, Integer driveId) {
        super(folderName, null);
        setDriveId(driveId);
    }

    public WebDavFolder(UUID id, String name, Date createdDate, Date modifiedDate, WebDavFolder parent) {
        super(id, name, createdDate, modifiedDate, parent);
    }

    public WebDavFile addFile(String fileName, Long size) {
        WebDavFile file = new WebDavFile(fileName, size, this);
        file.setDirectory(false);
        return file;
    }

    public WebDavFolder addFolder(String folderName) {
        return new WebDavFolder(folderName, this);
    }

}
