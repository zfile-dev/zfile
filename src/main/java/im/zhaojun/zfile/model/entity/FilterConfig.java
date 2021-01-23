package im.zhaojun.zfile.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author zhaojun
 */
@Entity(name = "FILTER_CONFIG")
@Data
public class FilterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer driveId;

    private String expression;

}