package im.zhaojun.zfile.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(defaultValue = "false")
    private Boolean available;

}
