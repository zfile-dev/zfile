package im.zhaojun.zfile.module.storage.chain;

import im.zhaojun.zfile.module.storage.model.request.base.FileListRequest;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.chain.impl.ContextBase;

import java.util.List;

/**
 * 文件处理责任链上下文
 *
 * @author zhaojun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@Builder
public class FileContext extends ContextBase {

	/**
	 * 存储源 id
	 */
	private Integer storageId;

	/**
	 * 存储源请求
	 */
	private FileListRequest fileListRequest;

	/**
	 * 根据存储源请求获取到的文件列表
	 */
	private List<FileItemResult> fileItemList;

	/**
	 * 当前目录密码路径表达式
	 */
	private String passwordPattern;

	/**
	 * 存储源 Service
	 */
    private AbstractBaseFileService<?> fileService;

    /**
     * 操作者用户ID（在分享模式下为分享者的用户ID）
     */
    private Integer operatorUserId;

}