package im.zhaojun.zfile.repository;

import im.zhaojun.zfile.model.entity.ShortLinkConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author zhaojun
 */
@Repository
public interface ShortLinkConfigRepository extends JpaRepository<ShortLinkConfig, Integer>, JpaSpecificationExecutor<ShortLinkConfig> {

    /**
     * 获取驱动器下的所有规则
     *
     * @param       key
     *              驱动器 ID
     */
    ShortLinkConfig findByKey(String key);

    @Query(nativeQuery = true,
            value = " select * from SHORT_LINK where " +
                    " key like concat('%', :key,'%') " +
                    " and url like concat('%', :url,'%') " +
                    " and (:dateFrom is null or create_date >= :dateFrom" +
                    " and (:dateTo is null or create_date <= :dateTo) ",
            countQuery =  " select count(1) from SHORT_LINK where " +
                    " key like concat('%', :key,'%') " +
                    " and url like concat('%', :url,'%') " +
                    " and (:dateFrom is null or create_date >= :dateFrom" +
                    " and (:dateTo is null or create_date <= :dateTo) "
    )
    // @Query(nativeQuery = true,
    //         value = " select * from SHORT_LINK where " +
    //                 " key like concat('%', :key,'%') " +
    //                 " and url like concat('%', :url,'%') " +
    //                 " and (:dateFrom is null or date_format(create_date, '%Y-%m-%d') >= date_format(:dateFrom, '%Y-%m-%d'))" +
    //                 " and (:dateTo is null or date_format(create_date, '%Y-%m-%d') <= date_format(:dateTo, '%Y-%m-%d')) ) ",
    //         countQuery =  " select count(1) from SHORT_LINK where " +
    //                 " key like concat('%', :key,'%') " +
    //                 " and url like concat('%', :url,'%') " +
    //                 " and (:dateFrom  is null or date_format(create_date, '%Y-%m-%d') >= date_format(:dateFrom, '%Y-%m-%d'))" +
    //                 " and (:dateTo is null or date_format(create_date, '%Y-%m-%d') <= date_format(:dateTo, '%Y-%m-%d')) ) "
    // )
    Page<ShortLinkConfig> findByPage(String key, String url, Date dateFrom, Date dateTo, Pageable pageable);
}