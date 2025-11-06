package im.zhaojun.zfile.module.link.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.InvalidStorageSourceBizException;
import im.zhaojun.zfile.core.exception.core.ErrorPageBizException;
import im.zhaojun.zfile.core.exception.status.ForbiddenAccessException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.HttpUtil;
import im.zhaojun.zfile.core.util.StringUtils;
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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class LinkDownloadService {

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
    public ResponseEntity<?> handlerDirectLink(String storageKey, String filePath) {
        // 检查系统是否允许直链
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if (BooleanUtils.isNotTrue(systemConfigDTO.getShowPathLink())) {
            throw new ForbiddenAccessException(ErrorCode.BIZ_DIRECT_LINK_NOT_ALLOWED);
        }
        return handlerDownloadGetUrl(storageKey, filePath, null, DownloadLog.DOWNLOAD_TYPE_DIRECT_LINK);
    }

    @RefererCheck
    @LinkRateLimiter
    public ResponseEntity<?> handlerShortLink(String shortKey) throws IOException {
        // 从缓存中判断是否短链是否过期
        if (expireKeySet.contains(shortKey)) {
            throw new ForbiddenAccessException(ErrorCode.BIZ_SHORT_LINK_EXPIRED);
        }

        // 判断是否允许生成短链.
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if (BooleanUtils.isNotTrue(systemConfigDTO.getShowShortLink())) {
            throw new ForbiddenAccessException(ErrorCode.BIZ_SHORT_LINK_NOT_ALLOWED);
        }

        // 判断短链是否存在
        ShortLink shortLink = shortLinkService.findByKey(shortKey);
        if (shortLink == null) {
            throw new NotFoundAccessException(ErrorCode.BIZ_SHORT_LINK_NOT_FOUNT);
        }

        // 判断短链是否过期
        if (shortLink.getExpireDate() != null) {
            DateTime now = DateUtil.date();
            boolean isExpire = now.isAfter(shortLink.getExpireDate());
            if (isExpire) {
                expireKeySet.add(shortKey);
                throw new ForbiddenAccessException(ErrorCode.BIZ_SHORT_LINK_EXPIRED);
            }
        }

        // 获取实际文件路径，下载并记录日志
        Integer storageId = shortLink.getStorageId();
        String storageKey = storageSourceService.findStorageKeyById(storageId);
        String filePath = shortLink.getUrl();
        return handlerDownloadGetUrl(storageKey, filePath, shortKey, DownloadLog.DOWNLOAD_TYPE_SHORT_LINK);
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
     */
    private ResponseEntity<?> handlerDownloadGetUrl(String storageKey, String filePath, String shortKey, String downloadType) {
        String fileAlias = StringUtils.equals(downloadType, DownloadLog.DOWNLOAD_TYPE_DIRECT_LINK) ? filePath : shortKey;

        // 获取存储源 Service
        AbstractBaseFileService<?> fileService;
        try {
            fileService = StorageSourceContext.getByStorageKey(storageKey);
        } catch (InvalidStorageSourceBizException e) {
            throw new ErrorPageBizException("无效的或初始化失败的存储源 [" + storageKey + "] 文件 [" + fileAlias + "] 下载链接异常, 无法下载.", e);
        }

        if (fileService == null) {
            throw new ErrorPageBizException("未找到存储源 [" + storageKey + "] 文件 [" + fileAlias + "] 下载链接异常, 无法下载.");
        }

        StorageSource storageSource = storageSourceService.findByStorageKey(storageKey);
        Boolean enable = storageSource.getEnable();
        if (!enable) {
            throw new ErrorPageBizException("未启用的存储源 [" + storageKey + "] 文件 [" + fileAlias + "] 下载链接异常, 无法下载.");
        }

        // 检查是否访问了禁止下载的目录
        if (filterConfigService.checkFileIsDisableDownload(storageSource.getId(), filePath)) {
            // 获取 Forbidden 页面地址
            return ResponseEntity.status(302)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header(HttpHeaders.LOCATION, systemConfigService.getForbiddenUrl())
                    .build();
        }

        // 获取文件下载链接
        String downloadUrl;
        try {
            downloadUrl = fileService.getDownloadUrl(filePath);
        } catch (NotFoundAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new ErrorPageBizException("获取存储源 [" + storageKey + "] 文件 [" + fileAlias + "] 下载链接异常, 无法下载.", e);
        }

        // 判断下载链接是否为空
        if (StringUtils.isEmpty(downloadUrl)) {
            throw new ErrorPageBizException("获取存储源 [" + storageKey + "] 文件 [" + fileAlias + "] 下载链接为空, 无法下载.");
        }

        // 记录下载日志.
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        Boolean recordDownloadLog = systemConfig.getRecordDownloadLog();
        if (BooleanUtils.isTrue(recordDownloadLog)) {
            DownloadLog downloadLog = new DownloadLog(downloadType, filePath, storageKey, shortKey);
            downloadLogService.save(downloadLog);
        }

        // 判断下载链接是否为 m3u8 格式, 如果是则返回 m3u8 内容.
        if (StringUtils.equalsIgnoreCase(FileUtil.extName(filePath), "m3u8")) {
            String textContent = HttpUtil.getTextContent(downloadUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl;charset=utf-8"));
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("attachment")
                    .filename(FileUtils.getName(filePath), StandardCharsets.UTF_8)
                    .build();
            headers.setContentDisposition(contentDisposition);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(textContent);
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header(HttpHeaders.LOCATION, downloadUrl)
                .build();
    }

}