package im.zhaojun.zfile.module.storage.controller.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.request.operator.BatchDeleteRequest;
import im.zhaojun.zfile.module.storage.model.request.operator.NewFolderRequest;
import im.zhaojun.zfile.module.storage.model.request.operator.RenameFileRequest;
import im.zhaojun.zfile.module.storage.model.request.operator.RenameFolderRequest;
import im.zhaojun.zfile.module.storage.model.request.operator.UploadFileRequest;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件操作相关接口, 如新建文件夹, 上传文件, 删除文件, 移动文件等.
 *
 * @author zhaojun
 */
@Api(tags = "文件操作模块")
@ApiSort(3)
@Slf4j
@RestController
@RequestMapping("/api/file/operator")
public class FileOperatorController {

	@Resource
	private StorageSourceContext storageSourceContext;
	
	@Resource
	private PasswordConfigService passwordConfigService;

	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "创建文件夹")
	@PostMapping("/mkdir")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	public AjaxJson<?> mkdir(@Valid @RequestBody NewFolderRequest newFolderRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByStorageKey(newFolderRequest.getStorageKey());
		boolean flag = fileService.newFolder(newFolderRequest.getPath(), newFolderRequest.getName());
		if (flag) {
			return AjaxJson.getSuccess("创建成功");
		} else {
			return AjaxJson.getError("创建失败");
		}
	}


	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "批量删除文件/文件夹")
	@PostMapping("/delete/batch")
	public AjaxJson<?> deleteFile(@Valid @RequestBody BatchDeleteRequest batchDeleteRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByStorageKey(batchDeleteRequest.getStorageKey());
		List<BatchDeleteRequest.DeleteItem> deleteItems = batchDeleteRequest.getDeleteItems();
		
		int deleteSuccessCount = 0, deleteFailCount = 0, totalCount = CollUtil.size(deleteItems);
		
		Map<String, Boolean> pathCheckCache = new HashMap<>();
		
		
		for (BatchDeleteRequest.DeleteItem deleteItem : deleteItems) {
			// 检查权限
			Boolean pathCheckResult = pathCheckCache.get(deleteItem.getPath());
			
			// 缓存值为 false, 则即为失败, 直接跳过此删除的文件
			if (BooleanUtil.isFalse(pathCheckResult)) {
				deleteFailCount++;
				continue;
			}
			
			// 缓存没有, 则进行校验
			if (pathCheckResult == null) {
				VerifyResultDTO verifyResultDTO = passwordConfigService.verifyPassword(fileService.getStorageId(), deleteItem.getPath(), deleteItem.getPassword());
				// 校验不通过, 则跳过此删除的文件
				if (!verifyResultDTO.isPassed()) {
					log.warn("因密码原因删除失败, 类型: {}, 路径: {}, 名称: {}, 原因: {}", deleteItem.getType(), deleteItem.getPath(), deleteItem.getName(), verifyResultDTO.getMsg());
					pathCheckCache.put(deleteItem.getPath(), false);
					deleteFailCount++;
					continue;
				}
				pathCheckCache.put(deleteItem.getPath(), true);
			}
			
			boolean flag = false;
			try {
				if (deleteItem.getType() == FileTypeEnum.FILE) {
					flag = fileService.deleteFile(deleteItem.getPath(), deleteItem.getName());
				} else if (deleteItem.getType() == FileTypeEnum.FOLDER) {
					flag = fileService.deleteFolder(deleteItem.getPath(), deleteItem.getName());
				}
				
				if (flag) {
					deleteSuccessCount++;
				} else {
					deleteFailCount++;
				}
			} catch (Exception e) {
				log.error("删除文件/文件夹失败, 文件路径: {}, 文件名称: {}", deleteItem.getPath(), deleteItem.getName(), e);
				deleteFailCount++;
			}
		}
		
		if (totalCount > 1) {
			return AjaxJson.getSuccess("批量删除 " + totalCount + " 个, 删除成功 " + deleteSuccessCount + " 个, 失败 " + deleteFailCount + " 个.");
		} else {
			return totalCount == deleteSuccessCount ? AjaxJson.getSuccess("删除成功") : AjaxJson.getError("删除失败");
		}
	}


	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "重命名文件")
	@PostMapping("/rename/file")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	public AjaxJson<?> rename(@Valid @RequestBody RenameFileRequest renameFileRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByStorageKey(renameFileRequest.getStorageKey());
		boolean flag = fileService.renameFile(renameFileRequest.getPath(), renameFileRequest.getName(), renameFileRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}


	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "重命名文件夹")
	@PostMapping("/rename/folder")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	public AjaxJson<?> deleteFile(@Valid @RequestBody RenameFolderRequest renameFolderRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByStorageKey(renameFolderRequest.getStorageKey());
		boolean flag = fileService.renameFolder(renameFolderRequest.getPath(), renameFolderRequest.getName(), renameFolderRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}


	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "上传文件")
	@PostMapping("/upload/file")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	public AjaxJson<?> getUploadFileUrl(@Valid @RequestBody UploadFileRequest uploadFileRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByStorageKey(uploadFileRequest.getStorageKey());
		String uploadUrl = fileService.getUploadUrl(uploadFileRequest.getPath(),
				uploadFileRequest.getName(), uploadFileRequest.getSize());
		return AjaxJson.getSuccessData(uploadUrl);
	}

}