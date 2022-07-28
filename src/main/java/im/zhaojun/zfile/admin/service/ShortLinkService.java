package im.zhaojun.zfile.admin.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.ShortLinkMapper;
import im.zhaojun.zfile.admin.model.entity.DownloadLog;
import im.zhaojun.zfile.admin.model.entity.ShortLink;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.common.exception.InvalidStorageSourceException;
import im.zhaojun.zfile.common.exception.file.operator.DownloadFileException;
import im.zhaojun.zfile.common.util.HttpUtil;
import im.zhaojun.zfile.common.util.RequestHolder;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EncodingUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * 短链 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
public class ShortLinkService extends ServiceImpl<ShortLinkMapper, ShortLink> implements IService<ShortLink> {

    @Resource
    private ShortLinkMapper shortLinkMapper;

    @Resource
    private StorageSourceService storageSourceService;

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
        HttpServletResponse response = RequestHolder.getResponse();

        // 获取存储源 Service
        AbstractBaseFileService<?> fileService;
        try {
            fileService = storageSourceContext.getByKey(storageKey);
        } catch (InvalidStorageSourceException e) {
            log.error("无效的存储源，存储源 key {}, 文件路径 {}", storageKey, filePath);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.getWriter().write("无效的或初始化失败的存储源, 请联系管理员!");
            return;
        }

        // 获取文件下载链接
        String downloadUrl;
        try {
            downloadUrl = fileService.getDownloadUrl(filePath);
        } catch (DownloadFileException e) {
            log.error("获取文件下载链接异常 {}. 存储源 ID: {}, 文件路径: {}", e.getMessage(), e.getStorageId(), e.getPathAndName());
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
        }

        // 禁止直链被浏览器 302 缓存.
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        // 重定向到下载链接.
        response.sendRedirect(downloadUrl);
    }

}