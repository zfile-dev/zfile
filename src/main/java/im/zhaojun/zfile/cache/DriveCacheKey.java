package im.zhaojun.zfile.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriveCacheKey {

    private Integer driveId;

    private String key;

}