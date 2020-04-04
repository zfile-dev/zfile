package im.zhaojun.zfile.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import com.UpYun;
import im.zhaojun.zfile.exception.NotExistFileException;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.base.BaseFileService;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
@Slf4j
public class UpYunServiceImpl extends AbstractBaseFileService implements BaseFileService {

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
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
            String bucketName = stringStorageConfigMap.get(StorageConfigConstant.BUCKET_NAME_KEY).getValue();
            String username = stringStorageConfigMap.get(StorageConfigConstant.USERNAME_KEY).getValue();
            String password = stringStorageConfigMap.get(StorageConfigConstant.PASSWORD_KEY).getValue();
            domain = stringStorageConfigMap.get(StorageConfigConstant.DOMAIN_KEY).getValue();
            basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();
            basePath = ObjectUtil.defaultIfNull(basePath, "");

            if (Objects.isNull(bucketName) || Objects.isNull(username) || Objects.isNull(password)) {
                log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
                isInitialized = false;
            } else {
                upYun = new UpYun(bucketName, username, password);
                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
        }
    }

    @Override
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
    public String getDownloadUrl(String path) {
        return URLUtil.complateUrl(domain, path);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.UPYUN;
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        List<FileItemDTO> list;
        try {
            int end = path.lastIndexOf("/");
            list = fileList(path.substring(0, end));
        } catch (Exception e) {
            throw new NotExistFileException();
        }

        for (FileItemDTO fileItemDTO : list) {
            String fullPath = StringUtils.concatUrl(fileItemDTO.getPath(), fileItemDTO.getName());
            if (Objects.equals(fullPath, path)) {
                return fileItemDTO;
            }
        }

        throw new NotExistFileException();
    }

    @Override
    public List<StorageConfig> storageStrategyList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("bucket-name", "云存储服务名称"));
            add(new StorageConfig("username", "操作员名称"));
            add(new StorageConfig("password", "操作员密码"));
            add(new StorageConfig("domain", "加速域名"));
            add(new StorageConfig("base-path", "基路径"));
        }};
    }

}