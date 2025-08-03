package im.zhaojun.zfile.module.storage.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author zhaojun
 */
@Data
@Schema(title="gd drive 列表请求类")
public class GetGoogleDriveListRequest {
	
	@NotBlank(message = "accessToken 不能为空")
	@Schema(title = "accessToken", requiredMode = Schema.RequiredMode.REQUIRED, example = "v7LtfjIbnxLCTj0R3riwhyxcbv4KVH5HuPWHWrrewHMEwjJyUlYXV6D4m1MLJ2dP__GX_7CKCc-HudUetPXWS2wwbfkNs6ydLq3xrk1gHA7wcD_pmt6oNuRXw5mnFzfdLkH5wIG1suQp3p0eHJurzIaCgYKATASATASFQE65dr8hO725r41QtZc9RJVUg12cA0163")
	private String accessToken;
	
}