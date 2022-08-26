package im.zhaojun.zfile.home.controller;

import cn.hutool.core.collection.CollUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.request.operator.BatchDeleteRequest;
import im.zhaojun.zfile.home.model.request.operator.NewFolderRequest;
import im.zhaojun.zfile.home.model.request.operator.RenameFileRequest;
import im.zhaojun.zfile.home.model.request.operator.RenameFolderRequest;
import im.zhaojun.zfile.home.model.request.operator.UploadFileRequest;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.common.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

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

	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "创建文件夹")
	@PostMapping("/mkdir")
	public AjaxJson<?> mkdir(@Valid @RequestBody NewFolderRequest newFolderRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(newFolderRequest.getStorageKey());
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
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(batchDeleteRequest.getStorageKey());
		List<BatchDeleteRequest.DeleteItem> deleteItems = batchDeleteRequest.getDeleteItems();
		
		int deleteSuccessCount = 0, deleteFailCount = 0, totalCount = CollUtil.size(deleteItems);
		
		for (BatchDeleteRequest.DeleteItem deleteItem : deleteItems) {
			boolean flag = false;
			if (deleteItem.getType() == FileTypeEnum.FILE) {
				flag = fileService.deleteFile(deleteItem.getPath(), deleteItem.getName());
			} else if (deleteItem.getType() == FileTypeEnum.FOLDER) {
				flag = fileService.deleteFile(deleteItem.getPath(), deleteItem.getName());
			}
			
			if (flag) {
				deleteSuccessCount++;
			} else {
				deleteFailCount++;
			}
		}
		
		if (totalCount > 1) {
			return AjaxJson.getSuccess("批量删除 " + totalCount + " 个, 删除成功 " + deleteSuccessCount + " 个, 失败 " + deleteFailCount + " 个.");
		} else {
			return totalCount == deleteSuccessCount ? AjaxJson.getSuccess("删除成功") : AjaxJson.getError("删除失败");
		}
	}


	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "重命名文件")
	@PostMapping("/rename/file")
	public AjaxJson<?> rename(@Valid @RequestBody RenameFileRequest renameFileRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(renameFileRequest.getStorageKey());
		boolean flag = fileService.renameFile(renameFileRequest.getPath(), renameFileRequest.getName(), renameFileRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}


	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "重命名文件夹")
	@PostMapping("/rename/folder")
	public AjaxJson<?> deleteFile(@Valid @RequestBody RenameFolderRequest renameFolderRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(renameFolderRequest.getStorageKey());
		boolean flag = fileService.renameFolder(renameFolderRequest.getPath(), renameFolderRequest.getName(), renameFolderRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}


	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "上传文件")
	@PostMapping("/upload/file")
	public AjaxJson<?> uploadFile(@Valid @RequestBody UploadFileRequest uploadFileRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(uploadFileRequest.getStorageKey());
		String uploadUrl = fileService.getUploadUrl(uploadFileRequest.getPath(),
				uploadFileRequest.getName(), uploadFileRequest.getSize());
		return AjaxJson.getSuccessData(uploadUrl);
	}

}