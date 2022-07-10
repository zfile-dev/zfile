package im.zhaojun.zfile.admin.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.FilterConfigMapper;
import im.zhaojun.zfile.admin.model.entity.FilterConfig;
import im.zhaojun.zfile.common.exception.FileAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储源过滤器 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class FilterConfigService extends ServiceImpl<FilterConfigMapper, FilterConfig> {

    @Resource
    private FilterConfigMapper filterConfigMapper;


    /**
     * 存储源 ID -> 过滤器列表(全部)缓存.
     */
    private final Map<Integer, List<FilterConfig>> baseCache = new HashMap<>();

    /**
     * 存储源 ID -> 过滤器列表(禁止访问)缓存.
     */
    private final Map<Integer, List<FilterConfig>> inaccessibleCache = new HashMap<>();

    /**
     * 存储源 ID -> 过滤器列表(禁止下载)缓存.
     */
    private final Map<Integer, List<FilterConfig>> disableDownloadCache = new HashMap<>();


    /**
     * 根据存储源 ID 获取存储源配置列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源过滤器配置列表
     */
    public List<FilterConfig> findByStorageId(Integer storageId){
        if (baseCache.get(storageId) != null) {
            return baseCache.get(storageId);
        } else {
            List<FilterConfig> dbResult = filterConfigMapper.findByStorageId(storageId);
            baseCache.put(storageId, dbResult);
            return dbResult;
        }
    }


    /**
     * 批量保存存储源过滤器配置, 会先删除之前的所有配置(在事务中运行)
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   filterConfigList
     *          存储源过滤器配置列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(Integer storageId, List<FilterConfig> filterConfigList) {
        filterConfigMapper.deleteByStorageId(storageId);
        super.saveBatch(filterConfigList);
        baseCache.put(storageId, filterConfigList);
    }


    /**
     * 判断访问的路径是否是不允许访问的
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   path
     *          请求路径
     *
     * @throws FileAccessException 如果没权限访问此目录, 则抛出异常
     *
     */
    public void checkPathPermission(Integer storageId, String path) {
        List<FilterConfig> filterConfigList = findByStorageIdAndInaccessible(storageId);
        if (testPattern(filterConfigList, path)) {
            throw new FileAccessException("您没有权限访问该路径");
        }

    }


    /**
     * 获取所有类型为禁止访问的过滤规则
     *
     * @param   storageId
     *          存储 ID
     *
     * @return  禁止访问的过滤规则列表
     */
    public List<FilterConfig> findByStorageIdAndInaccessible(Integer storageId){
        if (inaccessibleCache.get(storageId) != null) {
            return inaccessibleCache.get(storageId);
        } else {
            List<FilterConfig> dbResult = filterConfigMapper.findByStorageIdAndInaccessible(storageId);
            inaccessibleCache.put(storageId, dbResult);
            return dbResult;
        }
    }


    /**
     * 获取所有类型为禁止下载的过滤规则
     *
     * @param   storageId
     *          存储 ID
     *
     * @return  禁止下载的过滤规则列表
     */
    public List<FilterConfig> findByStorageIdAndDisableDownload(Integer storageId){
        if (disableDownloadCache.get(storageId) != null) {
            return disableDownloadCache.get(storageId);
        } else {
            List<FilterConfig> dbResult = filterConfigMapper.findByStorageIdAndDisableDownload(storageId);
            disableDownloadCache.put(storageId, dbResult);
            return dbResult;
        }
    }


    /**
     * 指定存储源下的文件名称, 根据过滤表达式判断是否会显示, 如果符合任意一条表达式, 则返回 true, 反之则返回 false.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   fileName
     *          文件名
     *
     * @return  是否显示
     */
    public boolean filterResultIsHidden(Integer storageId, String fileName) {
        List<FilterConfig> filterConfigList = findByStorageId(storageId);
        return testPattern(filterConfigList, fileName);
    }


    /**
     * 指定存储源下的文件名称, 根据过滤表达式判断文件名和所在路径是否禁止下载, 如果符合任意一条表达式, 则返回 true, 反之则返回 false.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   fileName
     *          文件名
     *
     * @return  是否显示
     */
    public boolean filterResultIsDisableDownload(Integer storageId, String fileName) {
        List<FilterConfig> filterConfigList = findByStorageIdAndDisableDownload(storageId);
        String filePath = FileUtil.getParent(fileName, 1);
        if (StrUtil.isEmpty(filePath)) {
            return testPattern(filterConfigList, fileName);
        } else {
            return testPattern(filterConfigList, fileName) || testPattern(filterConfigList, filePath);
        }
    }


    /**
     * 根据规则表达式和测试字符串进行匹配，如测试字符串和其中一个规则匹配上，则返回 true，反之返回 false。
     *
     * @param   patternList
     *          规则列表
     *
     * @param   test
     *
     *          测试字符串
     *
     * @return  是否显示
     */
    private boolean testPattern(List<FilterConfig> patternList, String test) {
        for (FilterConfig filterConfig : patternList) {
            String expression = filterConfig.getExpression();

            if (StrUtil.isEmpty(expression)) {
                return false;
            }

            try {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + expression);
                boolean match = pathMatcher.matches(Paths.get(test));
                if (match) {
                    return true;
                }
                log.debug("regex: {}, name {}, contains: {}", expression, test, match);
            } catch (Exception e) {
                log.debug("regex: {}, name {}, parse error, skip expression", expression, test);
            }
        }

        return false;
    }

}