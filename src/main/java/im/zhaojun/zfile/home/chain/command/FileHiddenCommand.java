package im.zhaojun.zfile.home.chain.command;

import cn.hutool.core.collection.CollUtil;
import im.zhaojun.zfile.admin.service.FilterConfigService;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.chain.FileContext;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件隐藏责任链 command 命令
 *      过滤此存储源通过规则隐藏的文件.
 *
 * @author zhaojun
 */
@Service
public class FileHiddenCommand implements Command {

	@Resource
	private FilterConfigService filterConfigService;


	/**
	 * 隐藏存储源规律规则匹配到的文件.
	 *
	 * @param   context
	 *          文件处理责任链上下文
	 *
	 * @return  是否停止执行责任链, true: 停止执行责任链, false: 继续执行责任链
	 */
	@Override
	public boolean execute(Context context) throws Exception {
		FileContext fileContext = (FileContext) context;
		Integer storageId = fileContext.getStorageId();
		List<FileItemResult> fileItemList = fileContext.getFileItemList();
		if (CollUtil.isEmpty(fileItemList)) {
			return false;
		}

		// 创建副本, 防止排序和过滤对原数据产生影响
		List<FileItemResult> result = new ArrayList<>();

		fileItemList.forEach(
				fileItem -> {
					if (!filterConfigService.filterResultIsHidden(storageId, StringUtils.concat(fileItem.getPath(), fileItem.getName()))) {
						result.add(fileItem);
					}
				}
		);

		fileContext.setFileItemList(result);
		return false;
	}

}