package im.zhaojun.zfile.module.share.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享条目类型
 */
@Schema(description = "分享条目类型")
@Getter
@AllArgsConstructor
public enum ShareEntryTypeEnum {

    @Schema(description = "文件")
    FILE("FILE"),

    @Schema(description = "文件夹")
    FOLDER("FOLDER");

    @EnumValue
    @JsonValue
    private final String value;

}
