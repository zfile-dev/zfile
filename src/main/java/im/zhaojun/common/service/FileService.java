package im.zhaojun.common.service;

import im.zhaojun.common.model.dto.FileItemDTO;

import java.util.List;

/**
 * @author zhaojun
 */
public interface FileService {

    /**
     * 获取指定路径下的文件及文件及
     * @param path 文件路径
     * @return     文件及文件夹列表
     */
    List<FileItemDTO> fileList(String path) throws Exception;

    /**
     * 获取文件下载地址
     * @param path  文件路径
     * @return      文件下载地址
     */
    String getDownloadUrl(String path);

}
