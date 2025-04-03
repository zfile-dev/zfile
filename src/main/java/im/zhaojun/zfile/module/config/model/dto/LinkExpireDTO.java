package im.zhaojun.zfile.module.config.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LinkExpireDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer value;

    private String unit;

    private Long seconds;

}
