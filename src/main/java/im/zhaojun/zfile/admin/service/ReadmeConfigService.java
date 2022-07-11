package im.zhaojun.zfile.admin.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.ReadmeConfigMapper;
import im.zhaojun.zfile.admin.model.entity.ReadmeConfig;
import im.zhaojun.zfile.common.constant.ZFileConstant;
import im.zhaojun.zfile.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

/**
 * 存储源 readme 配置 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class ReadmeConfigService extends ServiceImpl<ReadmeConfigMapper, ReadmeConfig> {

	@Resource
	private ReadmeConfigMapper readmeConfigMapper;


	/**
	 * 根据存储源 ID 查询文档配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源文档配置列表
	 */
	public List<ReadmeConfig> findByStorageId(Integer storageId){
		return readmeConfigMapper.findByStorageId(storageId);
	}


	/**
	 * 批量保存存储源 readme 配置, 会先删除之前的所有配置(在事务中运行)
	 *
	 * @param   storageId
	 *          存储源 ID
	 *
	 * @param   filterConfigList
	 *          存储源 readme 配置列表
	 */
	@Transactional(rollbackFor = Exception.class)
	public void batchSave(Integer storageId, List<ReadmeConfig> filterConfigList) {
		readmeConfigMapper.deleteByStorageId(storageId);
		super.saveBatch(filterConfigList);
	}



	/**
	 * 根据存储源指定路径下的 readme 配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @param   path
	 *          文件夹路径
	 *
	 * @return  存储源 readme 配置列表
	 */
	public ReadmeConfig findReadmeByPath(Integer storageId, String path) {
		List<ReadmeConfig> readmeConfigList = readmeConfigMapper.findByStorageId(storageId);
		return getReadmeByTestPattern(readmeConfigList, path);
	}


	/**
	 * 根据规则表达式和测试字符串进行匹配，如测试字符串和其中一个规则匹配上，则返回 true，反之返回 false。
	 *
	 * @param   patternList
	 *          规则列表
	 *
	 * @param   test
	 *          测试字符串
	 *
	 * @return  是否显示
	 */
	private ReadmeConfig getReadmeByTestPattern(List<ReadmeConfig> patternList, String test) {
		test = StringUtils.concat(test, ZFileConstant.PATH_SEPARATOR);

		for (ReadmeConfig filterConfig : patternList) {
			String expression = filterConfig.getExpression();
			if (StrUtil.isEmpty(expression)) {
				continue;
			}

			try {
				PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + expression);
				boolean match = pathMatcher.matches(Paths.get(test));
				if (match) {
					return filterConfig;
				}
				log.debug("regex: {}, name {}, contains: {}", expression, test, match);
			} catch (Exception e) {
				log.debug("regex: {}, name {}, parse error, skip expression", expression, test);
			}
		}

		return null;
	}

}