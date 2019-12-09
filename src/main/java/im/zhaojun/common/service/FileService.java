package im.zhaojun.common.service;

import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public interface FileService {

    @Cacheable(condition = "#root.targetClass.simpleName != 'LocalServiceImpl'")
    List<FileItemDTO> fileList(String path) throws Exception;

    @Cacheable
    String getDownloadUrl(String path) throws Exception;

    @PostConstruct
    default void init() {}

    /**
     * 清除缓存.
     */
    @CacheEvict(allEntries = true)
    default void clearCache() {}

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

    StorageTypeEnum getStorageTypeEnum();

    boolean getIsInitialized();
}
