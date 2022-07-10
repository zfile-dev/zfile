package im.zhaojun.zfile.home.service.base;

/**
 * 需要刷新 Token 服务的存储源
 *
 * @author zhaojun
 */
public interface RefreshTokenService {

	void refreshAccessToken() throws Exception;

}