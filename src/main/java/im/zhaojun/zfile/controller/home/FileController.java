package im.zhaojun.zfile.controller.home;

import com.alibaba.fastjson.JSON;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.exception.NotExistFileException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.dto.SystemFrontConfigDTO;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.model.support.FilePageModel;
import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.DriveConfigService;
import im.zhaojun.zfile.service.FilterConfigService;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.FileComparator;
import im.zhaojun.zfile.util.HttpUtil;
import im.zhaojun.zfile.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 前台文件管理
 * @author zhaojun
 */
@Slf4j
@RequestMapping("/api")
@RestController
public class FileController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DriveContext driveContext;

    @Resource
    private DriveConfigService driveConfigService;

    @Resource
    private FilterConfigService filterConfigService;

    /**
     * 滚动加载每页条数.
     */
    private static final Integer PAGE_SIZE = 30;


    /**
     * 获取所有已启用的驱动器
     *
     * @return  所有已启用驱动器
     */
    @GetMapping("/drive/list")
    public ResultBean drives() {
        return ResultBean.success(driveConfigService.listOnlyEnable());
    }

    /**
     * 获取某个驱动器下, 指定路径的数据, 每页固定 {@link #PAGE_SIZE} 条数据.
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   path
     *          路径
     *
     * @param   password
     *          文件夹密码, 某些文件夹需要密码才能访问, 当不需要密码时, 此参数可以为空
     *
     * @param   page
     *          页数
     *
     * @return  当前路径下所有文件及文件夹
     */
    @GetMapping("/list/{driveId}")
    public ResultBean list(@PathVariable(name = "driveId") Integer driveId,
                           @RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String password,
                           @RequestParam(defaultValue = "1") Integer page) throws Exception {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        List<FileItemDTO> fileItemList =
                fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));

        for (FileItemDTO fileItemDTO : fileItemList) {
            if (ZFileConstant.PASSWORD_FILE_NAME.equals(fileItemDTO.getName())) {
                String expectedPasswordContent;
                try {
                    expectedPasswordContent = HttpUtil.getTextContent(fileItemDTO.getUrl());
                } catch (HttpClientErrorException httpClientErrorException) {
                    log.error("尝试重新获取密码文件缓存中链接后仍失败, driveId: {}, path: {}, inputPassword: {}, passwordFile:{} ",
                            driveId, path, password, JSON.toJSONString(fileItemDTO), httpClientErrorException);
                    try {
                        String fullPath = StringUtils.removeDuplicateSeparator(fileItemDTO.getPath() + ZFileConstant.PATH_SEPARATOR + fileItemDTO.getName());
                        FileItemDTO fileItem = fileService.getFileItem(fullPath);
                        expectedPasswordContent = HttpUtil.getTextContent(fileItem.getUrl());
                    } catch (Exception e) {
                        log.error("尝试重新获取密码文件链接后仍失败, 已暂时取消密码", e);
                        break;
                    }
                }

                if (Objects.equals(expectedPasswordContent, password)) {
                    break;
                }

                if (password != null && !"".equals(password)) {
                    return ResultBean.error("密码错误.", ResultBean.INVALID_PASSWORD);
                }
                return ResultBean.error("此文件夹需要密码.", ResultBean.REQUIRED_PASSWORD);
            }
        }

        // 过滤掉表达式中不存在的数据.
        fileItemList.removeIf(next -> filterConfigService.filterResultIsHidden(driveId, StringUtils.concatUrl(next.getPath(), next.getName())));
        return ResultBean.successData(getSortedPagingData(fileItemList, page));
    }

    /**
     * 获取系统配置信息和当前页的标题, 页面文档信息
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  返回指定驱动器的系统配置信息
     */
    @GetMapping("/config/{driveId}")
    public ResultBean getConfig(@PathVariable(name = "driveId") Integer driveId, String path) {
        SystemFrontConfigDTO systemConfig = systemConfigService.getSystemFrontConfig(driveId);

        AbstractBaseFileService fileService = driveContext.get(driveId);
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        String fullPath = StringUtils.removeDuplicateSeparator(path + ZFileConstant.PATH_SEPARATOR + ZFileConstant.README_FILE_NAME);
        FileItemDTO fileItem = null;
        try {
            fileItem = fileService.getFileItem(fullPath);

            if (!Objects.equals(driveConfig.getType(), StorageTypeEnum.FTP)) {
                String readme = HttpUtil.getTextContent(fileItem.getUrl());
                systemConfig.setReadme(readme);
            }
        } catch (Exception e) {
            if (e instanceof NotExistFileException) {
                log.debug("不存在 README 文件, 已跳过, fullPath: {}, fileItem: {}", fullPath, JSON.toJSONString(fileItem));
            } else {
                log.error("获取 README 文件异常, fullPath: {}, fileItem: {}", fullPath, JSON.toJSONString(fileItem), e);
            }
        }

        return ResultBean.successData(systemConfig);
    }


    @GetMapping("/search/{driveId}")
    public ResultBean search(@RequestParam(value = "name", defaultValue = "/") String name,
                             @RequestParam(defaultValue = "name") String sortBy,
                             @RequestParam(defaultValue = "asc") String order,
                             @RequestParam(defaultValue = "1") Integer page,
                             @PathVariable("driveId") Integer driveId) {
        return ResultBean.error("暂不支持搜索功能");
    }


    /**
     * 过滤文件列表, 去除密码, 文档文件.
     *
     * @param   fileItemList
     *          文件列表
     */
    private void filterFileList(List<FileItemDTO> fileItemList) {
        if (fileItemList == null) {
            return;
        }

        fileItemList.removeIf(fileItem -> ZFileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())
                || ZFileConstant.README_FILE_NAME.equals(fileItem.getName()));
    }


    /**
     * 对传入的文件列表, 按照文件名进行排序, 然后取相应页数的文件
     *
     * @param   fileItemList
     *          文件列表
     *
     * @param   page
     *          要取的页数
     *
     * @return  排序及分页后的那段数据
     */
    private FilePageModel getSortedPagingData(List<FileItemDTO> fileItemList, Integer page) {
        ArrayList<FileItemDTO> copy = new ArrayList<>(Arrays.asList(new FileItemDTO[fileItemList.size()]));
        Collections.copy(copy, fileItemList);

        // 排序, 先按照文件类型比较, 文件夹在前, 文件在后, 然后根据 sortBy 字段排序, 默认为升序;
        copy.sort(new FileComparator());
        filterFileList(copy);

        int total = copy.size();
        int totalPage = (total + PAGE_SIZE - 1) / PAGE_SIZE;

        if (page > totalPage) {
            return new FilePageModel(totalPage, Collections.emptyList());
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = page * PAGE_SIZE;
        end = Math.min(end, total);
        return new FilePageModel(totalPage, copy.subList(start, end));
    }


    /**
     * 获取指定路径下的文件信息内容
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   path
     *          文件全路径
     *
     * @return  该文件的名称, 路径, 大小, 下载地址等信息.
     */
    @GetMapping("/directlink/{driveId}")
    public ResultBean directlink(@PathVariable(name = "driveId") Integer driveId, String path) {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        return ResultBean.successData(fileService.getFileItem(path));
    }

}