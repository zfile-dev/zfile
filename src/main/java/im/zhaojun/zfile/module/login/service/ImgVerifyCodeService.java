package im.zhaojun.zfile.module.login.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.lang.UUID;
import im.zhaojun.zfile.core.exception.LoginVerifyException;
import im.zhaojun.zfile.module.login.model.result.LoginVerifyImgResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 图片验证码 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
public class ImgVerifyCodeService {

	/**
	 * 最大容量为 100 的验证码缓存，防止恶意请求占满内存. 验证码有效期为 60 秒.
	 */
	private final FIFOCache<String, String> verifyCodeCache = CacheUtil.newFIFOCache(100,60 * 1000L);


	/**
	 * 生成验证码，并写入缓存中.
	 *
	 * @return  验证码生成结果
	 */
	public LoginVerifyImgResult generatorCaptcha() {
		CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 45, 4, 7);
		String code = captcha.getCode();
		String imageBase64 = captcha.getImageBase64Data();

		String uuid = UUID.fastUUID().toString();
		verifyCodeCache.put(uuid, code);

		LoginVerifyImgResult loginVerifyImgResult = new LoginVerifyImgResult();
		loginVerifyImgResult.setImgBase64(imageBase64);
		loginVerifyImgResult.setUuid(uuid);
		return loginVerifyImgResult;
	}


	/**
	 * 对验证码进行验证.
	 *
	 * @param   uuid
	 *          验证码 uuid
	 *
	 * @param   code
	 *          验证码
	 *
	 * @return  是否验证成功
	 */
	public boolean verifyCaptcha(String uuid, String code) {
		String expectedCode = verifyCodeCache.get(uuid);
		return Objects.equals(expectedCode, code);
	}


	/**
	 * 对验证码进行验证, 如验证失败则抛出异常
	 *
	 * @param   uuid
	 *          验证码 uuid
	 *
	 * @param   code
	 *          验证码
	 */
	public void checkCaptcha(String uuid, String code) {
		boolean flag = verifyCaptcha(uuid, code);
		if (!flag) {
			throw new LoginVerifyException("验证码错误或已失效.");
		}
	}


}