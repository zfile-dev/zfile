package im.zhaojun.zfile.module.link.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.exception.IllegalDownloadLinkException;
import im.zhaojun.zfile.core.exception.InvalidShortLinkException;
import im.zhaojun.zfile.core.exception.file.InvalidStorageSourceException;
import im.zhaojun.zfile.core.exception.file.operator.StorageSourceFileOperatorException;
import im.zhaojun.zfile.core.util.HttpUtil;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.filter.service.FilterConfigService;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import im.zhaojun.zfile.module.log.service.DownloadLogService;
import im.zhaojun.zfile.module.storage.annotation.LinkRateLimiter;
import im.zhaojun.zfile.module.storage.annotation.RefererCheck;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EncodingUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class LinkDownloadService {

    @Resource
    private StorageSourceContext storageSourceContext;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private DownloadLogService downloadLogService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private FilterConfigService filterConfigService;

    private final Set<String> expireKeySet = new HashSet<>();

    @RefererCheck
    @LinkRateLimiter
    public void handlerDirectLink(String storageKey, String filePath) throws IOException {
        // 检查系统是否允许直链
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if (BooleanUtil.isFalse(systemConfigDTO.getShowPathLink())) {
            throw new InvalidShortLinkException("当前系统不允许使用直链.");
        }
        handlerDownload(storageKey, filePath, null, DownloadLog.DOWNLOAD_TYPE_DIRECT_LINK);
    }

    @RefererCheck
    @LinkRateLimiter
    public void handlerShortLink(String shortKey) throws IOException {
        // 从缓存中判断是否短链是否过期
        if (expireKeySet.contains(shortKey)) {
            throw new InvalidShortLinkException("此链接已过期.");
        }

        // 判断是否允许生成短链.
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if ( BooleanUtil.isFalse(systemConfigDTO.getShowShortLink())) {
            throw new IllegalDownloadLinkException("当前系统不允许使用短链.");
        }

        // 判断短链是否存在
        ShortLink shortLink = shortLinkService.findByKey(shortKey);
        if (shortLink == null) {
            throw new InvalidShortLinkException("此直链不存在或已失效.");
        }

        // 判断短链是否过期
        if (shortLink.getExpireDate() != null) {
            DateTime now = DateUtil.date();
            boolean isExpire = now.isAfter(shortLink.getExpireDate());
            if (isExpire) {
                expireKeySet.add(shortKey);
                throw new InvalidShortLinkException("此链接已过期.");
            }
        }

        // 获取实际文件路径，下载并记录日志
        Integer storageId = shortLink.getStorageId();
        String storageKey = storageSourceService.findStorageKeyById(storageId);
        String filePath = shortLink.getUrl();
        handlerDownload(storageKey, filePath, shortKey, DownloadLog.DOWNLOAD_TYPE_SHORT_LINK);
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
     * @param   downloadType
     *          下载类型, 直链下载(directLink)或短链下载(shortLink)
     *
     * @throws IOException 可能抛出的 IO 异常
     */
    private void handlerDownload(String storageKey, String filePath, String shortKey, String downloadType) throws IOException {
        HttpServletRequest request = RequestHolder.getRequest();
        HttpServletResponse response = RequestHolder.getResponse();

        // 获取存储源 Service
        AbstractBaseFileService<?> fileService;
        try {
            fileService = storageSourceContext.getByStorageKey(storageKey);
        } catch (InvalidStorageSourceException e) {
            throw new RuntimeException("无效的或初始化失败的存储源 [" + storageKey + "] 文件 [" + filePath + "] 下载链接异常, 无法下载.", e);
        }

        StorageSource storageSource = storageSourceService.findByStorageKey(storageKey);
        Boolean enable = storageSource.getEnable();
        if (!enable) {
            throw new RuntimeException("未启用的存储源 [" + storageKey + "] 文件 [" + filePath + "] 下载链接异常, 无法下载.");
        }

        // 检查是否访问了禁止下载的目录
        if (filterConfigService.checkFileIsDisableDownload(storageSource.getId(), filePath)) {
            // 获取 Forbidden 页面地址
            String forbiddenUrl = systemConfigService.getForbiddenUrl();
            RequestHolder.getResponse().sendRedirect(forbiddenUrl);
            return;
        }

        // 获取文件下载链接
        String downloadUrl;
        try {
            downloadUrl = fileService.getDownloadUrl(filePath);
        } catch (StorageSourceFileOperatorException e) {
            throw new RuntimeException("获取存储源 [" + storageKey + "] 文件 [" + filePath + "] 下载链接异常, 无法下载.", e);
        }

        // 判断下载链接是否为空
        if (StrUtil.isEmpty(downloadUrl)) {
            throw new RuntimeException("获取存储源 [" + storageKey + "] 文件 [" + filePath + "] 下载链接为空, 无法下载.");
        }

        // 记录下载日志.
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        Boolean recordDownloadLog = systemConfig.getRecordDownloadLog();
        if (BooleanUtil.isTrue(recordDownloadLog)) {
            DownloadLog downloadLog = new DownloadLog(downloadType, filePath, storageKey, shortKey);
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

}
