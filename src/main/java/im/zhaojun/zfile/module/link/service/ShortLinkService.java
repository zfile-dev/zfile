package im.zhaojun.zfile.module.link.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.module.config.model.dto.LinkExpireDTO;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.event.DeleteExpireLinkEvent;
import im.zhaojun.zfile.module.link.mapper.ShortLinkMapper;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 短链 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "shortLink")
public class ShortLinkService {

    @Resource
    private ShortLinkMapper shortLinkMapper;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 根据短链接 key 查询短链接
     *
     * @param   key
     *          短链接 key
     *
     * @return  短链接信息
     */
    @Cacheable(key = "#key", unless = "#result == null", condition = "#key != null")
    public ShortLink findByKey(String key) {
        return shortLinkMapper.findByKey(key);
    }

    /**
     * 根据存储源 ID 和 URL 查询短链接
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   url
     *          文件路径
     *
     * @return  短链接信息
     */
    public @Nullable ShortLink findByStorageIdAndUrl(Integer storageId, String url, @Nullable Date expireDate) {
        return shortLinkMapper.findByStorageIdAndUrl(storageId, url, expireDate);
    }


    /**
     * 为存储源指定路径生成短链接, 保证生成的短连接 key 是不同的 (如果是永久链接, 则不会重新生成)
     *
     * @param   storageId
     *          存储源 id
     *
     * @param   fullPath
     *          存储源路径
     *
     * @return  生成后的短链接信息
     */
    public ShortLink generatorShortLink(Integer storageId, String fullPath, Long expireTime) {
        boolean validate = checkExpireDateIsValidate(expireTime);
        if (!validate) {
            throw new BizException(ErrorCode.BIZ_EXPIRE_TIME_ILLEGAL);
        }

        // 永久链接不在重复生成
        if (Objects.equals(expireTime, ShortLink.PERMANENT_EXPIRE_TIME)) {
            ShortLink shortLink = findByStorageIdAndUrl(storageId, fullPath, ShortLink.PERMANENT_EXPIRE_DATE);
            if (shortLink != null) {
                return shortLink;
            }
        }

        ShortLink shortLink;
        String randomKey;
        int generateCount = 0;
        do {
            // 获取短链
            randomKey = RandomUtil.randomString(6);
            shortLink = ((ShortLinkService) AopContext.currentProxy()).findByKey(randomKey);
            generateCount++;
        } while (shortLink != null);

        shortLink = new ShortLink();
        shortLink.setStorageId(storageId);
        shortLink.setUrl(fullPath);
        shortLink.setCreateDate(new Date());
        shortLink.setShortKey(randomKey);

        if (expireTime == -1) {
            shortLink.setExpireDate(ShortLink.PERMANENT_EXPIRE_DATE);
        } else {
            shortLink.setExpireDate(new Date(System.currentTimeMillis() + expireTime * 1000L));
        }


        if (log.isDebugEnabled()) {
            log.debug("生成直/短链: 存储源 ID: {}, 文件路径: {}, 短链 key {}, 随机生成直链冲突次数: {}",
                    shortLink.getStorageId(), shortLink.getUrl(), shortLink.getShortKey(), generateCount);
        }

        shortLinkMapper.insert(shortLink);
        return shortLink;
    }

    @CacheEvict(allEntries = true)
    public int deleteExpireLink() {
        applicationEventPublisher.publishEvent(new DeleteExpireLinkEvent());
        int deleteSize = shortLinkMapper.deleteExpireLink();
        log.info("删除过期直/短链 {} 条", deleteSize);
        return deleteSize;
    }

    @CacheEvict(allEntries = true)
    public void removeById(Integer id) {
        log.info("删除 id 为 {} 的直/短链", id);
        shortLinkMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public void removeBatchByIds(List<Integer> ids) {
        log.info("批量删除直/短链，id 集合为 {}", ids);
        shortLinkMapper.deleteBatchIds(ids);
    }

    @CacheEvict(allEntries = true)
    public int deleteByStorageId(Integer storageId) {
        int deleteSize = shortLinkMapper.deleteByStorageId(storageId);
        log.info("删除存储源 ID 为 {} 的短链 {} 条", storageId, deleteSize);
        return deleteSize;
    }

    /**
     * 监听存储源删除事件，根据存储源 id 删除相关的短链
     *
     * @param   storageSourceDeleteEvent
     *          存储源删除事件
     */
    @EventListener
    public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
        Integer storageId = storageSourceDeleteEvent.getId();
        int updateRows = ((ShortLinkService) AopContext.currentProxy()).deleteByStorageId(storageId);
        if (log.isDebugEnabled()) {
            log.debug("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源短链 {} 条",
                    storageId,
                    storageSourceDeleteEvent.getName(),
                    storageSourceDeleteEvent.getType().getDescription(),
                    updateRows);
        }
    }

    public Page<ShortLink> selectPage(Page<ShortLink> pages, Wrapper<ShortLink> queryWrapper) {
        return shortLinkMapper.selectPage(pages, queryWrapper);
    }

    private boolean checkExpireDateIsValidate(Long expires) {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

        List<LinkExpireDTO> linkExpireTimeList = systemConfig.getLinkExpireTimes();

        for (LinkExpireDTO linkExpireDTO : linkExpireTimeList) {
            if (linkExpireDTO.getSeconds().equals(expires)) {
                return true;
            }
        }
        return false;
    }

}