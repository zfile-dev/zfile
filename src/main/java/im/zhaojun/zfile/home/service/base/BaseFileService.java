package im.zhaojun.zfile.home.service.base;

import im.zhaojun.zfile.home.model.result.FileItemResult;

import java.util.List;

/**
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
     * 获取文件下载地址
     *
     * @param   pathAndName
     *          文件路径及文件名称
     *
     * @return  文件下载地址
     */
    String getDownloadUrl(String pathAndName);

}