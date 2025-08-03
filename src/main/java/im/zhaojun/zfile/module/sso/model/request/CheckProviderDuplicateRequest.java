package im.zhaojun.zfile.module.sso.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CheckProviderDuplicateRequest {

    @Schema(title="id")
    private Integer id;

    @Schema(title="提供商")
    private String provider;

}
