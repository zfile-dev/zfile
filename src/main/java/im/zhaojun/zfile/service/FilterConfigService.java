package im.zhaojun.zfile.service;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.model.entity.FilterConfig;
import im.zhaojun.zfile.repository.FilterConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class FilterConfigService {

    @Resource
    private FilterConfigRepository filterConfigRepository;

    public List<FilterConfig> findByDriveId(Integer driveId) {
        return filterConfigRepository.findByDriveId(driveId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<FilterConfig> filterConfigList, Integer driveId) {
        filterConfigRepository.deleteByDriveId(driveId);
        filterConfigRepository.saveAll(filterConfigList);
    }

    /**
     * 指定驱动器下的文件名称, 根据过滤表达式判断是否会显示, 如果符合任意一条表达式, 则不显示, 反之则显示.
     * @param   driveId
     *          驱动器 ID
     * @param   fileName
     *          文件名
     * @return  是否显示
     */
    public boolean filterResultIsHidden(Integer driveId, String fileName) {
        List<FilterConfig> filterConfigList = findByDriveId(driveId);

        for (FilterConfig filterConfig : filterConfigList) {
            String expression = filterConfig.getExpression();
            if (StrUtil.isEmpty(expression)) {
                return false;
            }

            try {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + expression);
                boolean match = pathMatcher.matches(Paths.get(fileName));
                if (match) {
                    return true;
                }
                log.debug("regex: {}, name {}, contains: {}", expression, fileName, match);
            } catch (Exception e) {
                log.debug("regex: {}, name {}, parse error, skip expression", expression, fileName);
            }
        }

        return false;
    }

}