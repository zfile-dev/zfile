package im.zhaojun.common.enums;

public enum FileTypeEnum {
    FILE("File"), FOLDER("Folder");

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