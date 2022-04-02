package im.zhaojun.zfile.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "SHORT_LINK")
@Data
public class ShortLinkConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String key;

    private String url;

    private Date createDate;

}