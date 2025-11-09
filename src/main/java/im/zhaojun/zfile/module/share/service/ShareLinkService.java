package im.zhaojun.zfile.module.share.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.share.mapper.ShareLinkMapper;
import im.zhaojun.zfile.module.share.model.dto.ShareEntryDTO;
import im.zhaojun.zfile.module.share.model.entity.ShareLink;
import im.zhaojun.zfile.module.share.model.request.CreateShareLinkRequest;
import im.zhaojun.zfile.module.share.model.request.ShareLinkListRequest;
import im.zhaojun.zfile.module.share.model.result.CreateShareLinkResult;
import im.zhaojun.zfile.module.share.model.result.ShareLinkResult;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.service.UserService;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = "shareLink")
public class ShareLinkService {

    @Resource
    private ShareLinkMapper shareLinkMapper;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserStorageSourceService userStorageSourceService;

    @Resource
    private UserService userService;

    /**
     * 缓冲区刷新阈值（当某个分享链接的访问或下载次数在缓冲区累积达到该值时，触发刷新操作，将数据写入数据库）
     */
    private static final int BUFFER_FLUSH_THRESHOLD = 10;

    /**
     * 缓冲区刷新间隔（定时任务每隔多长时间触发一次刷新操作，单位：毫秒）
     */
    private static final long BUFFER_FLUSH_INTERVAL_MILLIS = 5000L;

    private final ConcurrentMap<String, AtomicInteger> accessCountBuffer = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, AtomicInteger> downloadCountBuffer = new ConcurrentHashMap<>();

    private ScheduledExecutorService bufferFlushScheduler;

    @PostConstruct
    public void initBufferFlushScheduler() {
        bufferFlushScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "share-link-buffer-flusher");
            thread.setDaemon(true);
            return thread;
        });
        bufferFlushScheduler.scheduleAtFixedRate(this::flushAllBuffersSafely,
                BUFFER_FLUSH_INTERVAL_MILLIS,
                BUFFER_FLUSH_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdownBufferFlushScheduler() {
        if (bufferFlushScheduler != null) {
            bufferFlushScheduler.shutdown();
            try {
                if (!bufferFlushScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    bufferFlushScheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                bufferFlushScheduler.shutdownNow();
            }
        }
        flushAllBuffersSafely();
    }

    /**
     * 获取指定分享链接
     */
    @Cacheable(key = "'shareKey:' + #shareKey", condition = "#shareKey != null", unless = "#result == null")
    public ShareLink getByShareKey(String shareKey) {
        return shareLinkMapper.getByShareKey(shareKey);
    }

    /**
     * 删除分享链接
     */
    @CacheEvict(key = "'shareKey:' + #shareKey")
    public void deleteShareLink(String shareKey) {
        ShareLink shareLink = ((ShareLinkService) AopContext.currentProxy()).getByShareKey(shareKey);
        if (shareLink == null) {
            throw new BizException(ErrorCode.BIZ_SHARE_LINK_NOT_EXIST);
        }
        Integer currentUserId = ZFileAuthUtil.getCurrentUserId();
        boolean currentIsAdmin = Objects.equals(UserConstant.ADMIN_ID, currentUserId);
        boolean deleteIsCurrentUser = Objects.equals(shareLink.getUserId(), currentUserId);
        if (!deleteIsCurrentUser && !currentIsAdmin) {
            throw new BizException(ErrorCode.BIZ_STORAGE_SOURCE_ILLEGAL_OPERATION);
        }
        shareLinkMapper.deleteById(shareLink.getId());
    }

    /**
     * 删除过期的分享链接
     */
    @CacheEvict(allEntries = true)
    public int deleteExpiredLinks() {
        return shareLinkMapper.deleteExpiredLinks(new Date());
    }

    /**
     * 删除指定用户的过期分享链接
     */
    @CacheEvict(allEntries = true)
    public int deleteExpiredLinksByUserId(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return shareLinkMapper.deleteExpiredLinksByUserId(userId, new Date());
    }

    /**
     * 更新分享访问次数（先累加到缓冲区，满足条件后统一写入数据库）
     */
    public void incrementAccessCount(String shareKey) {
        bufferIncrement(shareKey, accessCountBuffer,
                (key, delta) -> shareLinkMapper.incrementAccessCount(key, delta), "访问");
    }

    /**
     * 更新分享下载次数（先累加到缓冲区，满足条件后统一写入数据库）
     */
    public void incrementDownloadCount(String shareKey) {
        bufferIncrement(shareKey, downloadCountBuffer,
                (key, delta) -> shareLinkMapper.incrementDownloadCount(key, delta), "下载");
    }

    private void bufferIncrement(String shareKey,
                                  ConcurrentMap<String, AtomicInteger> buffer,
                                  BiConsumer<String, Integer> flusher,
                                  String metricType) {
        if (StrUtil.isBlank(shareKey)) {
            return;
        }
        AtomicInteger counter = buffer.computeIfAbsent(shareKey, key -> new AtomicInteger());
        int current = counter.incrementAndGet();
        if (current >= BUFFER_FLUSH_THRESHOLD) {
            drainSingleEntry(shareKey, buffer, counter, flusher, metricType);
        }
    }

    private void flushAllBuffersSafely() {
        try {
            flushBuffer(accessCountBuffer,
                    (key, delta) -> shareLinkMapper.incrementAccessCount(key, delta), "访问");
            flushBuffer(downloadCountBuffer,
                    (key, delta) -> shareLinkMapper.incrementDownloadCount(key, delta), "下载");
        } catch (Exception ex) {
            log.warn("刷新分享链接访问/下载次数缓冲区失败", ex);
        }
    }

    private void flushBuffer(ConcurrentMap<String, AtomicInteger> buffer,
                              BiConsumer<String, Integer> flusher,
                              String metricType) {
        for (Map.Entry<String, AtomicInteger> entry : buffer.entrySet()) {
            drainSingleEntry(entry.getKey(), buffer, entry.getValue(), flusher, metricType);
        }
    }

    private void drainSingleEntry(String shareKey,
                                  ConcurrentMap<String, AtomicInteger> buffer,
                                  AtomicInteger counter,
                                  BiConsumer<String, Integer> flusher,
                                  String metricType) {
        int delta = counter.getAndSet(0);
        if (delta <= 0) {
            return;
        }
        try {
            flusher.accept(shareKey, delta);
            if (log.isDebugEnabled()) {
                log.debug("刷新分享链接 {} 的{}次数增量 {}", shareKey, metricType, delta);
            }
        } catch (Exception ex) {
            counter.addAndGet(delta);
            log.warn("刷新分享链接 {} 的{}增量 {} 失败", shareKey, metricType, delta, ex);
        } finally {
            if (counter.get() == 0) {
                buffer.remove(shareKey, counter);
            }
        }
    }

    // ======================== 业务方法 ========================

    /**
     * 创建分享链接
     */
    public CreateShareLinkResult createShareLink(CreateShareLinkRequest request) {
        // 生成或验证分享 key
        String shareKey = generateOrValidateShareKey(request.getShareKey(), request.getStorageKey());

        // 校验请求参数并获取文件服务
        AbstractBaseFileService<?> fileService = validateAndGetFileService(request);

        // 获取当前用户基础路径，存储分享路径，避免用户路径变更后分享链接失效的问题
        String absoluteSharePath = StringUtils.concat(fileService.getCurrentUserBasePath(), request.getSharePath());

        // 构建分享链接对象
        ShareLink shareLink = new ShareLink();
        shareLink.setShareKey(shareKey);
        shareLink.setPassword(request.getPassword());
        shareLink.setExpireDate(request.getExpireDate());
        shareLink.setStorageKey(request.getStorageKey());
        shareLink.setSharePath(absoluteSharePath);  // 存储绝对路径
        List<ShareEntryDTO> normalizedEntries = request.getShareEntries().stream()
                .map(entry -> {
                    ShareEntryDTO dto = new ShareEntryDTO();
                    String name = entry.getName() == null ? null : entry.getName().trim();
                    dto.setName(name);
                    dto.setType(entry.getType());
                    return dto;
                })
                .collect(Collectors.toList());

        shareLink.setShareItem(JSON.toJSONString(normalizedEntries));
        shareLink.setShareType(request.getShareType());
        shareLink.setUserId(ZFileAuthUtil.getCurrentUserId());
        shareLink.setCreateDate(new Date());

        // 保存到数据库
        shareLinkMapper.insert(shareLink);

        // 构建返回结果
        CreateShareLinkResult result = new CreateShareLinkResult();
        result.setShareKey(shareKey);
        result.setFullShareUrl(StringUtils.removeDuplicateSlashes(systemConfigService.getAxiosFromDomainOrSetting() + "/share/" + shareKey));
        return result;
    }

    /**
     * 根据分享 key 获取分享信息
     */
    public ShareLinkResult getShareLinkInfo(String shareKey) {
        ShareLink shareLink = getValidShareLink(shareKey);
        return buildShareLinkResult(shareLink, false);
    }

    /**
     * 验证分享密码
     */
    public boolean verifyPassword(String shareKey, String password) {
        ShareLink shareLink = getValidShareLink(shareKey);

        // 如果没有设置密码，则验证通过
        if (StrUtil.isBlank(shareLink.getPassword())) {
            return true;
        }

        return Objects.equals(shareLink.getPassword(), password);
    }

    /**
     * 获取有效分享链接
     *
     * @param shareKey 分享链接 key
     * @return 分享链接
     */
    public ShareLink getValidShareLink(String shareKey) {
        ShareLink shareLink = ((ShareLinkService) AopContext.currentProxy()).getByShareKey(shareKey);
        if (shareLink == null) {
            throw new BizException(ErrorCode.BIZ_SHARE_LINK_NOT_EXIST);
        }

        // 检查是否过期
        if (isExpired(shareLink)) {
            throw new BizException(ErrorCode.BIZ_SHARE_LINK_EXPIRED);
        }

        return shareLink;
    }


    /**
     * 获取用户创建的分享列表（分页）
     */
    public Page<ShareLinkResult> getUserShareList(ShareLinkListRequest request) {
        request.handleDefaultValue();

        Integer currentUserId = ZFileAuthUtil.getCurrentUserId();
        Page<ShareLinkResult> emptyPage = new Page<>(request.getPage(), request.getLimit());
        emptyPage.setRecords(Collections.emptyList());

        if (currentUserId == null) {
            emptyPage.setTotal(0);
            return emptyPage;
        }

        LambdaQueryWrapper<ShareLink> queryWrapper = buildShareListQueryWrapper(request);
        queryWrapper.eq(ShareLink::getUserId, currentUserId);

        Page<ShareLink> page = shareLinkMapper.selectPage(new Page<ShareLink>(request.getPage(), request.getLimit()).addOrder(request.getOrderItem()), queryWrapper);
        return buildShareResultPage(page, false);
    }

    /**
     * 管理员查询全部分享列表（分页）
     */
    public Page<ShareLinkResult> getAdminShareList(ShareLinkListRequest request) {
        request.handleDefaultValue();

        LambdaQueryWrapper<ShareLink> queryWrapper = buildShareListQueryWrapper(request);
        Page<ShareLink> page = shareLinkMapper.selectPage(new Page<ShareLink>(request.getPage(), request.getLimit()).addOrder(request.getOrderItem()), queryWrapper);
        return buildShareResultPage(page, true);
    }

    private LambdaQueryWrapper<ShareLink> buildShareListQueryWrapper(ShareLinkListRequest request) {
        LambdaQueryWrapper<ShareLink> queryWrapper = Wrappers.lambdaQuery();

        if (StrUtil.isNotBlank(request.getStorageKey())) {
            queryWrapper.eq(ShareLink::getStorageKey, request.getStorageKey().trim());
        }

        if (StrUtil.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper.like(ShareLink::getShareKey, keyword)
                    .or().like(ShareLink::getShareItem, keyword)
                    .or().like(ShareLink::getSharePath, keyword));
        }

        String status = StrUtil.blankToDefault(request.getStatus(), "all").toLowerCase(Locale.ROOT);
        Date now = new Date();
        switch (status) {
            case "expired" -> {
                queryWrapper.isNotNull(ShareLink::getExpireDate);
                queryWrapper.le(ShareLink::getExpireDate, now);
            }
            case "active" -> queryWrapper.and(wrapper -> wrapper.isNull(ShareLink::getExpireDate)
                    .or().gt(ShareLink::getExpireDate, now));
            default -> {
                // all, do nothing
            }
        }

        if (request.getCreateDateStart() != null) {
            queryWrapper.ge(ShareLink::getCreateDate, request.getCreateDateStart());
        }
        if (request.getCreateDateEnd() != null) {
            queryWrapper.le(ShareLink::getCreateDate, request.getCreateDateEnd());
        }

        return queryWrapper;
    }

    private Page<ShareLinkResult> buildShareResultPage(Page<ShareLink> page, boolean includeUserInfo) {
        Page<ShareLinkResult> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ShareLink> shareLinks = page.getRecords();

        Map<Integer, User> userMap;
        if (includeUserInfo && !shareLinks.isEmpty()) {
            userMap = new HashMap<>();
            shareLinks.stream()
                    .map(ShareLink::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(userId -> {
                        User user = userService.getById(userId);
                        if (user != null) {
                            userMap.put(userId, user);
                        }
                    });
        } else {
            userMap = Collections.emptyMap();
        }

        List<ShareLinkResult> records = shareLinks.stream()
                .map(item -> {
                    ShareLinkResult result = buildShareLinkResult(item, true);
                    if (includeUserInfo) {
                        result.setUserId(item.getUserId());
                        User user = userMap.get(item.getUserId());
                        if (user != null) {
                            result.setUsername(user.getUsername());
                            result.setNickname(user.getNickname());
                        }
                    }
                    return result;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }



    /**
     * 判断分享链接是否过期
     */
    private boolean isExpired(ShareLink shareLink) {
        if (shareLink.getExpireDate() == null) {
            return false;
        }
        return new Date().after(shareLink.getExpireDate());
    }

    /**
     * 构建分享链接结果
     *
     * @param shareLink 分享链接实体
     * @return 分享链接结果
     */
    private ShareLinkResult buildShareLinkResult(ShareLink shareLink, boolean includeSensitive) {
        ShareLinkResult result = new ShareLinkResult();
        BeanUtils.copyProperties(shareLink, result);

        if (includeSensitive) {
            result.setPassword(shareLink.getPassword());
        } else {
            result.setPassword(null);
        }

        // 解析分享条目
        if (StrUtil.isNotBlank(shareLink.getShareItem())) {
            try {
                List<ShareEntryDTO> shareEntries = JSON.parseArray(shareLink.getShareItem(), ShareEntryDTO.class);
                result.setShareEntries(shareEntries == null ? List.of() : shareEntries);
            } catch (Exception e) {
                result.setShareEntries(List.of());
            }
        } else {
            result.setShareEntries(List.of());
        }

        result.setNeedPassword(StrUtil.isNotBlank(shareLink.getPassword()));
        result.setExpired(isExpired(shareLink));

        if (StrUtil.isNotBlank(shareLink.getStorageKey())) {
            StorageSource storageSource = storageSourceService.findByStorageKey(shareLink.getStorageKey());
            if (storageSource != null) {
                result.setStorageId(storageSource.getId());
                result.setStorageName(storageSource.getName());
            }
        }

        return result;
    }

    /**
     * 校验创建分享链接请求参数并获取文件服务
     *
     * @param request 创建分享链接请求
     * @return 文件服务实例
     */
    private AbstractBaseFileService<?> validateAndGetFileService(CreateShareLinkRequest request) {
        String storageKey = request.getStorageKey();

        // 验证存储源是否存在
        if (!storageSourceService.existByStorageKey(storageKey)) {
            throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
        }

        // 获取文件服务实例
        AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(storageKey);
        if (fileService == null) {
            throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
        }

        // 验证过期时间是否合法
        if (request.getExpireDate() != null && request.getExpireDate().before(new Date())) {
            throw new BizException(ErrorCode.BIZ_SHARE_LINK_EXPIRY_MUST_BE_FUTURE);
        }

        return fileService;
    }

    /**
     * 生成或验证分享 key
     *
     * @param customShareKey 自定义分享 key，可以为空
     * @return 有效的分享 key
     */
    private String generateOrValidateShareKey(String customShareKey, String storageKey) {
        // 如果提供了自定义 key，则验证是否可用
        if (StrUtil.isNotBlank(customShareKey)) {
            return validateCustomShareKey(customShareKey, storageKey);
        }

        // 如果没有提供自定义 key，则自动生成
        return generateShareKey();
    }

    /**
     * 验证自定义分享 key
     *
     * @param customShareKey 自定义分享 key
     * @param storageKey     存储源 key
     * @return 验证通过的分享 key
     */
    private String validateCustomShareKey(String customShareKey, String storageKey) {
        // 检查用户是否有使用自定义分享 key 的权限
        Integer storageId = storageSourceService.findIdByKey(storageKey);
        if (!hasCustomKeyPermission(storageId)) {
            throw new BizException(ErrorCode.NO_CUSTOM_SHARE_LINK_KEY_PERMISSION);
        }

        // 验证格式：只能包含字母、数字、下划线和短横线，长度 3-8
        if (!customShareKey.matches("^[a-zA-Z0-9_-]{3,8}$")) {
            throw new BizException(ErrorCode.BIZ_CUSTOM_SHARE_LINK_KEY_FORMAT_ILLEGAL);
        }

        // 验证是否已存在
        if (((ShareLinkService) AopContext.currentProxy()).getByShareKey(customShareKey) != null) {
            throw new BizException(ErrorCode.BIZ_SHARE_LINK_KEY_ALREADY_EXIST);
        }

        return customShareKey;
    }

    /**
     * 生成分享 key
     */
    private String generateShareKey() {
        String shareKey;
        do {
            shareKey = IdUtil.randomUUID().replace("-", "").substring(0, 8);
        } while (((ShareLinkService) AopContext.currentProxy()).getByShareKey(shareKey) != null);
        return shareKey;
    }

    /**
     * 检查用户是否有使用自定义分享 key 的权限
     *
     * @param storageId 存储源 ID
     * @return 是否有权限
     */
    private boolean hasCustomKeyPermission(Integer storageId) {
        Integer currentUserId = ZFileAuthUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        UserStorageSource userStorageSource = userStorageSourceService.getByUserIdAndStorageId(currentUserId, storageId);
        if (userStorageSource == null || !Boolean.TRUE.equals(userStorageSource.getEnable())) {
            return false;
        }

        return userStorageSource.getPermissions().contains(FileOperatorTypeEnum.CUSTOM_SHARE_KEY.getValue());
    }

}
