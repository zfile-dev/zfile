package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.exception.file.init.InitializeStorageSourceException;
import im.zhaojun.zfile.core.util.ClassUtils;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.core.util.PlaceholderUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelect;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractBaseFileService<P extends IStorageParam> implements BaseFileService {

	@Resource
	private SystemConfigService systemConfigService;

	/**
	 * 存储源初始化配置
	 */
	public P param;

	/**
	 * 是否初始化成功
	 */
	protected boolean isInitialized = false;

	/**
	 * 存储源 ID
	 */
	public Integer storageId;


	/**
	 * 初始化存储源, 在调用前要设置存储的 {@link #storageId} 属性. 和 {@link #param} 属性.
	 */
	public abstract void init();


	/**
	 * 获取指定路径下的文件及文件夹, 默认缓存 60 分钟，每隔 30 分钟刷新一次.
	 *
	 * @param   folderPath
	 *          文件夹路径，如 /音乐/毛不易/
	 *
	 * @return  文件及文件夹列表
	 *
	 * @throws  Exception 获取文件列表中出现的异常
	 */
	@Override
	public abstract List<FileItemResult> fileList(String folderPath) throws Exception;


	/**
	 * 测试是否连接成功, 会尝试取调用获取根路径的文件, 如果没有抛出异常, 则认为连接成功.
	 */
	public void testConnection() {
		try {
			fileList("/");
			isInitialized = true;
		} catch (Exception e) {
			throw new InitializeStorageSourceException(CodeMsg.STORAGE_SOURCE_INIT_FAIL, storageId, "初始化异常, 错误信息为: " + e.getMessage(), e).setResponseExceptionMessage(true);
		}
	}


	private static final Map<Class<? extends AbstractBaseFileService>, List<StorageSourceParamDef>> STORAGE_SOURCE_PARAM_CACHE = new ConcurrentHashMap<>();

	/**
	 * 获取初始化当前存储源, 所需要的参数信息 (用于表单填写)
	 *
	 * @return 初始化所需的参数列表
	 */
	public List<StorageSourceParamDef> getStorageSourceParamList() {
		// 如果缓存中有, 则直接返回
		Class<? extends AbstractBaseFileService> thisClass = this.getClass();
		if (STORAGE_SOURCE_PARAM_CACHE.containsKey(thisClass)) {
			return STORAGE_SOURCE_PARAM_CACHE.get(thisClass);
		}

		ArrayList<StorageSourceParamDef> result = new ArrayList<>();

		// 获取存储源实现类的泛型参数类型
		Class<?> paramClass = ClassUtils.getClassFirstGenericsParam(this.getClass());
		Field[] fields = ReflectUtil.getFields(paramClass);

		// 已添加的字段列表.
		List<String> fieldNames = new ArrayList<>();

		for (Field field : fields) {
			// 获取字段上的注解
			StorageParamItem annotation = field.getAnnotation(StorageParamItem.class);
			if (annotation != null) {
				String key = annotation.key();
				String name = annotation.name();
				String description = annotation.description();
				boolean required = annotation.required();
				String defaultValue = annotation.defaultValue();
				defaultValue = PlaceholderUtils.resolvePlaceholdersBySpringProperties(defaultValue);

				String link = annotation.link();
				// 如果不为空，且不是 http 或 https 开头，则添加站点域名开头
				if (StrUtil.isNotEmpty(link) && !link.toLowerCase().startsWith("http")) {
					String domain = systemConfigService.getDomain();
					link = StringUtils.concat(domain, link);
				}

				String linkName = annotation.linkName();
				StorageParamTypeEnum type = annotation.type();

				int order = annotation.order();

				// 取注解上标注的字段名称, 如果为空, 则使用字段名称
				if (StrUtil.isEmpty(key)) {
					key = field.getName();
				}

				// 如果字段已存在, 则跳过
				if (fieldNames.contains(field.getName())) {
					continue;
				}

				// 如果默认值不为空, 则该字段则不是必填的
				if (StrUtil.isNotEmpty(defaultValue)) {
					required = false;
				}

				// 如果 type 为 select, 则获取 options 下拉列表.
				List<StorageSourceParamDef.Options> optionsList = new ArrayList<>();
				// 从注解中获取 options
				StorageParamSelectOption[] options = annotation.options();
				if (ArrayUtil.isNotEmpty(options)) {
					for (StorageParamSelectOption storageParamSelectOption : options) {
						StorageSourceParamDef.Options option = new StorageSourceParamDef.Options(storageParamSelectOption);
						optionsList.add(option);
					}
				}
				// 从实现类中通过反射获取 options
				Class<? extends StorageParamSelect> storageParamSelectClass = annotation.optionsClass();
				if (ObjectUtil.isNotEmpty(storageParamSelectClass)
						&& ObjectUtil.notEqual(storageParamSelectClass.getName(), "im.zhaojun.zfile.module.storage.annotation.StorageParamSelect")) {
					StorageParamSelect storageParamSelect = ReflectUtil.newInstance(storageParamSelectClass);
					List<StorageSourceParamDef.Options> storageParamSelectOptions = storageParamSelect.getOptions(annotation, param);
					optionsList.addAll(storageParamSelectOptions);
				}

				StorageSourceParamDef storageSourceParamDef = StorageSourceParamDef.builder().
						key(key).
						name(name).
						description(description).
						required(required).
						defaultValue(defaultValue).
						link(link).
						linkName(linkName).
						type(type).
						options(optionsList).
						order(order).
						build();
				result.add(storageSourceParamDef);
				fieldNames.add(field.getName());
			}
		}

		// 按照顺序排序
		result.sort(Comparator.comparingInt(StorageSourceParamDef::getOrder));

		// 写入到缓存中
		STORAGE_SOURCE_PARAM_CACHE.put(thisClass, result);
		return result;
	}


	/**
	 * 获取单个文件信息
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  单个文件的内容.
	 */
	public abstract FileItemResult getFileItem(String pathAndName);

	public abstract boolean newFolder(String path, String name);

	public abstract boolean deleteFile(String path, String name);

	public abstract boolean deleteFolder(String path, String name);

	public abstract boolean renameFile(String path, String name, String newName);

	public abstract boolean renameFolder(String path, String name, String newName);

	public abstract String getUploadUrl(String path, String name, Long size);

	public void setStorageId(Integer storageId) {
		if (this.storageId != null) {
			throw new IllegalStateException("请勿重复初始化存储源");
		}
		this.storageId = storageId;
	}

	public void setParam(P param) {
		if (this.param != null) {
			throw new IllegalStateException("请勿重复初始化存储源");
		}
		this.param = param;
	}

	/**
	 * 获取是否初始化成功
	 *
	 * @return 初始化成功与否
	 */
	public boolean getIsUnInitialized() {
		return !isInitialized;
	}


	/**
	 * 获取是否初始化成功
	 *
	 * @return 初始化成功与否
	 */
	public boolean getIsInitialized() {
		return isInitialized;
	}


	public P getParam() {
		return param;
	}

	public Integer getStorageId() {
		return storageId;
	}
}