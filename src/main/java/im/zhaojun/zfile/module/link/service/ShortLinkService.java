package im.zhaojun.zfile.module.link.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.core.exception.file.InvalidStorageSourceException;
import im.zhaojun.zfile.core.exception.file.operator.StorageSourceFileOperatorException;
import im.zhaojun.zfile.core.util.HttpUtil;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.mapper.ShortLinkMapper;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import im.zhaojun.zfile.module.log.service.DownloadLogService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EncodingUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

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
    private StorageSourceContext storageSourceContext;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DownloadLogService downloadLogService;

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
    public ShortLink generatorShortLink(Integer storageId, String fullPath) {
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
    
        if (log.isDebugEnabled()) {
            log.debug("生成直/短链: 存储源 ID: {}, 文件路径: {}, 短链 key {}, 随机生成直链冲突次数: {}",
                    shortLink.getStorageId(), shortLink.getUrl(), shortLink.getShortKey(), generateCount);
        }
        
        shortLinkMapper.insert(shortLink);
        return shortLink;
    }


    /**
     * 处理指定存储源的下载请求
     *
     * @param   storageKey
     *          存储源 key
     *
     * @param   filePath
     *          文件路径
     *
     * @param   shortKey
     *          短链接 key
     *
     * @throws  IOException 可能抛出的 IO 异常
     */
    public void handlerDownload(String storageKey, String filePath, String shortKey) throws IOException {
        HttpServletRequest request = RequestHolder.getRequest();
        HttpServletResponse response = RequestHolder.getResponse();

        // 获取存储源 Service
        AbstractBaseFileService<?> fileService;
        try {
            fileService = storageSourceContext.getByStorageKey(storageKey);
        } catch (InvalidStorageSourceException e) {
            log.error("无效的存储源，存储源 ID: {}, 文件路径: {}", e.getStorageId(), filePath, e);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.getWriter().write("无效的或初始化失败的存储源, 请联系管理员!");
            return;
        }

        // 获取文件下载链接
        String downloadUrl;
        try {
            downloadUrl = fileService.getDownloadUrl(filePath);
        } catch (StorageSourceFileOperatorException e) {
            log.error("获取文件下载链接异常. 存储源 ID: {}, 文件路径: {}", e.getStorageId(), filePath, e);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.getWriter().write("获取下载链接异常，请联系管理员!");
            return;
        }

        // 判断下载链接是否为空
        if (StrUtil.isEmpty(downloadUrl)) {
            log.error("获取到文件下载链接为空，存储源 key {}, 文件路径 {}", storageKey, filePath);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.getWriter().write("获取下载链接异常，请联系管理员![2]");
        }

        // 记录下载日志.
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        Boolean recordDownloadLog = systemConfig.getRecordDownloadLog();
        if (BooleanUtil.isTrue(recordDownloadLog)) {
            DownloadLog downloadLog = new DownloadLog(filePath, storageKey, shortKey);
            downloadLogService.save(downloadLog);
        }

        // 判断下载链接是否为 m3u8 格式, 如果是则返回 m3u8 内容.
        if (StrUtil.equalsIgnoreCase(FileUtil.extName(filePath), "m3u8")) {
            String textContent = HttpUtil.getTextContent(downloadUrl);
            response.setContentType("application/vnd.apple.mpegurl;charset=utf-8");
            OutputStream outputStream = response.getOutputStream();
            byte[] textContentBytes = EncodingUtils.getBytes(textContent, CharsetUtil.CHARSET_UTF_8.displayName());
            IoUtil.write(outputStream, true, textContentBytes);
            return;
        }

        // 禁止直链被浏览器 302 缓存.
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        // 重定向到下载链接.
    
        String parameterType = request.getParameter("type");
        if (StrUtil.equals(parameterType, "preview")) {
            downloadUrl = UrlUtils.concatQueryParam(downloadUrl, "type", "preview");
        }
        
        response.sendRedirect(downloadUrl);
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
    
  
}