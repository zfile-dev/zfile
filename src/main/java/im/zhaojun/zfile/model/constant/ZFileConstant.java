package im.zhaojun.zfile.model.constant;

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

    public static final Character PATH_SEPARATOR_CHAR = '/';

    public static final String PATH_SEPARATOR = "/";

    /**
     * 页面文档文件
     */
    public static String README_FILE_NAME = "readme.md";

    /**
     * 密码文件
     */
    public static String PASSWORD_FILE_NAME = "password.txt";

    /**
     * 最大支持文件大小为 ? MB 的音乐文件解析封面, 歌手等信息.
     */
    public static Long AUDIO_MAX_FILE_SIZE_MB = 1L;

    /**
     * 最大支持文本文件大小为 ? KB 的文件内容.
     */
    public static Long TEXT_MAX_FILE_SIZE_KB = 100L;


    @Autowired(required = false)
    public void setHeaderFileName(@Value("${zfile.constant.readme}") String headerFileName) {
        ZFileConstant.README_FILE_NAME = headerFileName;
    }

    @Autowired(required = false)
    public void setPasswordFileName(@Value("${zfile.constant.password}") String passwordFileName) {
        ZFileConstant.PASSWORD_FILE_NAME = passwordFileName;
    }

    @Autowired(required = false)
    public void setAudioMaxFileSizeMb(@Value("${zfile.preview.audio.maxFileSizeMb}") Long maxFileSizeMb) {
        ZFileConstant.AUDIO_MAX_FILE_SIZE_MB = maxFileSizeMb;
    }

    @Autowired(required = false)
    public void setTextMaxFileSizeMb(@Value("${zfile.preview.text.maxFileSizeKb}") Long maxFileSizeKb) {
        ZFileConstant.TEXT_MAX_FILE_SIZE_KB = maxFileSizeKb;
    }


}
