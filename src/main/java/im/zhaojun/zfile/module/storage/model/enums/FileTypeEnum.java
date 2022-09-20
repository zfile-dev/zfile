package im.zhaojun.zfile.module.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    /**
     * 文件
     */
    FILE("FILE"),

    /**
     * 文件夹
     */
    FOLDER("FOLDER");

    @EnumValue
    @JsonValue
    private final String value;

}