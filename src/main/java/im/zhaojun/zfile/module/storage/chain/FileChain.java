package im.zhaojun.zfile.module.storage.chain;

import im.zhaojun.zfile.module.storage.chain.command.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * 文件处理责任链定义
 *
 * @author zhaojun
 */
@Service
@Slf4j
public class FileChain extends ChainBase {

	@Resource
	private FileAccessPermissionVerifyCommand fileAccessPermissionVerifyCommand;

	@Resource
	private FolderPasswordVerifyCommand folderPasswordVerifyCommand;

	@Resource
	private FileHiddenCommand fileHiddenCommand;

	@Resource
	private FileSortCommand fileSortCommand;

	@Resource
	private FileDownloadPermissionCommand fileDownloadPermissionCommand;

	/**
	 * 初始化责任链
	 */
	@PostConstruct
	public void init() {
		this.addCommand(fileAccessPermissionVerifyCommand);
		this.addCommand(folderPasswordVerifyCommand);
		this.addCommand(fileHiddenCommand);
		this.addCommand(fileSortCommand);
		this.addCommand(fileDownloadPermissionCommand);
	}

	/**
	 * 执行文件处理责任链
	 *
	 * @param   content
	 *          文件上下文
	 *
	 * @return  是否执行成功
	 */
	public FileContext execute(FileContext content) throws Exception {
		super.execute(content);
		return content;
	}

}