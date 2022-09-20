package im.zhaojun.zfile.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author zhaojun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CodeMsg {
	
	/**
	 * 错误码
	 * <p>
	 * 均为 5 位数, 如 00000, 10100, 20105 等.
	 * <br>
	 * 第一位表示错误类型, 4 为用户请求输入错误, 5 为服务端处理错误, 6 为警告信息
	 * <br>
	 * 第二位到第三位为二级类型
	 * <br>
	 * 第四位到第五位为具体错误代码, 根据业务场景自行定义
	 * <p>
	 * 以上三种类型均不允许重复, 且都需保持递增.
	 */
	private String code;
	
	/**
	 * 错误消息
	 */
	private String msg;
	
	// 通用返回值
	public static CodeMsg SUCCESS = new CodeMsg("00000", "success");
	public static CodeMsg BAD_REQUEST = new CodeMsg("40000", "非法请求");
	public static CodeMsg ERROR = new CodeMsg("50000", "服务端异常");
	
	
	// -------------- 用户输入级错误 --------------
	public static CodeMsg REQUIRED_PASSWORD = new CodeMsg("40100", "请输入密码");
	public static CodeMsg PASSWORD_FAULT = new CodeMsg("40101", "密码输入错误");
	
	public static CodeMsg STORAGE_SOURCE_NOT_FOUND = new CodeMsg("40102", "无效的或初始化失败的存储源");
	public static CodeMsg STORAGE_SOURCE_FORBIDDEN = new CodeMsg("40103", "无权访问存储源");
	public static CodeMsg STORAGE_SOURCE_FILE_FORBIDDEN = new CodeMsg("40104", "无权访问该目录");
	public static CodeMsg STORAGE_SOURCE_ILLEGAL_OPERATION = new CodeMsg("40105", "非法操作");
	
	
	
	// -------------- 服务端处理错误 --------------
	
	// 初始化相关错误
	public static CodeMsg STORAGE_SOURCE_INIT_FAIL = new CodeMsg("50100", "初始化存储源失败");
	public static CodeMsg STORAGE_SOURCE_INIT_STORAGE_CONFIG_FAIL = new CodeMsg("50101", "初始化存储源参数失败");
	public static CodeMsg STORAGE_SOURCE_INIT_STORAGE_PARAM_FIELD_FAIL = new CodeMsg("50102", "填充存储源字段失败");
	
	
	// 文件操作相关错误
	public static CodeMsg STORAGE_SOURCE_FILE_NEW_FOLDER_FAIL = new CodeMsg("50201", "新建文件夹失败");
	public static CodeMsg STORAGE_SOURCE_FILE_DELETE_FAIL = new CodeMsg("50202", "删除失败");
	public static CodeMsg STORAGE_SOURCE_FILE_RENAME_FAIL = new CodeMsg("50203", "重命名失败");
	public static CodeMsg STORAGE_SOURCE_FILE_GET_UPLOAD_FAIL = new CodeMsg("50204", "获取上传链接失败");
	public static CodeMsg STORAGE_SOURCE_FILE_PROXY_UPLOAD_FAIL = new CodeMsg("50205", "文件上传失败");
	public static CodeMsg STORAGE_SOURCE_FILE_PROXY_DOWNLOAD_FAIL = new CodeMsg("50206", "文件下载失败");
	public static CodeMsg STORAGE_SOURCE_FILE_GET_ITEM_FAIL = new CodeMsg("50207", "文件不存在或请求异常");
	public static CodeMsg STORAGE_SOURCE_FILE_DISABLE_PROXY_DOWNLOAD = new CodeMsg("50208", "非法操作, 当前文件不支持此类下载方式");
	
	
	
	
	
}