package im.zhaojun.zfile.module.storage.service.base;

/**
 * 需要刷新 Token 服务的存储源
 *
 * @author zhaojun
 */
public interface RefreshTokenService extends BaseFileService {
	
	/**
	 * 刷新存储源 AccessToken 或者其他需要定时刷新的 Token
	 *
	 * @throws Exception	刷新 Token 时出现的异常
	 */
	void refreshAccessToken() throws Exception;

}