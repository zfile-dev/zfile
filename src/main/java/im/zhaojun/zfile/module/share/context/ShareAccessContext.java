package im.zhaojun.zfile.module.share.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * 分享访问上下文，用于在分享访问时传递相关信息
 * 解决分享访问时绕过用户基础路径限制的问题
 *
 * @author zhaojun
 */
@Component
public class ShareAccessContext {
    
    private static final ThreadLocal<ShareAccessInfo> CONTEXT = new ThreadLocal<>();
    
    /**
     * 分享访问信息
     */
    @Getter
    @AllArgsConstructor
    public static class ShareAccessInfo {
        private boolean isShareAccess;
        private String shareBasePath;
        private String shareKey;
        private Integer shareUserId;
    }
    
    /**
     * 设置分享访问上下文
     *
     * @param shareKey      分享链接 key
     * @param shareBasePath 分享的基础路径
     */
    public static void setShareAccess(String shareKey, String shareBasePath) {
        setShareAccess(shareKey, shareBasePath, null);
    }

    /**
     * 设置分享访问上下文（带分享者用户ID）
     *
     * @param shareKey      分享链接 key
     * @param shareBasePath 分享的基础路径
     * @param shareUserId   分享者用户ID
     */
    public static void setShareAccess(String shareKey, String shareBasePath, Integer shareUserId) {
        CONTEXT.set(new ShareAccessInfo(true, shareBasePath, shareKey, shareUserId));
    }
    
    /**
     * 检查当前是否为分享访问
     *
     * @return 是否为分享访问
     */
    public static boolean isShareAccess() {
        ShareAccessInfo info = CONTEXT.get();
        return info != null && info.isShareAccess;
    }
    
    /**
     * 获取分享的基础路径
     *
     * @return 分享基础路径，如果不是分享访问则返回 null
     */
    public static String getShareBasePath() {
        ShareAccessInfo info = CONTEXT.get();
        return info != null ? info.shareBasePath : null;
    }
    
    /**
     * 获取分享链接 key
     *
     * @return 分享链接 key，如果不是分享访问则返回 null
     */
    public static String getShareKey() {
        ShareAccessInfo info = CONTEXT.get();
        return info != null ? info.shareKey : null;
    }

    /**
     * 获取分享者用户ID
     *
     * @return 分享者用户ID，如果不是分享访问则返回 null
     */
    public static Integer getShareUserId() {
        ShareAccessInfo info = CONTEXT.get();
        return info != null ? info.shareUserId : null;
    }
    
    /**
     * 清理当前线程的分享访问上下文
     * 必须在分享访问结束后调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }
    
    /**
     * 获取当前分享访问信息
     *
     * @return 分享访问信息，如果不是分享访问则返回 null
     */
    public static ShareAccessInfo getCurrentInfo() {
        return CONTEXT.get();
    }
}
