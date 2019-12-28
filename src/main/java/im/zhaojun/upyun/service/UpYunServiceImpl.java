package im.zhaojun.upyun.service;

import cn.hutool.core.util.URLUtil;
import com.UpYun;
import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public class UpYunServiceImpl extends AbstractFileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(UpYunServiceImpl.class);

    private static final String END_MARK = "g2gCZAAEbmV4dGQAA2VvZg";

    @Resource
    private StorageConfigService storageConfigService;

    private String domain;

    private UpYun upYun;

    private String basePath;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.UPYUN);
            String bucketName = stringStorageConfigMap.get(StorageConfigConstant.BUCKET_NAME_KEY).getValue();
            String username = stringStorageConfigMap.get(StorageConfigConstant.USERNAME_KEY).getValue();
            String password = stringStorageConfigMap.get(StorageConfigConstant.PASSWORD_KEY).getValue();
            domain = stringStorageConfigMap.get(StorageConfigConstant.DOMAIN_KEY).getValue();
            basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();
            upYun = new UpYun(bucketName, username, password);
            isInitialized = testConnection();
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + "初始化异常, 已跳过");
        }
    }

    @Override
    @Cacheable
    public List<FileItemDTO> fileList(String path) throws Exception {
        ArrayList<FileItemDTO> fileItemList = new ArrayList<>();
        String nextMark = null;

        do {
            HashMap<String, String> hashMap = new HashMap<>(24);
            hashMap.put("x-list-iter", nextMark);
            hashMap.put("x-list-limit", "100");
            UpYun.FolderItemIter folderItemIter = upYun.readDirIter(URLUtil.encode(basePath + path), hashMap);
            nextMark = folderItemIter.iter;
            ArrayList<UpYun.FolderItem> folderItems = folderItemIter.files;
            if (folderItems != null) {
                for (UpYun.FolderItem folderItem : folderItems) {
                    FileItemDTO fileItemDTO = new FileItemDTO();
                    fileItemDTO.setName(folderItem.name);
                    fileItemDTO.setSize(folderItem.size);
                    fileItemDTO.setTime(folderItem.date);
                    fileItemDTO.setPath(path);

                    if ("folder".equals(folderItem.type)) {
                        fileItemDTO.setType(FileTypeEnum.FOLDER);
                    } else {
                        fileItemDTO.setType(FileTypeEnum.FILE);
                        fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(basePath + path, fileItemDTO.getName())));
                    }
                    fileItemList.add(fileItemDTO);
                }
            }
        } while (!END_MARK.equals(nextMark));
        return fileItemList;

    }

    @Override
    @Cacheable
    public String getDownloadUrl(String path) {
        return URLUtil.complateUrl(domain, path);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.UPYUN;
    }

}