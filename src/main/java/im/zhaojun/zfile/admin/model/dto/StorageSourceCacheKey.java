package im.zhaojun.zfile.admin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存对象，用户表示那个存储源的那个文件夹.
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageSourceCacheKey {

    private Integer storageId;

    private String key;

}