package im.zhaojun.zfile.module.readme.service;

import im.zhaojun.zfile.core.util.CollectionUtils;
import im.zhaojun.zfile.core.util.HttpUtil;
import im.zhaojun.zfile.core.util.PatternMatcherUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.readme.mapper.ReadmeConfigMapper;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import im.zhaojun.zfile.module.readme.model.enums.ReadmePathModeEnum;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.event.StorageSourceCopyEvent;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	/**
	 * 根据存储源 ID 查询文档配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源文档配置列表
	 */
	@Cacheable(key = "#storageId",
				condition = "#storageId != null")
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
		((ReadmeConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);

		log.info("更新存储源 ID 为 {} 的目录文档配置 {} 条", storageId, readmeConfigList.size());

		readmeConfigList.forEach(readmeConfig -> {
			readmeConfig.setId(null);
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
	 * 监听存储源删除事件，根据存储源 id 删除相关的目录文档设置
	 *
	 * @param   storageSourceDeleteEvent
	 *          存储源删除事件
	 */
	@EventListener
	public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
		Integer storageId = storageSourceDeleteEvent.getId();
		int updateRows = ((ReadmeConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);
		if (log.isDebugEnabled()) {
			log.debug("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源目录文档设置 {} 条",
					storageId,
					storageSourceDeleteEvent.getName(),
					storageSourceDeleteEvent.getType().getDescription(),
					updateRows);
		}
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
		List<ReadmeConfig> readmeConfigList = ((ReadmeConfigService) AopContext.currentProxy()).findByStorageId(storageId);
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
		if (BooleanUtils.isTrue(compatibilityReadme)) {
			try {
				AbstractBaseFileService<IStorageParam> abstractBaseFileService = StorageSourceContext.getByStorageId(storageId);
				String pathAndName = StringUtils.concat(path, "readme.md");
				FileItemResult fileItem = abstractBaseFileService.getFileItem(pathAndName);
				if (fileItem != null) {
					String url = fileItem.getUrl();
					String readmeText = HttpUtil.getTextContent(url);
					if (log.isDebugEnabled()) {
						log.debug("存储源 {} 兼容获取目录 {} 下的 readme.md 文件成功, url: {}", storageId, path, url);
					}
					readmeByPath.setReadmeText(readmeText);
				}
			} catch (Exception e) {
				log.error("存储源 {} 兼容获取目录 {} 下的 readme.md 文件失败", storageId, path, e);
			}
		} else {
			// 获取指定目录 readme 文件
			ReadmeConfig dbReadmeConfig = ((ReadmeConfigService) AopContext.currentProxy()).findReadmeByPath(storageId, path);
			if (dbReadmeConfig != null) {
				readmeByPath = dbReadmeConfig;
			}
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
		if (CollectionUtils.isEmpty(patternList)) {
			if (log.isDebugEnabled()) {
				log.debug("目录文档规则列表为空, 存储源 ID: {}, 测试字符串: {}", storageId, test);
			}
			return null;
		}

		for (ReadmeConfig readmeConfig : patternList) {
			String expression = readmeConfig.getExpression();

			if (StringUtils.isEmpty(expression)) {
				if (log.isDebugEnabled()) {
					log.debug("存储源 {} 目录文档规则表达式为空: {}, 测试字符串: {}, 表达式为空，跳过该规则比对", storageId, expression, test);
				}
				continue;
			}

			try {
                ReadmePathModeEnum pathMode = readmeConfig.getPathMode();
                boolean match;

                if (pathMode == ReadmePathModeEnum.ABSOLUTE) {
                    AbstractBaseFileService<IStorageParam> abstractBaseFileService = StorageSourceContext.getByStorageId(storageId);
                    String currentUserBasePath = abstractBaseFileService.getCurrentUserBasePath();
                    match = PatternMatcherUtils.testCompatibilityGlobPattern(expression, StringUtils.concat(currentUserBasePath, test));
                } else {
                    match = PatternMatcherUtils.testCompatibilityGlobPattern(expression, test);
                }

				if (log.isDebugEnabled()) {
					log.debug("存储源 {} 目录文档规则表达式: {}, 测试字符串: {}, 匹配结果: {}", storageId, expression, test, match);
				}

				if (match) {
					return readmeConfig;
				}
			} catch (Exception e) {
				log.error("存储源 {} 目录文档规则表达式: {}, 测试字符串: {}, 匹配异常，跳过该规则.", storageId, expression, test, e);
			}
		}

		return null;
	}


	/**
	 * 监听存储源复制事件, 复制存储源时, 复制存储源目录文档设置
	 *
	 * @param   storageSourceCopyEvent
	 *          存储源复制事件
	 */
	@EventListener
	public void onStorageSourceCopy(StorageSourceCopyEvent storageSourceCopyEvent) {
		Integer fromId = storageSourceCopyEvent.getFromId();
		Integer newId = storageSourceCopyEvent.getNewId();

		List<ReadmeConfig> readmeConfigList = ((ReadmeConfigService) AopContext.currentProxy()).findByStorageId(fromId);

		readmeConfigList.forEach(readmeConfig -> {
			ReadmeConfig newReadmeConfig = new ReadmeConfig();
			BeanUtils.copyProperties(readmeConfig, newReadmeConfig);
			newReadmeConfig.setId(null);
			newReadmeConfig.setStorageId(newId);
			readmeConfigMapper.insert(newReadmeConfig);
		});

		log.info("复制存储源 ID 为 {} 的存储源目录文档设置到存储源 ID 为 {} 成功, 共 {} 条", fromId, newId, readmeConfigList.size());
	}

}