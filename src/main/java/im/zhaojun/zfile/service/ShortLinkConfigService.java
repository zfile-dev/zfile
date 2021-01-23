package im.zhaojun.zfile.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.model.entity.ShortLinkConfig;
import im.zhaojun.zfile.repository.ShortLinkConfigRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ShortLinkConfigService {

    @Resource
    private ShortLinkConfigRepository shortLinkConfigRepository;

    public ShortLinkConfig findByKey(String key) {
        return shortLinkConfigRepository.findByKey(key);
    }

    public void save(ShortLinkConfig shortLinkConfig) {
        shortLinkConfig.setCreateDate(new Date());
        shortLinkConfigRepository.save(shortLinkConfig);
    }

    public Page<ShortLinkConfig> find(String key,
                                      String url,
                                      String dateFrom,
                                      String dateTo,
                                      Integer page,
                                      Integer limit,
                                      String orderBy,
                                      String orderDirection) {

        Sort sort = Sort.by("desc".equals(orderDirection) ? Sort.Direction.DESC : Sort.Direction.ASC, orderBy);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Specification<ShortLinkConfig> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotEmpty(dateFrom) && StrUtil.isNotEmpty(dateTo)) {
                predicates.add(criteriaBuilder.between(root.get("createDate"),
                        DateUtil.parseDateTime(dateFrom + " 00:00:00"),
                        DateUtil.parseDateTime(dateTo + " 23:59:59")));
            }

            if (StrUtil.isNotEmpty(key)) {
                predicates.add(criteriaBuilder.like(root.get("key"), "%" + key + '%'));
            }

            if (StrUtil.isNotEmpty(url)) {
                predicates.add(criteriaBuilder.like(root.get("url"), "%" + url + '%'));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return shortLinkConfigRepository.findAll(specification, pageable);
    }

    public void deleteById(Integer id) {
        shortLinkConfigRepository.deleteById(id);
    }


}