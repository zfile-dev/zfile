package im.zhaojun.common.util;

import cn.hutool.core.codec.Base64;
import com.mpatric.mp3agic.*;
import im.zhaojun.common.model.AudioInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AudioHelper {

    private static final Logger log = LoggerFactory.getLogger(AudioHelper.class);

    public static AudioInfo parseAudioInfo(File file) throws InvalidDataException, IOException, UnsupportedTagException {
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setTitle("未知歌曲");
        audioInfo.setArtist("未知");
        audioInfo.setCover("/shikwasa/audio.png");

        Mp3File mp3File = null;
        try {
            mp3File = new Mp3File(file);
        } catch (InvalidDataException e) {
            if (log.isDebugEnabled()) {
                log.debug("无法解析的音频文件.");
            }
        }

        if (mp3File == null) {
            return audioInfo;
        }

        ID3v1 audioTag = null;

        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            byte[] albumImage = id3v2Tag.getAlbumImage();
            if (albumImage != null) {
                audioInfo.setCover("data:" + id3v2Tag.getAlbumImageMimeType() + ";base64," + Base64.encode(albumImage));
            }
            audioTag = id3v2Tag;
        }

        if (audioTag != null) {
            audioInfo.setTitle(audioTag.getTitle());
            audioInfo.setArtist(audioTag.getArtist());
        }

        return audioInfo;
    }
}
