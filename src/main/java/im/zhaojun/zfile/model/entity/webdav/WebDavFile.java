package im.zhaojun.zfile.model.entity.webdav;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * WebDav文件实体
 *
 * @author me
 * @date 2022/4/9
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WebDavFile extends WebDavEntity {
    /**
     * 大小
     */
    private Long size;
    /**
     * 内容类型
     */
    private String contentType;

    public WebDavFile(String fileName, Long size, WebDavFolder parent) {
        super(fileName, parent);
        this.setSize(size);
        this.setDirectory(false);
    }

    public WebDavFile(UUID id, String name, Date createdDate, Date modifiedDate, WebDavFolder parent) {
        super(id, name, createdDate, modifiedDate, parent);
        this.setDirectory(false);
    }
}
