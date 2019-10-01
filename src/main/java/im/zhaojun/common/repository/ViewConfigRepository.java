package im.zhaojun.common.repository;

import im.zhaojun.common.model.ViewConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewConfigRepository extends JpaRepository<ViewConfig, Integer> {

    public ViewConfig findFirstBy();

}
