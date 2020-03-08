package im.zhaojun.zfile.repository;

import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhaojun
 */
@Repository
public interface StorageConfigRepository extends JpaRepository<StorageConfig, Integer> {

    /**
     * 根据存储类型找对应的配置信息
     * @param type  存储类型
     * @return      此类型所有的配置信息
     */
    List<StorageConfig> findByTypeOrderById(StorageTypeEnum type);

    /**
     * 根据存储类型找到某个 KEY 的值
     * @param type  存储类型
     * @param key   KEY
     * @return      KEY 对应的对象
     */
    StorageConfig findByTypeAndKey(StorageTypeEnum type, String key);

}