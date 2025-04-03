package im.zhaojun.zfile.module.link.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CacheInfo<K, V> {

    private K key;

    private V value;

    private Date expiredTime;

    private Long ttl;

}
