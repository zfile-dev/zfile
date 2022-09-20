package im.zhaojun.zfile.module.readme.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.core.util.HttpUtil;
import im.zhaojun.zfile.core.util.PatternMatcherUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.readme.mapper.ReadmeConfigMapper;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 存储源 readme 配置 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "readmeConfig")
public class ReadmeConfigService {

	@Resource
	private ReadmeConfigMapper readmeConfigMapper;
	
	@Resource
	private ReadmeConfigService readmeConfigService;
	
	@Resource
	private StorageSourceContext storageSourceContext;

	/**
	 * 根据存储源 ID 查询文档配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源文档配置列表
	 */
	@Cacheable(key = "#storageId")
	public List<ReadmeConfig> findByStorageId(Integer storageId){
		return readmeConfigMapper.findByStorageId(storageId);
	}


	/**
	 * 批量保存存储源 readme 配置, 会先删除之前的所有配置(在事务中运行)
	 *
	 * @param   storageId
	 *          存储源 ID
	 *
	 * @param   readmeConfigList
	 *          存储源 readme 配置列表
	 */
	@Transactional(rollbackFor = Exception.class)
	public void batchSave(Integer storageId, List<ReadmeConfig> readmeConfigList) {
		readmeConfigService.deleteByStorageId(storageId);
		
		log.info("更新存储源 ID 为 {} 的目录文档配置 {} 条", storageId, readmeConfigList.size());
		
		readmeConfigList.forEach(readmeConfig -> {
			readmeConfig.setStorageId(storageId);
			readmeConfigMapper.insert(readmeConfig);
			
			if (log.isDebugEnabled()) {
				log.debug("新增目录文档, 存储源 ID: {}, 表达式: {}, 描述: {}, 显示模式: {}",
						readmeConfig.getStorageId(), readmeConfig.getExpression(),
						readmeConfig.getDescription(), readmeConfig.getDisplayMode().getValue());
			}
		});
	}
	
	
	/**
	 * 根据存储源 ID 删除存储源 readme 配置
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 */
	@CacheEvict(key = "#storageId")
	public int deleteByStorageId(Integer storageId) {
		int deleteSize = readmeConfigMapper.deleteByStorageId(storageId);
		log.info("删除存储源 ID 为 {} 的目录文档配置 {} 条", storageId, deleteSize);
		return deleteSize;
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
		List<ReadmeConfig> readmeConfigList = readmeConfigService.findByStorageId(storageId);
		return getReadmeByTestPattern(storageId, readmeConfigList, path);
	}
	
	
	/**
	 * 根据存储源指定路径下的 readme 配置，如果指定为兼容模式，则会读取指定目录下的 readme.md 文件.
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @param 	path
	 * 			存储源路径
	 *
	 * @param 	compatibilityReadme
	 * 			是否兼容为读取 readme.md 文件
	 *
	 * @return  目录下存储源 readme 配置
	 */
	public ReadmeConfig getByStorageAndPath(Integer storageId, String path, Boolean compatibilityReadme) {
		ReadmeConfig readmeByPath = new ReadmeConfig();
		readmeByPath.setStorageId(storageId);
		readmeByPath.setDisplayMode(ReadmeDisplayModeEnum.BOTTOM);
		if (BooleanUtil.isTrue(compatibilityReadme)) {
			try {
				log.info("存储源 {} 兼容获取目录 {} 下的 readme.md", storageId, path);
				AbstractBaseFileService<IStorageParam> abstractBaseFileService = storageSourceContext.getByStorageId(storageId);
				String pathAndName = StringUtils.concat(path, "readme.md");
				FileItemResult fileItem = abstractBaseFileService.getFileItem(pathAndName);
				if (fileItem != null) {
					String url = fileItem.getUrl();
					String readmeText = HttpUtil.getTextContent(url);
					readmeByPath.setReadmeText(readmeText);
				}
			} catch (Exception e) {
				log.error("存储源 {} 兼容获取目录 {} 下的 readme.md 文件失败", storageId, path, e);
			}
		} else {
			// 获取指定目录 readme 文件
			ReadmeConfig dbReadmeConfig = readmeConfigService.findReadmeByPath(storageId, path);
			if (dbReadmeConfig != null) {
				readmeByPath = dbReadmeConfig;
			}
			log.info("存储源 {} 规则模式获取目录 {} 下文档信息", storageId, path);
		}
		
		return readmeByPath;
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
	private ReadmeConfig getReadmeByTestPattern(Integer storageId, List<ReadmeConfig> patternList, String test) {
		// 如果目录文档规则为空, 则可直接返回空.
		if (CollUtil.isEmpty(patternList)) {
			if (log.isDebugEnabled()) {
				log.debug("目录文档规则列表为空, 存储源 ID: {}, 测试字符串: {}", storageId, test);
			}
			return null;
		}
		
		for (ReadmeConfig filterConfig : patternList) {
			String expression = filterConfig.getExpression();
			
			if (StrUtil.isEmpty(expression)) {
				if (log.isDebugEnabled()) {
					log.debug("存储源 {} 目录文档规则表达式为空: {}, 测试字符串: {}, 表达式为空，跳过该规则比对", storageId, expression, test);
				}
				continue;
			}
			
			try {
				boolean match = PatternMatcherUtils.testCompatibilityGlobPattern(expression, test);
				
				if (log.isDebugEnabled()) {
					log.debug("存储源 {} 目录文档规则表达式: {}, 测试字符串: {}, 匹配结果: {}", storageId, expression, test, match);
				}
				
				if (match) {
					return filterConfig;
				}
			} catch (Exception e) {
				log.error("存储源 {} 目录文档规则表达式: {}, 测试字符串: {}, 匹配异常，跳过该规则.", storageId, expression, test, e);
			}
		}

		return null;
	}
	

}