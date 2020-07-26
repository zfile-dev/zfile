package im.zhaojun.zfile.model.enums;

/**
 * @author zhaojun
 */
public enum FileTypeEnum {

    /**
     * 文件
     */
    FILE("File"),

    /**
     * 文件夹
     */
    FOLDER("Folder");

    private String value;

    FileTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}