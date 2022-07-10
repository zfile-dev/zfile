package im.zhaojun.zfile.common.constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ZFile 常量
 *
 * @author zhaojun
 */
@Configuration
public class ZFileConstant {

    public static final Character PATH_SEPARATOR_CHAR = '/';

    public static final String PATH_SEPARATOR = "/";


    /**
     * 系统产生的临时文件路径
     */
    public static String TEMP_FILE_PATH = "/.zfile/temp/";


    /**
     * 最大支持文件大小为 ? MB 的音乐文件解析封面, 歌手等信息.
     */
    public static Long AUDIO_MAX_FILE_SIZE_MB = 1L;


    /**
     * 最大支持文本文件大小为 ? KB 的文件内容.
     */
    public static Long TEXT_MAX_FILE_SIZE_KB = 100L;


    @Autowired(required = false)
    public void setTmpFilePath(@Value("${zfile.temp.path}") String tmpFilePath) {
        ZFileConstant.TEMP_FILE_PATH = tmpFilePath;
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