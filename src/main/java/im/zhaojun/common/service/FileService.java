package im.zhaojun.common.service;

import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojun
 */
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public interface FileService {

    /**
     * 获取指定路径下的文件及文件及
     * @param path 文件路径
     * @return     文件及文件夹列表
     * @throws Exception    获取时可能抛出的任何异常, 如 key 异常, 网络超时, 路径不存在等问题.
     */
    List<FileItemDTO> fileList(String path) throws Exception;

    /**
     * 获取文件下载地址
     * @param path  文件路径
     * @return      文件下载地址
     * @throws Exception    生成下载地址异常
     */
    String getDownloadUrl(String path) throws Exception;

    /**
     * 初始化方法, 启动时自动调用实现类的此方法进行初始化.
     */
    @PostConstruct
    default void init() {}

    /**
     * 清除缓存.
     */
    @CacheEvict(allEntries = true)
    default void clearCache() {}

    /**
     * 搜索文件
     * @param name 文件名
     * @return      包含该文件名的所有文件或文件夹
     * @throws Exception    搜索过程出现的异常
     */
    default List<FileItemDTO> search(String name) throws Exception {
        List<FileItemDTO> result = new ArrayList<>();

        List<FileItemDTO> fileItemList = selectAllFileList();
        for (FileItemDTO fileItemDTO : fileItemList) {
            if (fileItemDTO.getName().contains(name)) {
                result.add(fileItemDTO);
            }
        }

        return result;
    }

    /**
     * 查询所有文件
     * @return      所有文件
     * @throws Exception    异常现象
     */
    default List<FileItemDTO> selectAllFileList() throws Exception {
        List<FileItemDTO> result = new ArrayList<>();

        String path = "/";

        FileService currentFileService = (FileService) AopContext.currentProxy();
        List<FileItemDTO> fileItemList = currentFileService.fileList(path);
        ArrayDeque<FileItemDTO> queue = new ArrayDeque<>(fileItemList);

        while (!queue.isEmpty()) {
            FileItemDTO fileItemDTO = queue.pop();
            result.add(fileItemDTO);
            if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
                String filePath = StringUtils.removeDuplicateSeparator("/" + fileItemDTO.getPath() + "/" + fileItemDTO.getName() + "/");
                queue.addAll(currentFileService.fileList(filePath));
            }
        }

        return result;
    }

    /**
     * 获取存储引擎类型
     * @return  存储引擎类型枚举
     */
    StorageTypeEnum getStorageTypeEnum();

    /**
     * 获取是否初始化成功
     * @return  初始化成功与否
     */
    boolean getIsInitialized();

    default boolean testConnection() {
        boolean flag = true;
        try {
            fileList("/");
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}
