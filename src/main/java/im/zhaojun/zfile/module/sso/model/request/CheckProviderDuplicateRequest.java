package im.zhaojun.zfile.module.sso.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CheckProviderDuplicateRequest {

    @Schema(name="id")
    private Integer id;

    @Schema(name="提供商")
    private String provider;

}
