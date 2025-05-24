package im.zhaojun.zfile.module.storage.controller.file;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.password.model.dto.VerifyResultDTO;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.request.operator.*;
import im.zhaojun.zfile.module.storage.model.result.operator.BatchOperatorResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 文件操作相关接口, 如新建文件夹, 上传文件, 删除文件, 移动文件等.
 *
 * @author zhaojun
 */
@Tag(name = "文件操作模块")
@ApiSort(3)
@Slf4j
@RestController
@RequestMapping("/api/file/operator")
public class FileOperatorController {

	@Resource
	private PasswordConfigService passwordConfigService;

	@ApiOperationSupport(order = 1)
	@Operation(summary = "创建文件夹")
	@PostMapping("/mkdir")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	@DemoDisable
	public AjaxJson<Void> mkdir(@Valid @RequestBody NewFolderRequest newFolderRequest) {
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(newFolderRequest.getStorageKey());
		boolean flag = fileService.newFolder(newFolderRequest.getPath(), newFolderRequest.getName());
		if (flag) {
			return AjaxJson.getSuccess("创建成功");
		} else {
			return AjaxJson.getError("创建失败");
		}
	}


	@ApiOperationSupport(order = 2)
	@Operation(summary = "批量删除文件/文件夹")
	@PostMapping("/delete/batch")
	@DemoDisable
	public AjaxJson<List<BatchOperatorResult>> deleteFile(@Valid @RequestBody BatchDeleteRequest batchDeleteRequest) {
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(batchDeleteRequest.getStorageKey());
		if (fileService == null) {
			throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
		}

		List<BatchDeleteRequest.DeleteItem> deleteItems = batchDeleteRequest.getDeleteItems();

		Map<String, Boolean> pathCheckCache = new HashMap<>();

		List<BatchOperatorResult> batchOperatorResults = new ArrayList<>();

		for (BatchDeleteRequest.DeleteItem deleteItem : deleteItems) {
			// 检查权限
			String deletePath = deleteItem.getPath();
			String deleteName = deleteItem.getName();
			FileTypeEnum deleteType = deleteItem.getType();

			Boolean pathCheckResult = pathCheckCache.get(deletePath);

			// 缓存值为 false, 则即为失败, 直接跳过此删除的文件
			if (BooleanUtils.isFalse(pathCheckResult)) {
				batchOperatorResults.add(BatchOperatorResult.fail(deletePath, deleteName, "密码错误"));
				continue;
			}

			// 缓存没有, 则进行校验
			if (pathCheckResult == null) {
				String fullPath = StringUtils.concat(fileService.getCurrentUserBasePath(), deletePath);
				VerifyResultDTO verifyResultDTO = passwordConfigService.verifyPassword(fileService.getStorageId(), fullPath, deleteItem.getPassword());
				// 校验不通过, 则跳过此删除的文件
				if (!verifyResultDTO.isPassed()) {
					log.warn("因密码原因删除失败, 类型: {}, 路径: {}, 名称: {}, 原因: {}", deleteType, deletePath, deleteName, verifyResultDTO.getErrorCode());
					pathCheckCache.put(deletePath, false);
					batchOperatorResults.add(BatchOperatorResult.fail(deletePath, deleteName, "密码错误"));
					continue;
				}
				pathCheckCache.put(deletePath, true);
			}

			boolean flag = false;
			try {
				if (deleteType == FileTypeEnum.FILE) {
					flag = fileService.deleteFile(deletePath, deleteName);
				} else if (deleteType == FileTypeEnum.FOLDER) {
					flag = fileService.deleteFolder(deletePath, deleteName);
				}

				if (flag) {
					batchOperatorResults.add(BatchOperatorResult.success(deletePath, deleteName));
				} else {
					batchOperatorResults.add(BatchOperatorResult.fail(deletePath, deleteName, "操作失败"));
				}
			} catch (Exception e) {
				log.error("删除文件/文件夹失败, 文件路径: {}, 文件名称: {}", deletePath, deleteName, e);
				batchOperatorResults.add(BatchOperatorResult.fail(deletePath, deleteName, e.getMessage()));
			}
		}

		return AjaxJson.getSuccessData(batchOperatorResults);
	}


	@ApiOperationSupport(order = 3)
	@Operation(summary = "重命名文件")
	@PostMapping("/rename/file")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	@DemoDisable
	public AjaxJson<Void> rename(@Valid @RequestBody RenameFileRequest renameFileRequest) {
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(renameFileRequest.getStorageKey());
		boolean flag = fileService.renameFile(renameFileRequest.getPath(), renameFileRequest.getName(), renameFileRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}


	@ApiOperationSupport(order = 4)
	@Operation(summary = "重命名文件夹")
	@PostMapping("/rename/folder")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	@DemoDisable
	public AjaxJson<Void> renameFolder(@Valid @RequestBody RenameFolderRequest renameFolderRequest) {
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(renameFolderRequest.getStorageKey());
		boolean flag = fileService.renameFolder(renameFolderRequest.getPath(), renameFolderRequest.getName(), renameFolderRequest.getNewName());
		if (flag) {
			return AjaxJson.getSuccess("重命名成功");
		} else {
			return AjaxJson.getError("重命名失败");
		}
	}

	@ApiOperationSupport(order = 5)
	@Operation(summary = "上传文件")
	@PostMapping("/upload/file")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].password")
	@DemoDisable
	public AjaxJson<String> getUploadFileUrl(@Valid @RequestBody UploadFileRequest uploadFileRequest) {
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(uploadFileRequest.getStorageKey());
		String uploadUrl = fileService.getUploadUrl(uploadFileRequest.getPath(),
				uploadFileRequest.getName(), uploadFileRequest.getSize());
		return AjaxJson.getSuccessData(uploadUrl);
	}

	@ApiOperationSupport(order = 6)
	@Operation(summary = "(移动/复制)(文件/文件夹)")
	@PostMapping("/{action:move|copy}/{type:file|folder}")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].path",
			passwordFieldExpression = "[0].srcPathPassword")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
			pathFieldExpression = "[0].targetPath",
			passwordFieldExpression = "[0].targetPathPassword")
	@DemoDisable
	public AjaxJson<List<BatchOperatorResult>> moveFile(@Valid @RequestBody BatchMoveOrCopyFileRequest batchMoveOrCopyFileRequest,
								@PathVariable("action") String action,
								@PathVariable("type") String type) {
		if (batchMoveOrCopyFileRequest.getNameList().size() != batchMoveOrCopyFileRequest.getTargetNameList().size()) {
			throw new BizException(ErrorCode.BIZ_BAD_REQUEST);
		}

		String storageKey = batchMoveOrCopyFileRequest.getStorageKey();
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageKey(storageKey);

		Map<String, String> dictMap = new HashMap<>() {{
            put("move", "移动");
            put("copy", "复制");
            put("file", "文件");
            put("folder", "文件夹");
        }};

		List<BatchOperatorResult> batchOperatorResults = new ArrayList<>();

		List<String> targetNameList = batchMoveOrCopyFileRequest.getTargetNameList();
		String srcPath = batchMoveOrCopyFileRequest.getPath();
		String targetPath = batchMoveOrCopyFileRequest.getTargetPath();

		for (int i = 0; i < targetNameList.size(); i++) {
			String srcName = batchMoveOrCopyFileRequest.getNameList().get(i);
			String targetName = batchMoveOrCopyFileRequest.getTargetNameList().get(i);

			if (StringUtils.isBlank(srcName) || StringUtils.isBlank(targetName) || StringUtils.isBlank(srcPath) || StringUtils.isBlank(targetPath)) {
				batchOperatorResults.add(BatchOperatorResult.fail(srcPath, srcName, "参数错误"));
				continue;
			}

			// 判断不能移动/复制到自己的子文件夹下
			String srcFullPath = StringUtils.concat(srcPath, srcName);
			if (targetPath.startsWith(srcFullPath)) {
				batchOperatorResults.add(BatchOperatorResult.fail(srcPath, srcName, "不能" + dictMap.get(action) + dictMap.get(type) + "到自己的子文件夹下"));
				continue;
			}

			boolean flag = false;
			try {
				if ("move".equals(action)) {
					if ("file".equals(type)) {
						flag = fileService.moveFile(srcPath, srcName, targetPath, targetName);
					} else if ("folder".equals(type)) {
						flag = fileService.moveFolder(srcPath, srcName, targetPath, targetName);
					}
				} else if ("copy".equals(action)) {
					if ("file".equals(type)) {
						flag = fileService.copyFile(srcPath, srcName, targetPath, targetName);
					} else if ("folder".equals(type)) {
						flag = fileService.copyFolder(srcPath, srcName, targetPath, targetName);
					}
				}

				if (flag) {
					batchOperatorResults.add(BatchOperatorResult.success(srcPath, srcName));
				} else {
					batchOperatorResults.add(BatchOperatorResult.fail(srcPath, srcName, "操作失败"));
				}
			} catch (Exception e) {
				if (e instanceof BizException bizException) {
					if (!Objects.equals(bizException.getCode(), ErrorCode.BIZ_UNSUPPORTED_OPERATION.getCode())) {
						log.warn("批量{}{}失败，源文件路径: {}, 源文件名称: {}, 目标文件路径: {}, 目标文件名称: {}, err: {}", dictMap.get(action), dictMap.get(type), srcPath, srcName, targetPath, targetName, e.getMessage());
					}
				} else {
					log.error("批量{}{}失败，源文件路径: {}, 源文件名称: {}, 目标文件路径: {}, 目标文件名称: {}", dictMap.get(action), dictMap.get(type), srcPath, srcName, targetPath, targetName, e);
				}
				batchOperatorResults.add(BatchOperatorResult.fail(srcPath, srcName, e.getMessage()));
			}
		}

		return AjaxJson.getSuccessData(batchOperatorResults);
	}

}