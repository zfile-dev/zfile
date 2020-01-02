package im.zhaojun.common.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaojun
 * @date 2019/12/28 19:27
 */
@Slf4j
public abstract class AbstractFileService implements FileService {

    @Value("${zfile.cache.timeout}")
    protected Long timeout;

    protected boolean isInitialized;

    @CreateCache(name = "zfile-cache:")
    private Cache<String, Object> userCache;

    /***
     * 获取指定路径下的文件及文件夹, 默认缓存 60 分钟，每隔 30 分钟刷新一次.
     * @param path 文件路径
     * @return     文件及文件夹列表
     * @throws Exception  获取文件列表中出现的异常
     */
    @Override
    @Cached(name = "zfile-cache:",
            key =  "args[0]",
            cacheType = CacheType.LOCAL, condition = "mvel{bean('systemConfigService').enableCache}")
    @CacheRefresh(refresh = 30, timeUnit = TimeUnit.MINUTES)
    public abstract List<FileItemDTO> fileList(String path) throws Exception;


    /**
     * 清理当前存储引擎的缓存
     */
    public void clearCache() {
    }

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
