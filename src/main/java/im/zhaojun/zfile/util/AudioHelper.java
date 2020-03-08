package im.zhaojun.zfile.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.AudioInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * 音频解析工具类
 * @author zhaojun
 */
public class AudioHelper {

    private static final Logger log = LoggerFactory.getLogger(AudioHelper.class);

    public static AudioInfoDTO getAudioInfo(String url) throws Exception {
        String query = new URL(URLUtil.decode(url)).getQuery();

        if (query != null) {
            url = url.replace(query, URLUtil.encode(query));
        }

        File file = new File(ZFileConstant.USER_HOME + ZFileConstant.AUDIO_TMP_PATH + UUID.fastUUID());
        FileUtil.mkParentDirs(file);
        HttpUtil.downloadFile(url, file);
        AudioInfoDTO audioInfoDTO = parseAudioInfo(file);
        audioInfoDTO.setSrc(url);
        file.deleteOnExit();
        return audioInfoDTO;
    }

    private static AudioInfoDTO parseAudioInfo(File file) throws IOException, UnsupportedTagException {
        AudioInfoDTO audioInfoDTO = new AudioInfoDTO();
        audioInfoDTO.setTitle("未知歌曲");
        audioInfoDTO.setArtist("未知");
        audioInfoDTO.setCover("http://c.jun6.net/audio.png");

        Mp3File mp3File = null;
        try {
            mp3File = new Mp3File(file);
        } catch (InvalidDataException e) {
            if (log.isDebugEnabled()) {
                log.debug("无法解析的音频文件.");
            }
        }

        if (mp3File == null) {
            return audioInfoDTO;
        }

        ID3v1 audioTag = null;

        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            byte[] albumImage = id3v2Tag.getAlbumImage();
            if (albumImage != null) {
                audioInfoDTO.setCover("data:" + id3v2Tag.getAlbumImageMimeType() + ";base64," + Base64.encode(albumImage));
            }
            audioTag = id3v2Tag;
        }

        if (audioTag != null) {
            audioInfoDTO.setTitle(audioTag.getTitle());
            audioInfoDTO.setArtist(audioTag.getArtist());
        }

        return audioInfoDTO;
    }
}
