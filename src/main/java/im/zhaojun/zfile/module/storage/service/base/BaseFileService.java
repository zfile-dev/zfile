package im.zhaojun.zfile.module.storage.service.base;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;

import java.util.List;

/**
 * 基础文件服务接口，定义了了一些通用方法定义
 *
 * @author zhaojun
 */
public interface BaseFileService {

    /***
     * 获取指定路径下的文件及文件夹
     *
     * @param   folderPath
     *          文件夹路径
     *
     * @return  文件及文件夹列表
     *
     * @throws  Exception  获取文件列表中出现的异常
     */
    List<FileItemResult> fileList(String folderPath) throws Exception;

    /**
     * 获取单个文件信息
     *
     * @param   pathAndName
     *          文件路径及文件名称
     *
     * @return  单个文件的内容.
     */
    FileItemResult getFileItem(String pathAndName);

    /**
     * 创建新文件夹
     *
     * @param   path
     *          文件夹路径
     *
     * @param   name
     *          文件夹名称
     *
     * @return  是否创建成功
     */
    boolean newFolder(String path, String name);

    /**
     * 删除文件
     *
     * @param   path
     *          文件路径
     *
     * @param   name
     *          文件名称
     *
     * @return  是否删除成功
     */
    boolean deleteFile(String path, String name);

    /**
     * 删除文件夹
     *
     * @param   path
     *          文件夹路径
     *
     * @param   name
     *          文件夹名称
     *
     * @return  是否删除成功
     */
    boolean deleteFolder(String path, String name);

    /**
     * 复制文件
     *
     * @param   path
     *          文件路径
     *
     * @param   name
     *          文件名称
     *
     * @param   targetPath
     *          目标文件路径
     *
     * @param   targetName
     *          目标文件名称
     *
     * @return  是否复制成功
     */
    boolean copyFile(String path, String name, String targetPath, String targetName);

    /**
     * 复制文件夹
     *
     * @param   path
     *          文件夹路径
     *
     * @param   name
     *          文件夹名称
     *
     * @param   targetPath
     *          目标文件夹路径
     *
     * @param   targetName
     *          目标文件夹名称
     *
     * @return  是否复制成功
     */
    boolean copyFolder(String path, String name, String targetPath, String targetName);

    /**
     * 移动文件
     *
     * @param   path
     *          文件路径
     *
     * @param   name
     *          文件名称
     *
     * @param   targetPath
     *          目标文件路径
     *
     * @param   targetName
     *          目标文件名称
     *
     * @return  是否移动成功
     */
    boolean moveFile(String path, String name, String targetPath, String targetName);

    /**
     * 移动文件夹
     *
     * @param   path
     *          文件夹路径
     *
     * @param   name
     *          文件夹名称
     *
     * @param   targetPath
     *          目标文件夹路径
     *
     * @param   targetName
     *          目标文件夹名称
     *
     * @return  是否移动成功
     */
    boolean moveFolder(String path, String name, String targetPath, String targetName);

    /**
     * 重命名文件
     *
     * @param   path
     *          文件路径
     *
     * @param   name
     *          文件名称
     *
     * @param   newName
     *          新文件名称
     *
     * @return  是否重命名成功
     */
    boolean renameFile(String path, String name, String newName);

    /**
     * 重命名文件夹
     *
     * @param   path
     *          文件夹路径
     *
     * @param   name
     *          文件夹名称
     *
     * @param   newName
     *          新文件夹名称
     *
     * @return  是否重命名成功
     */
    boolean renameFolder(String path, String name, String newName);

    /**
     * 获取文件上传地址
     *
     * @param   path
     *          文件路径
     *
     * @param   name
     *          文件名称
     *
     * @param   size
     *          文件大小
     *
     * @return  文件上传地址
     */
    String getUploadUrl(String path, String name, Long size);

    /**
     * 获取文件下载地址
     *
     * @param   pathAndName
     *          文件路径及文件名称
     *
     * @return  文件下载地址
     */
    String getDownloadUrl(String pathAndName);

    /**
     * 获取存储源类型
     *
     * @return  存储源类型
     */
    StorageTypeEnum getStorageTypeEnum();

}