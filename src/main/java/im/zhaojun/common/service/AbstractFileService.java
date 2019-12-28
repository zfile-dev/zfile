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
 * @date 2019/12/28 19:27
 */
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public abstract class AbstractFileService implements FileService {

    protected boolean isInitialized;

    /**
     * 初始化方法, 启动时自动调用实现类的此方法进行初始化.
     */
    @PostConstruct
    public abstract void init();


    protected boolean testConnection() {
        boolean flag = true;
        try {
            fileList("/");
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 获取是否初始化成功
     * @return  初始化成功与否
     */
    public boolean getIsInitialized() {
        return isInitialized;
    }

    /**
     * 获取存储引擎类型
     * @return  存储引擎类型枚举
     */
    public abstract StorageTypeEnum getStorageTypeEnum();

    /**
     * 清除缓存.
     */
    @CacheEvict(allEntries = true)
    public void clearCache() {}

    /**
     * 搜索文件
     * @param name 文件名
     * @return      包含该文件名的所有文件或文件夹
     * @throws Exception    搜索过程出现的异常
     */
    public List<FileItemDTO> search(String name) throws Exception {
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
    public List<FileItemDTO> selectAllFileList() throws Exception {
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
}
