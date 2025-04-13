package im.zhaojun.zfile.module.storage.model.bo;

import lombok.Data;

/**
 * 存储源的元数据配置，用于指示页面功能，下面给的默认值是常用默认值，所以仅需配置与默认不同的参数。
 */
@Data
public class StorageSourceMetadata {

    /**
     * 上传使用的类型
     */
    private UploadType uploadType;

    /**
     * 是否支持重命名文件夹
     */
    private boolean supportRenameFolder = true;

    /**
     * 是否支持移动文件夹
     */
    private boolean supportMoveFolder = true;

    /**
     * 是否支持复制文件夹
     */
    private boolean supportCopyFolder = true;

    /**
     * 是否支持删除非空文件夹
     */
    private boolean supportDeleteNotEmptyFolder = true;

    /**
     * 是否需要在上传文件前创建文件夹
     */
    private boolean needCreateFolderBeforeUpload = true;

    public enum UploadType {

        /**
         * 微软系上传，onedrive, sharepoint，包含国际版、国内版
         */
        MICROSOFT,

        /**
         * 使用 ZFile 服务端中转传输
         */
        PROXY,

        /**
         * 亚马逊 S3 上传
         */
        S3,

        /**
         * 又拍云上传
         */
        UPYUN


    }

}
