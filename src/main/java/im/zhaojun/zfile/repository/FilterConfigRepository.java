package im.zhaojun.zfile.repository;

import im.zhaojun.zfile.model.entity.FilterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhaojun
 */
@Repository
public interface FilterConfigRepository extends JpaRepository<FilterConfig, Integer> {

    /**
     * 获取驱动器下的所有规则
     * @param       driveId
     *              驱动器 ID
     */
    List<FilterConfig> findByDriveId(Integer driveId);

    /**
     * 根据驱动器 ID 删除其所有的规则
     * @param       driveId
     *              驱动器 ID
     */
    void deleteByDriveId(Integer driveId);


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
    @Query(value="update FILTER_CONFIG set driveId = :newId where driveId = :updateId")
    void updateDriveId(Integer updateId, Integer newId);
}