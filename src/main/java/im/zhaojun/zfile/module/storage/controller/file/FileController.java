package im.zhaojun.zfile.module.storage.controller.file;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.InvalidStorageSourceBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import im.zhaojun.zfile.module.storage.annotation.ProCheck;
import im.zhaojun.zfile.module.storage.chain.FileChain;
import im.zhaojun.zfile.module.storage.chain.FileContext;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.convert.StorageSourceConvert;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.request.base.FileItemRequest;
import im.zhaojun.zfile.module.storage.model.request.base.FileListRequest;
import im.zhaojun.zfile.module.storage.model.request.base.SearchStorageRequest;
import im.zhaojun.zfile.module.storage.model.result.FileInfoResult;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.model.result.StorageSourceResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件列表相关接口, 如展示存储源列表, 展示文件列表, 搜索文件列表等.
 *
 * @author zhaojun
 */
@Tag(name = "文件列表模块")
@ApiSort(2)
@Slf4j
@RequestMapping("/api/storage")
@RestController
public class FileController {
	
	@Resource
	private StorageSourceService storageSourceService;
	
	@Resource
	private FileChain fileChain;
	
	@Resource
	private StorageSourceConvert storageSourceConvert;
	
	
	@ApiOperationSupport(order = 1)
	@Operation(summary = "获取存储源列表", description = "获取所有已启用的存储源, 并且按照后台顺序排序")
	@GetMapping("/list")
	@ProCheck
	public AjaxJson<List<StorageSourceResult>> storageList() {
		List<StorageSource> storageList = storageSourceService.findAllEnableOrderByOrderNum(ZFileAuthUtil.getCurrentUserId());
		List<StorageSourceResult> storageSourceResultList =
				storageSourceConvert.entityToResultList(storageList);
		return AjaxJson.getSuccessData(storageSourceResultList);
	}
	
	
	@ApiOperationSupport(order = 2)
	@Operation(summary = "获取文件列表", description = "获取某个存储源下, 指定路径的文件&文件夹列表")
	@PostMapping("/files")
	public AjaxJson<FileInfoResult> list(@Valid @RequestBody FileListRequest fileListRequest) throws Exception {
		String storageKey = fileListRequest.getStorageKey();
		Integer storageId = storageSourceService.findIdByKey(storageKey);
		if (storageId == null) {
			throw new InvalidStorageSourceBizException(storageKey);
		}
		
		// 处理请求参数默认值
		fileListRequest.handleDefaultValue();
		
		// 获取文件列表
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageId(storageId);
		List<FileItemResult> fileItemList = fileService.fileList(fileListRequest.getPath());
		
		// 执行责任链
		FileContext fileContext = FileContext.builder()
				.storageId(storageId)
				.fileListRequest(fileListRequest)
				.fileItemList(fileItemList)
				.fileService(fileService)
				.build();
		fileChain.execute(fileContext);
		
		return AjaxJson.getSuccessData(new FileInfoResult(fileContext.getFileItemList(), fileContext.getPasswordPattern()));
	}
	
	
	@ApiOperationSupport(order = 3)
	@Operation(summary = "获取单个文件信息", description = "获取某个存储源下, 单个文件的信息")
	@PostMapping("/file/item")
	@CheckPassword(storageKeyFieldExpression = "[0].storageKey",
					pathFieldExpression = "[0].path",
					pathIsDirectory = false,
					passwordFieldExpression = "[0].password")
	public AjaxJson<FileItemResult> fileItem(@Valid @RequestBody FileItemRequest fileItemRequest) {
		String storageKey = fileItemRequest.getStorageKey();
		Integer storageId = storageSourceService.findIdByKey(storageKey);
		if (storageId == null) {
			throw new InvalidStorageSourceBizException(storageKey);
		}
		
		// 处理请求参数默认值
		fileItemRequest.handleDefaultValue();
		
		// 获取文件列表
		AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageId(storageId);
		
		FileItemResult fileItemResult;
		try {
			fileItemResult = fileService.getFileItem(fileItemRequest.getPath());
		} catch (Exception e) {
			throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "获取文件信息失败: " + e.getMessage());
		}
		
		return AjaxJson.getSuccessData(fileItemResult);
	}
	
}