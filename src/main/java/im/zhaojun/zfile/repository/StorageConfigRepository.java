package im.zhaojun.zfile.repository;

import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhaojun
 */
@Repository
public interface StorageConfigRepository extends JpaRepository<StorageConfig, Integer> {

    /**
     * 根据存储类型找对应的配置信息
     *
     * @param   type
     *          存储类型
     *
     * @return  此类型所有的配置信息
     */
    List<StorageConfig> findByTypeOrderById(StorageTypeEnum type);


    /**
     * 根据存储类型找对应的配置信息
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  此驱动器所有的配置信息
     */
    List<StorageConfig> findByDriveIdOrderById(Integer driveId);


    /**
     * 根据驱动器找到对应的配置信息
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  此驱动器所有的配置信息
     */
    List<StorageConfig> findByDriveId(Integer driveId);


    /**
     * 删除指定驱动器对应的配置信息
     *
     * @param   driveId
     *          驱动器 ID
     */
    void deleteByDriveId(Integer driveId);


    /**
     * 查找某个驱动器的某个 KEY 的值
     *
     * @param   driveId
     *          驱动器
     *
     * @param   key
     *          KEY 值
     *
     * @return  KEY 对应的对象
     */
    StorageConfig findByDriveIdAndKey(Integer driveId, String key);


    /**
     * 更新驱动器 ID 对应的参数设置为新的驱动器 ID
     *
     * @param   updateId
     *          驱动器原 ID
     *
     * @param   newId
     *          驱动器新 ID
     */
    @Modifying
    @Query(value="update STORAGE_CONFIG set driveId = :newId where driveId = :updateId")
    void updateDriveId(Integer updateId, Integer newId);

}