package im.zhaojun.zfile.module.share.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import im.zhaojun.zfile.module.share.model.enums.ShareEntryTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分享条目 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "分享条目")
public class ShareEntryDTO {

    @Schema(title = "条目名称", example = "document.pdf")
    @NotBlank(message = "分享条目名称不能为空")
    private String name;

    @Schema(title = "条目类型", description = "FILE/FOLDER")
    @NotNull(message = "分享条目类型不能为空")
    private ShareEntryTypeEnum type;

}
