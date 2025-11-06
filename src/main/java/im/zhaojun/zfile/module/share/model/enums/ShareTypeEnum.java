package im.zhaojun.zfile.module.share.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShareTypeEnum {

    FILE("FILE", "单个文件"),

    FOLDER("FOLDER", "文件夹"),

    MULTIPLE("MULTIPLE", "多个文件或文件夹");

    @EnumValue
    @JsonValue
    private final String value;

    private final String description;

}