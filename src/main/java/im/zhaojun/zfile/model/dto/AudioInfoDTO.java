package im.zhaojun.zfile.model.dto;

/**
 * @author zhaojun
 */
public class AudioInfoDTO {
    private String title;
    private String artist;
    private String cover;
    private String src;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    @Override
    public String toString() {
        return "AudioInfoDTO{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", cover='" + cover + '\'' +
                ", src='" + src + '\'' +
                '}';
    }
}
