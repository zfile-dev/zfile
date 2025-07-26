package im.zhaojun.zfile.module.storage.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import im.zhaojun.zfile.core.cache.ZFileCacheManager;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.InvalidStorageSourceBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.StrPool;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import im.zhaojun.zfile.module.readme.service.ReadmeConfigService;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.convert.StorageSourceConvert;
import im.zhaojun.zfile.module.storage.event.StorageSourceCopyEvent;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import im.zhaojun.zfile.module.storage.mapper.StorageSourceMapper;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceAllParamDTO;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceDTO;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceInitDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.SearchModeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.model.request.admin.CopyStorageSourceRequest;
import im.zhaojun.zfile.module.storage.model.request.admin.UpdateStorageSortRequest;
import im.zhaojun.zfile.module.storage.model.request.base.FileListConfigRequest;
import im.zhaojun.zfile.module.storage.model.request.base.SaveStorageSourceRequest;
import im.zhaojun.zfile.module.storage.model.result.StorageSourceConfigResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 存储源基本信息 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "storageSource")
public class StorageSourceService {

    @Resource
    private StorageSourceMapper storageSourceMapper;

    @Resource
    private StorageSourceConvert storageSourceConvert;

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    @Resource
    private PasswordConfigService passwordConfigService;

    @Resource
    private UserStorageSourceService userStorageSourceService;

    @Resource
    private ReadmeConfigService readmeConfigService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private ZFileCacheManager zfileCacheManager;


    /**
     * 获取所有存储源列表
     *
     * @return 存储源列表
     */
    public List<StorageSource> findAllOrderByOrderNum() {
        return storageSourceMapper.findAllOrderByOrderNum();
    }


    /**
     * 获取当前登录用户可访问的存储源列表
     *
     * @return 存储源列表
     */
    public List<StorageSource> findAllEnableOrderByOrderNum(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return zfileCacheManager.findAllEnableOrderByOrderNum(userId,
                userEnableStorageSourceId -> storageSourceMapper.findUserEnableList(userEnableStorageSourceId)
        );
    }

    /**
     * 获取指定存储源设置
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源设置
     */
    @Cacheable(key = "#id", unless = "#result == null", condition = "#id != null")
    public StorageSource findById(Integer id) {
        return storageSourceMapper.selectById(id);
    }


    /**
     * 根据存储源 key 获取存储源
     *
     * @param   storageKey
     *          存储源 key
     *
     * @throws InvalidStorageSourceBizException   存储源不存在时, 抛出异常.
     *
     * @return  存储源信息
     */
    @Cacheable(key = "#storageKey", unless = "#result == null", condition = "#storageKey != null")
    public StorageSource findByStorageKey(String storageKey) {
        return storageSourceMapper.findByStorageKey(storageKey);
    }


    /**
     * 根据存储源 key 清除 key 的缓存
     *
     * @param   storageKey
     *          存储源 key
     */
    @CacheEvict(key = "#storageKey")
    public void clearCacheByStorageKey(String storageKey) {}


    /**
     * 根据存储源 key 获取存储源 id
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  存储源信息
     */
    public Integer findIdByKey(String storageKey) {
        return Optional.ofNullable(((StorageSourceService) AopContext.currentProxy()).findByStorageKey(storageKey)).map(StorageSource::getId).orElse(null);
    }


    /**
     * 根据存储源 id 获取存储源 key
     *
     * @param   id
     *          存储源 id
     *
     * @return  存储源 key
     */
    public String findStorageKeyById(Integer id){
        return Optional.ofNullable(((StorageSourceService)AopContext.currentProxy()).findById(id)).map(StorageSource::getKey).orElse(null);
    }


    /**
     * 根据 id 获取指定存储源的类型.
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源对应的类型.
     */
    public StorageTypeEnum findStorageTypeById(Integer id) {
        return Optional.ofNullable(((StorageSourceService)AopContext.currentProxy()).findById(id)).map(StorageSource::getType).orElse(null);
    }


    /**
     * 获取指定存储源 DTO 对象, 此对象包含详细的参数设置.
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源 DTO
     */
    @Cacheable(key = "'dto-' + #id", unless = "#result == null", condition = "#id != null")
    public StorageSourceDTO findDTOById(Integer id) {
        // 将参数列表通过反射写入到 StorageSourceAllParam 中.
        StorageSourceAllParamDTO storageSourceAllParam = new StorageSourceAllParamDTO();
        for (StorageSourceConfig storageSourceConfig : storageSourceConfigService.selectStorageConfigByStorageId(id)) {
            if (ReflectUtil.hasField(StorageSourceAllParamDTO.class, storageSourceConfig.getName())) {
                ReflectUtil.setFieldValue(storageSourceAllParam, storageSourceConfig.getName(), storageSourceConfig.getValue());
            } else {
                log.warn("数据库中存储源 {} 参数 {} 不存在于存储源参数 DTO 中, 请检查参数名是否正确.", id, storageSourceConfig.getName());
            }
        }
        // 获取数据库对象，转为 dto 对象返回
        StorageSource storageSource = findById(id);
        return storageSourceConvert.entityToDTO(storageSource, storageSourceAllParam);
    }


    /**
     * 判断存储源 key 是否已存在 (不读取缓存)
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  是否已存在
     */
    public boolean existByStorageKey(String storageKey) {
        return ((StorageSourceService)AopContext.currentProxy()).findByStorageKey(storageKey) != null;
    }


    /**
     * 删除指定存储源设置, 会级联删除其参数设置
     *
     * @param   id
     *          存储源 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "'dto-' + #id"),
            @CacheEvict(key = "#result.key", condition = "#result != null")
    })
    public StorageSource deleteById(Integer id) {
        log.info("删除 id 为 {} 的存储源", id);
        StorageSource storageSource = ((StorageSourceService)AopContext.currentProxy()).findById(id);

        if (storageSource == null) {
            throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
        }

        StorageSourceDeleteEvent storageSourceDeleteEvent = new StorageSourceDeleteEvent(storageSource);
        applicationEventPublisher.publishEvent(storageSourceDeleteEvent);

        int deleteEntitySize = storageSourceMapper.deleteById(id);

        StorageSourceContext.destroy(storageSource);
        log.info("删除存储源 {} 成功, 影响行数: {}", id, deleteEntitySize);
        zfileCacheManager.clearUserEnableStorageSourceCache();
        return storageSource;
    }


    /**
     * 交换存储源排序
     *
     * @param   updateStorageSortRequestList
     *          更新排序的存储源 id 及排序值列表
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public void updateStorageSort(List<UpdateStorageSortRequest> updateStorageSortRequestList) {
        for (int i = 0; i < updateStorageSortRequestList.size(); i++) {
            UpdateStorageSortRequest item = updateStorageSortRequestList.get(i);
            if (!Objects.equals(i, item.getOrderNum())) {
                log.info("变更存储源 {} 顺序号为 {}", item.getId(), i);
                storageSourceMapper.updateSetOrderNumById(i, item.getId());
            }
        }
    }


    @Caching(evict = {
            @CacheEvict(key = "#entity.id"),
            @CacheEvict(key = "#entity.key"),
            @CacheEvict(key = "'dto-' + #entity.id")
    })
    public void updateById(StorageSource entity) {
        storageSourceMapper.updateById(entity);
        zfileCacheManager.clearUserEnableStorageSourceCache();
    }


    /**
     * 保存存储源基本信息及其对应的参数设置
     *
     * @param   saveStorageSourceRequest
     *          存储源 DTO 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStorageSource(SaveStorageSourceRequest saveStorageSourceRequest) {
        boolean isSave = ObjUtil.isEmpty(saveStorageSourceRequest.getId());

        log.info("尝试保存存储源, id: {}, name: {}, key: {}, type: {}",
                saveStorageSourceRequest.getId(), saveStorageSourceRequest.getName(),
                saveStorageSourceRequest.getKey(), saveStorageSourceRequest.getType().getDescription());

        // 转换为存储源 entity 对象
        StorageSource storageSource = storageSourceConvert.saveRequestToEntity(saveStorageSourceRequest);
        storageSource.setSearchMode(SearchModeEnum.SEARCH_ALL_MODE);

        // 如果是更新，则销毁之前的存储源上下文
        if (!isSave) {
            StorageSource dbStorageSource = ((StorageSourceService) AopContext.currentProxy()).findById(saveStorageSourceRequest.getId());
            if (dbStorageSource != null) {
                StorageSourceContext.destroy(dbStorageSource);
            }
        }

        // 保存或更新存储源
        StorageSource dbSaveResult = ((StorageSourceService)AopContext.currentProxy()).saveOrUpdate(storageSource);

        log.info("保存存储源成功, id: {}, name: {}, key: {}, type: {}",
                dbSaveResult.getId(), dbSaveResult.getName(),
                dbSaveResult.getKey(), dbSaveResult.getType().getDescription());

        // 存储源 ID
        Integer storageId = dbSaveResult.getId();

        // 保存存储源参数
        List<StorageSourceConfig> storageSourceConfigList =
                storageSourceConfigService.storageSourceAllParamToConfigList(storageId,
                                                                    dbSaveResult.getType(),
                                                                    saveStorageSourceRequest.getStorageSourceAllParam());
        storageSourceConfigService.saveBatch(storageId, storageSourceConfigList);
        log.info("保存存储源参数成功，尝试根据参数初始化存储源, id: {}, name: {}, config size: {}",
                dbSaveResult.getId(), dbSaveResult.getName(), storageSourceConfigList.size());

        // 初始化并检查是否可用
        StorageSourceInitDTO storageSourceInitDTO = StorageSourceInitDTO.convert(dbSaveResult, storageSourceConfigList);
        StorageSourceContext.init(storageSourceInitDTO);
        log.info("根据参数初始化存储源成功, id: {}, name: {}, config size: {}",
                dbSaveResult.getId(), dbSaveResult.getName(), storageSourceConfigList.size());


        // 如果是新增存储源，根据用户设置为用户添加默认权限
        if (isSave) {
            userStorageSourceService.addDefaultPermissionsForAllUsersInStorageSource(storageId);
        }

        return storageId;
    }


    /**
     * 保存或修改存储源设置，如果没有 id 则新增，有则更新，且会检测是否填写 key，如果没写，则自动将 id 设置为 key 并保存。
     *
     * @param   storageSource
     *          存储源对象
     *
     * @return  保存后对象
     */
    @Caching(evict = {
            @CacheEvict(key = "#result.id"),
            @CacheEvict(key = "'dto-' + #result.id"),
            @CacheEvict(key = "#result.key")
    })
    public StorageSource saveOrUpdate(StorageSource storageSource) {
        // 保存存储源基本信息
        if (storageSource.getId() == null) {
            storageSourceMapper.insert(storageSource);
        } else {
            // 判断是否修改了存储源别名，如果修改了则清除之前存储源别名的缓存。
            StorageSource originStorageSource = storageSourceMapper.selectById(storageSource.getId());
            if (!StringUtils.equals(originStorageSource.getKey(), storageSource.getKey())) {
                ((StorageSourceService)AopContext.currentProxy()).clearCacheByStorageKey(originStorageSource.getKey());
            }
            storageSourceMapper.updateById(storageSource);
        }

        // 如果没输入存储源 key, 则自动将 id 设置为 key
        if (StringUtils.isEmpty(storageSource.getKey()) && !StringUtils.equals(storageSource.getId().toString(), storageSource.getKey())) {
            storageSource.setKey(Convert.toStr(storageSource.getId()));
            storageSourceMapper.updateById(storageSource);
        }
        zfileCacheManager.clearUserEnableStorageSourceCache();
        return storageSource;
    }


    public StorageSourceConfigResult getStorageConfigSource(FileListConfigRequest fileListConfigRequest) {
        String storageKey = fileListConfigRequest.getStorageKey();

        // 判断存储源是否存在.
        StorageSource storageSource = ((StorageSourceService)AopContext.currentProxy()).findByStorageKey(storageKey);
        if (storageSource == null) {
            throw new InvalidStorageSourceBizException(storageKey);
        }

        // 根据存储源 key 获取存储源 id
        Integer storageId = storageSource.getId();

        // 拼接用户目录
        AbstractBaseFileService<IStorageParam> baseFileService = StorageSourceContext.getByStorageId(storageId);
        String fullPath = StringUtils.concat(baseFileService.getCurrentUserBasePath(), fileListConfigRequest.getPath());

        VerifyResultDTO verifyPassword = passwordConfigService.verifyPassword(storageId, fullPath, fileListConfigRequest.getPassword());

        ReadmeConfig readmeByPath = null;
        if (verifyPassword.isPassed()) {
            // 获取指定存储源路径下的 readme 信息
            readmeByPath = readmeConfigService.getByStorageAndPath(storageId, fileListConfigRequest.getPath(), storageSource.getCompatibilityReadme());
        } else {
            log.info("文件夹密码验证失败，不获取 readme 信息, storageId: {}, path: {}, password: {}", storageId, fullPath, fileListConfigRequest.getPassword());
        }

        StorageSourceConfigResult storageSourceConfigResult = storageSourceConvert.entityToConfigResult(storageSource, readmeByPath);

        // 获取当前用户对该存储源的权限
        HashMap<String, Boolean> permissionMap = userStorageSourceService.getCurrentUserPermissionMapByStorageId(storageId);
        storageSourceConfigResult.setPermission(permissionMap);

        // 获取存储源元数据
        storageSourceConfigResult.setMetadata(baseFileService.getStorageSourceMetadata());

        UserStorageSource userStorageSource = userStorageSourceService.getByUserIdAndStorageId(ZFileAuthUtil.getCurrentUserId(), storageId);
        if (userStorageSource == null) {
            storageSourceConfigResult.setRootPath(StrPool.SLASH);
        } else {
            storageSourceConfigResult.setRootPath(userStorageSource.getRootPath());
        }
        return storageSourceConfigResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer copy(CopyStorageSourceRequest copyStorageSourceRequest) {
        // 检查目标存储源别名是否已存在
        String toKey = copyStorageSourceRequest.getToKey();
        boolean existByStorageKey = ((StorageSourceService)AopContext.currentProxy()).existByStorageKey(toKey);
        if (existByStorageKey) {
            throw new BizException(ErrorCode.BIZ_STORAGE_KEY_EXIST);
        }

        // 检查复制源是否存在
        Integer fromStorageId = copyStorageSourceRequest.getFromId();
        StorageSource storageSource = ((StorageSourceService)AopContext.currentProxy()).findById(fromStorageId);
        if (storageSource == null) {
            throw new InvalidStorageSourceBizException(fromStorageId);
        }

        StorageSource newStorageSource = new StorageSource();
        BeanUtils.copyProperties(storageSource, newStorageSource);
        newStorageSource.setId(null);
        newStorageSource.setKey(copyStorageSourceRequest.getToKey());
        newStorageSource.setName(copyStorageSourceRequest.getToName());
        StorageSource dbSaveResult = ((StorageSourceService)AopContext.currentProxy()).saveOrUpdate(newStorageSource);
        Integer newStorageId = dbSaveResult.getId();
        log.info("复制存储源成功，源 [id: {}, name: {}, key: {}], 复制后 [id: {}, name: {}, key: {}, type: {}]",
                fromStorageId, storageSource.getName(), storageSource.getKey(),
                newStorageId, dbSaveResult.getName(),
                dbSaveResult.getKey(), dbSaveResult.getType().getDescription());

        StorageSourceCopyEvent storageSourceCopyEvent = new StorageSourceCopyEvent(fromStorageId, newStorageId);
        applicationEventPublisher.publishEvent(storageSourceCopyEvent);

        // 初始化存储源
        List<StorageSourceConfig> storageSourceConfigList = storageSourceConfigService.selectStorageConfigByStorageId(newStorageId);
        StorageSourceInitDTO storageSourceInitDTO = StorageSourceInitDTO.convert(dbSaveResult, storageSourceConfigList);
        StorageSourceContext.init(storageSourceInitDTO);
        log.info("初始化存储源成功, id: {}, name: {}, config size: {}",
                newStorageId, dbSaveResult.getName(), storageSourceConfigList.size());

        return newStorageId;
    }

}