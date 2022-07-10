package im.zhaojun.zfile.admin.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.ShortLinkMapper;
import im.zhaojun.zfile.admin.model.entity.ShortLink;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 短链 Service
 *
 * @author zhaojun
 */
@Service
public class ShortLinkService extends ServiceImpl<ShortLinkMapper, ShortLink> implements IService<ShortLink> {

    @Resource
    private ShortLinkMapper shortLinkMapper;

    @Resource
    private StorageSourceService storageSourceService;

    /**
     * 根据短链接 key 查询短链接
     *
     * @param   key
     *          短链接 key
     *
     * @return  短链接信息
     */
    public ShortLink findByKey(String key) {
        return shortLinkMapper.findByKey(key);
    }


    /**
     * 根据短链接 id 查询短链接
     *
     * @param   id
     *          短链接 id
     *
     * @return  短链接信息
     */
    public ShortLink findById(Integer id) {
        return shortLinkMapper.selectById(id);
    }


    /**
     * 根据存储源 ID 和文件路径查询短链接
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   url
     *          短链接 url
     *
     * @return  短链接信息
     */
    public ShortLink findByStorageIdAndUrl(Integer storageId, String url) {
        return shortLinkMapper.findByStorageIdAndUrl(storageId, url);
    }


    /**
     * 根据存储源 KEY 和文件路径查询短链接
     *
     * @param   storageKey
     *          存储源 KEY
     *
     * @param   url
     *          短链接 url
     *
     * @return  短链接信息
     */
    public ShortLink findByStorageKeyAndUrl(String storageKey,String url) {
        Integer storageId = storageSourceService.findIdByKey(storageKey);
        return findByStorageIdAndUrl(storageId, url);
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
    public ShortLink generatorShortLink(Integer storageId, String fullPath) {
        ShortLink shortLink;
        String randomKey;
        do {
            // 获取短链
            randomKey = RandomUtil.randomString(6);
            shortLink = findByKey(randomKey);
        } while (shortLink != null);

        shortLink = new ShortLink();
        shortLink.setShortKey(randomKey);
        shortLink.setUrl(fullPath);
        shortLink.setCreateDate(new Date());
        shortLink.setStorageId(storageId);
        save(shortLink);

        return shortLink;
    }

}