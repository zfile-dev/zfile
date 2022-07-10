package im.zhaojun.zfile.home.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.home.model.request.operator.DeleteFileRequest;
import im.zhaojun.zfile.home.model.request.operator.DeleteFolderRequest;
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
	@ApiOperation(value = "删除文件")
	@PostMapping("/delete/file")
	public AjaxJson<?> deleteFile(@Valid @RequestBody DeleteFileRequest deleteFileRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(deleteFileRequest.getStorageKey());
		boolean flag = fileService.deleteFile(deleteFileRequest.getPath(), deleteFileRequest.getName());
		if (flag) {
			return AjaxJson.getSuccess("删除成功");
		} else {
			return AjaxJson.getError("删除失败");
		}
	}


	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "删除文件夹")
	@PostMapping("/delete/folder")
	public AjaxJson<?> deleteFolder(@Valid @RequestBody DeleteFolderRequest deleteFolderRequest) {
		AbstractBaseFileService<?> fileService = storageSourceContext.getByKey(deleteFolderRequest.getStorageKey());
		boolean flag = fileService.deleteFolder(deleteFolderRequest.getPath(), deleteFolderRequest.getName());
		if (flag) {
			return AjaxJson.getSuccess("删除成功");
		} else {
			return AjaxJson.getError("删除失败");
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