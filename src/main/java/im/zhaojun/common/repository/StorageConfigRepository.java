package im.zhaojun.common.repository;

import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.StorageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageConfigRepository extends JpaRepository<StorageConfig, Integer> {

    public List<StorageConfig> findByType(StorageTypeEnum type);

}