package im.zhaojun.common.repository;

import im.zhaojun.common.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer> {

    public SystemConfig findFirstBy();

}
