package im.zhaojun.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhaojun
 */
@Data
@ToString
public class SiteConfigDTO implements Serializable {

    private static final long serialVersionUID = 8811196207046121740L;

    private String readme;

    @JsonProperty("viewConfig")
    private SystemConfigDTO systemConfigDTO;

}
