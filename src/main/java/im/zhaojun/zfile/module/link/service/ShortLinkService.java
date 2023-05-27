package im.zhaojun.zfile.module.link.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.mapper.ShortLinkMapper;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ShortLinkService shortLinkService;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 根据短链接 key 查询短链接
     *
     * @param   key
     *          短链接 key
     *
     * @return  短链接信息
     */
    @Cacheable(key = "#key", unless = "#result == null")
    public ShortLink findByKey(String key) {
        return shortLinkMapper.findByKey(key);
    }


    /**
     * 根据存储源 ID 和文件路径查询短链接
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   fileFullPath
     *          文件全路径
     *
     * @return  短链接信息
     */
    @Cacheable(key = "#storageId + #fileFullPath", unless = "#result == null")
    public ShortLink findByStorageIdAndUrl(Integer storageId, String fileFullPath) {
        return shortLinkMapper.findByStorageIdAndUrl(storageId, fileFullPath);
    }


    /**
     * 为存储源指定路径生成短链接, 保证生成的短连接 key 是不同的
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
            throw new IllegalArgumentException("过期时间不合法");
        }

        ShortLink shortLink;
        String randomKey;
        int generateCount = 0;
        do {
            // 获取短链
            randomKey = RandomUtil.randomString(6);
            shortLink = shortLinkService.findByKey(randomKey);
            generateCount++;
        } while (shortLink != null);

        shortLink = new ShortLink();
        shortLink.setStorageId(storageId);
        shortLink.setUrl(fullPath);
        shortLink.setCreateDate(new Date());
        shortLink.setShortKey(randomKey);

        if (expireTime == -1) {
            shortLink.setExpireDate(DateUtil.parseDate("9999-12-31"));
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
        log.info("删除存储源 ID 为 {} 的直/短链 {} 条", storageId, deleteSize);
        return deleteSize;
    }

    public Page<ShortLink> selectPage(Page<ShortLink> pages, QueryWrapper<ShortLink> queryWrapper) {
        return shortLinkMapper.selectPage(pages, queryWrapper);
    }


    private boolean checkExpireDateIsValidate(Long expires) {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String linkExpireTimes = systemConfig.getLinkExpireTimes();
        JSONArray jsonArray = JSONArray.parse(linkExpireTimes);
        Set<Long> expireSet = new HashSet<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            expireSet.add(jsonArray.getJSONObject(i).getLong("seconds"));
        }
        return expireSet.contains(expires);
    }

}