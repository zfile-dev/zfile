package im.zhaojun.zfile.repository;

import im.zhaojun.zfile.model.entity.DriveConfig;
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
public interface DriverConfigRepository extends JpaRepository<DriveConfig, Integer> {

    /**
     * 根据存储策略类型获取所有驱动器
     *
     * @param   type
     *          存储类型
     *
     * @return  指定存储类型的驱动器
     */
    List<DriveConfig> findByType(StorageTypeEnum type);


    /**
     * 更新驱动器 ID 的排序值
     *
     * @param   orderNum
     *          排序值
     *
     * @param   id
     *          驱动器 ID
     */
    @Modifying
    @Query(value="update DRIVER_CONFIG set orderNum = :orderNum where id = :id")
    void updateSetOrderNumById(Integer orderNum, Integer id);


    /**
     * 查询驱动器最大的 ID
     *
     * @return  驱动器最大 ID
     */
    @Query(nativeQuery = true, value = "select max(id) max from DRIVER_CONFIG")
    Integer selectMaxId();


    /**
     * 更新驱动器 ID
     *
     * @param   updateId
     *          驱动器原 ID
     *
     * @param   newId
     *          驱动器新 ID
     */
    @Modifying
    @Query(value="update DRIVER_CONFIG set id = :newId where id = :updateId")
    void updateId(Integer updateId, Integer newId);

}