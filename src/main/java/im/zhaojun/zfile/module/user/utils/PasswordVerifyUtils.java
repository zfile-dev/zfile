package im.zhaojun.zfile.module.user.utils;

import cn.hutool.crypto.SecureUtil;
import im.zhaojun.zfile.core.util.StringUtils;
import org.springframework.data.util.Pair;

import java.util.Objects;

/**
 * @author zhaojun
 */
public class PasswordVerifyUtils {

    public static boolean verify(String dbPassword, String dbSalt, String requestPassword) {
        // 根据是否有盐值, 选择加密方式（兼容旧版本没有盐值的情况）
        String encryptedPassword;
        if (StringUtils.isBlank(dbSalt)) {
            encryptedPassword = SecureUtil.md5(requestPassword);
        } else {
            String sha1Pwd = SecureUtil.sha1(requestPassword);
            encryptedPassword = SecureUtil.md5(sha1Pwd + dbSalt);
        }
        return Objects.equals(dbPassword, encryptedPassword);
    }

    public static Pair<String, String> encrypt(String password) {
        String sha1Pwd = SecureUtil.sha1(password);
        String randomSalt = SecureUtil.md5(SecureUtil.sha1(String.valueOf(System.currentTimeMillis())));
        return Pair.of(SecureUtil.md5(sha1Pwd + randomSalt), randomSalt);
    }

}