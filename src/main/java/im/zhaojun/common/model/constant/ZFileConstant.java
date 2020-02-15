package im.zhaojun.common.model.constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhaojun
 */
@Configuration
public class ZFileConstant {

    public final static String USER_HOME = System.getProperty("user.home");

    public static final String AUDIO_TMP_PATH = "/.zfile/tmp/audio/";

    /**
     * 页面文档文件
     */
    public static String README_FILE_NAME = "readme.md";

    /**
     * 密码文件
     */
    public static String PASSWORD_FILE_NAME = "password.txt";

    @Autowired(required = false)
    public void setHeaderFileName(@Value("${zfile.constant.readme}") String headerFileName) {
        ZFileConstant.README_FILE_NAME = headerFileName;
    }

    @Autowired(required = false)
    public void setPasswordFileName(@Value("${zfile.constant.password}") String passwordFileName) {
        ZFileConstant.PASSWORD_FILE_NAME = passwordFileName;
    }

}
