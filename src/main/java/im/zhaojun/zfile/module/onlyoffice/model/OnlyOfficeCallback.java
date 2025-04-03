package im.zhaojun.zfile.module.onlyoffice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class OnlyOfficeCallback {

    /**
     * 定义编辑的文档标识符。
     */
    private String key;

    /**
     * 定义文档的状态。 可以有以下值：
     * 1 - 正在编辑文档，
     * 2 - 文档已准备好保存，
     * 3 - 发生文档保存错误，
     * 4 - 文档已关闭，没有任何更改，
     * 6 - 正在编辑文档，但保存了当前文档状态，
     * 7 - 强制保存文档时发生错误。
     */
    private int status;

    /**
     * 定义已编辑的要由文档存储服务保存的文档的链接。 仅当 status 值等于 2, 3, 6 或 7 时，链接才存在。
     */
    private String url;

    /**
     * 定义有文档更改历史的对象。 仅当 status 值等于 2 或 3 时，对象才存在。
     * 它包含对象 changes 和 serverVersion，它们必须作为对象的属性 changes 和 serverVersion 以参数形式发送给 refreshHistory 方法。
     */
    private Map<String, Object> history;

    /**
     * 定义打开文档进行编辑的用户的标识符列表；
     * 当文档被更改时，用户将返回最后编辑文档的用户的标识符（对于 status 2 和 status 6 的应答）。
     */
    private List<String> users;

    /**
     * 定义当用户对文档执行操作时接收到的对象。type 字段值可以具有以下值：
     * 0 - 用户断开与文档共同编辑的连接，
     * 1 - 新用户连接到文档共同编辑，
     * 2 - 用户单击 强制保存按钮。
     * userid 字段值是用户标识符。
     */
    private List<Action> actions;

    /**
     * 定义文档的最后保存日期和时间。
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date lastsave;

    /**
     * 文档是否未被修改。
     */
    private boolean notmodified;

    /**
     * JWT 令牌，用于验证用户。
     */
    private String token;

    /**
     * 定义从 url 参数指定的链接下载文档的扩展名。 文件类型默认为 OOXML，但如果启用了 assemblyFormatAsOrigin 服务器设置，则文件将以原始格式保存。
     */
    private String filetype;

    // Inner class for actions
    @Data
    public static class Action {

        /**
         * 定义当用户对文档执行操作时接收到的对象。type 字段值可以具有以下值：
         * 0 - 用户断开与文档共同编辑的连接，
         * 1 - 新用户连接到文档共同编辑，
         * 2 - 用户单击 强制保存按钮。
         * userid 字段值是用户标识符。
         */
        private int type;

        private String userid;

    }
}