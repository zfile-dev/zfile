package im.zhaojun.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zhao Jun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageStrategyDTO {

    private String key;

    private String description;

    private boolean available;

}
