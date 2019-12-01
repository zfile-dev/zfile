package im.zhaojun.common.repository;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageConfigRepository extends JpaRepository<StorageConfig, Integer> {

    List<StorageConfig> findByTypeOrderById(StorageTypeEnum type);

}