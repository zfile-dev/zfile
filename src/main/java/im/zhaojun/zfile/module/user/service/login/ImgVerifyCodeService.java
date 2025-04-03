package im.zhaojun.zfile.module.user.service.login;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.lang.UUID;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.status.ForbiddenAccessException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.user.model.result.LoginVerifyImgResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
		String code = null;
		try {
			code = captcha.getCode();
		} catch (Exception e) {
			if (StringUtils.contains(e.getMessage(), "Fontconfig")) {
				throw new BizException("验证码生成失败, 请安装字体库后重试，参考文档: https://docs.zfile.vip/question/ubuntu-awt");
			}
		}
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
		return StringUtils.equalsIgnoreCase(expectedCode, code);
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
			throw new ForbiddenAccessException(ErrorCode.BIZ_VERIFY_CODE_ERROR);
		}
	}


}