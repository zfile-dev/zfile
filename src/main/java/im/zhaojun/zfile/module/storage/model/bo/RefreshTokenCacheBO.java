package im.zhaojun.zfile.module.storage.model.bo;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import im.zhaojun.zfile.module.storage.model.dto.RefreshTokenInfoDTO;
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

	private static final Cache<Integer, RefreshTokenInfo> REFRESH_TOKEN_INFO_CACHE = CacheUtil.newFIFOCache(1024);

	public static void putRefreshTokenInfo(Integer storageId, RefreshTokenInfo refreshTokenInfo) {
		refreshTokenInfo.setStorageId(storageId);
		REFRESH_TOKEN_INFO_CACHE.put(storageId, refreshTokenInfo);
	}

	public static RefreshTokenInfo getRefreshTokenInfo(Integer storageId) {
		return REFRESH_TOKEN_INFO_CACHE.get(storageId);
	}

	@Data
	public static class RefreshTokenInfo {

		private Integer storageId;

		private boolean success;

		private Date lastRefreshTime;

		private String msg;

		private RefreshTokenInfoDTO data;

		public static RefreshTokenInfo success(RefreshTokenInfoDTO data) {
			RefreshTokenInfo info = new RefreshTokenInfo();
			info.setSuccess(true);
			info.setLastRefreshTime(new Date());
			info.setData(data);
			return info;
		}

		public static RefreshTokenInfo fail(String msg) {
			RefreshTokenInfo info = new RefreshTokenInfo();
			info.setSuccess(false);
			info.setMsg(msg);
			info.setLastRefreshTime(new Date());
			return info;
		}

		public boolean isExpired() {
			if (!success) {
				return true;
			}

			if (lastRefreshTime == null) {
				return true;
			}

			if (data == null || data.getExpiredAt() == null) {
				return true;
			}

			long expireTime = data.getExpiredAt() * 1000L;
			long currentTime = System.currentTimeMillis();
			long timeDiff = expireTime - currentTime;
			return timeDiff < 5 * 60 * 1000L;
		}

	}

}