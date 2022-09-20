package im.zhaojun.zfile.module.storage.model.bo;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 用于存储刷新 Token 信息的缓存
 *
 * @author zhaojun
 */
@ToString
public class RefreshTokenCacheBO {

	private static final Cache<Integer, RefreshTokenInfo> REFRESH_TOKEN_INFO_CACHE = CacheUtil.newFIFOCache(100);

	public static void putRefreshTokenInfo(Integer storageId, RefreshTokenInfo lastRefreshTime) {
		REFRESH_TOKEN_INFO_CACHE.put(storageId, lastRefreshTime);
	}

	public static RefreshTokenInfo getRefreshTokenInfo(Integer storageId) {
		return REFRESH_TOKEN_INFO_CACHE.get(storageId);
	}

	@Data
	public static class RefreshTokenInfo {

		private boolean success;

		private Date lastRefreshTime;

		private String msg;


		public static RefreshTokenInfo success() {
			RefreshTokenInfo info = new RefreshTokenInfo();
			info.setSuccess(true);
			info.setLastRefreshTime(new Date());
			return info;
		}

		public static RefreshTokenInfo fail(String msg) {
			RefreshTokenInfo info = new RefreshTokenInfo();
			info.setSuccess(false);
			info.setMsg(msg);
			info.setLastRefreshTime(new Date());
			return info;
		}

	}

}