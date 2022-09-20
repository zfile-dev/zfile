package im.zhaojun.zfile.module.storage.chain;

import im.zhaojun.zfile.module.storage.chain.command.FileAccessPermissionVerifyCommand;
import im.zhaojun.zfile.module.storage.chain.command.FileHiddenCommand;
import im.zhaojun.zfile.module.storage.chain.command.FileSortCommand;
import im.zhaojun.zfile.module.storage.chain.command.FileUrlAddVersionCommand;
import im.zhaojun.zfile.module.storage.chain.command.FolderPasswordVerifyCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
	private FileUrlAddVersionCommand fileUrlAddVersionCommand;

	/**
	 * 初始化责任链
	 */
	@PostConstruct
	public void init() {
		this.addCommand(fileAccessPermissionVerifyCommand);
		this.addCommand(folderPasswordVerifyCommand);
		this.addCommand(fileHiddenCommand);
		this.addCommand(fileSortCommand);
		this.addCommand(fileUrlAddVersionCommand);
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