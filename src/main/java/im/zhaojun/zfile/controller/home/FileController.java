package im.zhaojun.zfile.controller.home;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.exception.NotExistFileException;
import im.zhaojun.zfile.exception.PasswordVerifyException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.dto.SystemFrontConfigDTO;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.model.support.VerifyResult;
import im.zhaojun.zfile.service.DriveConfigService;
import im.zhaojun.zfile.service.FilterConfigService;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.FileComparator;
import im.zhaojun.zfile.util.HttpUtil;
import im.zhaojun.zfile.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Value("${zfile.debug}")
    private Boolean debug;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DriveContext driveContext;

    @Resource
    private DriveConfigService driveConfigService;

    @Resource
    private FilterConfigService filterConfigService;


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
     * 获取某个驱动器下, 指定路径的数据
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
     * @return  当前路径下所有文件及文件夹
     */
    @GetMapping("/list/{driveId}")
    public ResultBean list(@PathVariable(name = "driveId") Integer driveId,
                           @RequestParam(defaultValue = "/") String path,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String orderBy,
                           @RequestParam(required = false, defaultValue = "asc") String orderDirection) throws Exception {
        AbstractBaseFileService fileService = driveContext.get(driveId);
        List<FileItemDTO> fileItemList = fileService.fileList(StringUtils.removeDuplicateSeparator(ZFileConstant.PATH_SEPARATOR + path + ZFileConstant.PATH_SEPARATOR));

        // 创建副本, 防止排序和过滤对原数据产生影响
        List<FileItemDTO> copyList = new ArrayList<>(fileItemList);

        // 校验密码, 如果校验不通过, 则返回错误消息
        VerifyResult verifyResult = verifyPassword(copyList, driveId, path, password);
        if (!verifyResult.isPassed()) {
            return ResultBean.error(verifyResult.getMsg(), verifyResult.getCode());
        }

        // 过滤掉驱动器配置的表达式中要隐藏的数据
        filterFileList(copyList, driveId);

        // 按照自然排序
        copyList.sort(new FileComparator(orderBy, orderDirection));
        return ResultBean.successData(copyList);
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
        systemConfig.setDebugMode(debug);
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
                log.trace("不存在 README 文件, 已跳过, fullPath: {}, fileItem: {}", fullPath, JSON.toJSONString(fileItem));
            } else {
                log.trace("获取 README 文件异常, fullPath: {}, fileItem: {}", fullPath, JSON.toJSONString(fileItem), e);
            }
        }

        return ResultBean.successData(systemConfig);
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


    /**
     * 校验密码
     * @param   fileItemList
     *          文件列表
     * @param   driveId
     *          驱动器 ID
     * @param   path
     *          请求路径
     * @param   inputPassword
     *          用户输入的密码
     * @return  是否校验通过
     */
    private VerifyResult verifyPassword(List<FileItemDTO> fileItemList, Integer driveId, String path, String inputPassword) {
        AbstractBaseFileService fileService = driveContext.get(driveId);

        for (FileItemDTO fileItemDTO : fileItemList) {
            if (ZFileConstant.PASSWORD_FILE_NAME.equals(fileItemDTO.getName())) {
                String expectedPasswordContent;
                try {
                    expectedPasswordContent = HttpUtil.getTextContent(fileItemDTO.getUrl());
                } catch (HttpClientErrorException httpClientErrorException) {
                    log.trace("尝试重新获取密码文件缓存中链接后仍失败, driveId: {}, path: {}, inputPassword: {}, passwordFile:{} ",
                            driveId, path, inputPassword, JSON.toJSONString(fileItemDTO), httpClientErrorException);
                    try {
                        String pwdFileFullPath = StringUtils.removeDuplicateSeparator(fileItemDTO.getPath() + ZFileConstant.PATH_SEPARATOR + fileItemDTO.getName());
                        FileItemDTO pwdFileItem = fileService.getFileItem(pwdFileFullPath);
                        expectedPasswordContent = HttpUtil.getTextContent(pwdFileItem.getUrl());
                    } catch (Exception e) {
                        throw new PasswordVerifyException("此文件夹未加密文件夹, 但密码检查异常, 请联系管理员检查密码设置", e);
                    }
                }

                if (matchPassword(expectedPasswordContent, inputPassword)) {
                    break;
                }

                if (StrUtil.isEmpty(inputPassword)) {
                    return VerifyResult.fail("此文件夹需要密码.", ResultBean.REQUIRED_PASSWORD);
                }
                return VerifyResult.fail("密码错误.", ResultBean.INVALID_PASSWORD);
            }
        }

        return VerifyResult.success();
    }


    /**
     * 校验两个密码是否相同, 忽略空白字符
     *
     * @param   expectedPasswordContent
     *          预期密码
     *
     * @param   password
     *          实际输入密码
     *
     * @return  是否匹配
     */
    private boolean matchPassword(String expectedPasswordContent, String password) {
        if (Objects.equals(expectedPasswordContent, password)) {
            return true;
        }

        if (expectedPasswordContent == null) {
            return false;
        }

        if (password == null) {
            return false;
        }

        expectedPasswordContent = expectedPasswordContent.replace("\n", "").trim();
        password = password.replace("\n", "").trim();
        return Objects.equals(expectedPasswordContent, password);
    }


    /**
     * 过滤文件列表, 去除密码, 文档文件和此驱动器通过规则过滤的文件.
     *
     * @param   fileItemList
     *          文件列表
     * @param   driveId
     *          驱动器 ID
     */
    private void filterFileList(List<FileItemDTO> fileItemList, Integer driveId) {
        if (fileItemList == null) {
            return;
        }

        fileItemList.removeIf(
                fileItem -> ZFileConstant.PASSWORD_FILE_NAME.equals(fileItem.getName())
                        || ZFileConstant.README_FILE_NAME.equals(fileItem.getName())
                        || filterConfigService.filterResultIsHidden(driveId, StringUtils.concatUrl(fileItem.getPath(), fileItem.getName()))
        );
    }

}