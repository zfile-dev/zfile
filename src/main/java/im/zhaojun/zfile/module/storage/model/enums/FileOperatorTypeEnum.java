package im.zhaojun.zfile.module.storage.model.enums;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.module.storage.function.*;
import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * 文件操作类型枚举
 *
 * @author zhaojun
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum FileOperatorTypeEnum {

	/**
	 * 是否可用
	 */
	@Deprecated
	AVAILABLE("是否可用", "available",
			"显示且可访问存储源，匿名用户通过 URL 也无法访问. 但不影响直链/短链下载.", AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 新建文件夹操作
	 */
	NEW_FOLDER("新建文件夹", "newFolder",
			"关闭此权限会同时关闭上传文件夹的功能", BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 获取文件上传链接操作
	 */
	UPLOAD("上传", "upload",
			null, BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 预览操作
	 */
	PREVIEW("预览", "preview",
			"控制视频、音频、Office 等格式是否支持预览", AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 下载操作（如果允许预览，但不允许下载，则只是隐藏下载按钮而已，无法真正防止下载）
	 */
	DOWNLOAD("下载", "download",
			"如允许预览，但不允许下载，则只是隐藏下载按钮而已，无法真正防止下载。且不会限制直链下载。", AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 批量下载
	 */
	@Deprecated
	BATCH_DOWNLOAD("批量下载", "batchDownload", null, AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 打包下载
	 */
	@Deprecated
	PACKAGE_DOWNLOAD("打包下载", "packageDownload", null, AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 复制文件下载链接
	 */
	COPY_DOWNLOAD_LINK("复制下载链接", "copyDownloadLink", "如允许则右键菜单会增加复制文件下载链接功能（不是直链和短链，而是存储源本身的下载链接）", AllowAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 重命名文件&文件夹操作
	 */
	RENAME("重命名", "rename",
			null, BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 复制文件&文件夹操作
	 */
	COPY("复制", "copy",
			null, BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 移动文件&文件夹操作
	 */
	MOVE("移动", "move",
			null, BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 删除文件&文件夹操作
	 */
	DELETE("删除", "delete",
			null, BasicFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 搜索操作
	 */
	@Deprecated
	SEARCH("搜索", "search",
			"如您未启用存储源搜索功能, 此处授权也不会生效.", SearchFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 获取直链
	 */
	LINK("获取直链", "generateLink",
			null, LinkFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 生成短链
	 */
	SHORT_LINK("生成短链", "generateShortLink",
			null, ShortLinkFileOperatorTypeEnumDefaultValueFunc.class),

    /**
     * 创建分享链接
     */
    SHARE_LINK("创建分享链接", "createShareLink",
            "允许用户创建分享链接", AllowAdminFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 分享自定义 key
	 */
	CUSTOM_SHARE_KEY("自定义分享链接 key", "customShareKey",
			"允许用户在创建分享链接时使用自定义 key，而不是系统自动生成", AllowAdminFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 忽略密码
	 */
	IGNORE_PASSWORD("忽略密码", "ignorePassword",
			"勾选上，则会忽略所有密码规则，免输入密码", DisableAllFileOperatorTypeEnumDefaultValueFunc.class),

	/**
	 * 忽略隐藏
	 */
	IGNORE_HIDDEN("忽略隐藏", "ignoreHidden",
			"勾选上，则会忽略所有隐藏规则，可查看所有文件夹和文件", DisableAllFileOperatorTypeEnumDefaultValueFunc.class);


	/**
	 * 操作类型描述
	 */
	private final String name;

	/**
	 * 操作类型值
	 */
	@EnumValue
	@JsonValue
	private final String value;

	/**
	 * 辅助提示信息
	 */
	private final String tips;

	@Getter(AccessLevel.NONE)
	private final Class<? extends Function<Integer, FileOperatorTypeDefaultValueDTO>> defaultValueFuncClazz;

	public FileOperatorTypeDefaultValueDTO getDefaultValue(Integer storageId) {
		try {
			return ReflectUtil.newInstance(defaultValueFuncClazz).apply(storageId);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	public boolean isDeprecated() {
		try {
			Field field = getClass().getField(name());
			return field.isAnnotationPresent(Deprecated.class);
		} catch (NoSuchFieldException e) {
			log.error("获取枚举类注解失败", e);
		}

		return false;
	}
}