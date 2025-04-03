package im.zhaojun.zfile.module.user.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.request.CheckUserDuplicateRequest;
import im.zhaojun.zfile.module.user.model.request.CopyUserRequest;
import im.zhaojun.zfile.module.user.model.request.QueryUserRequest;
import im.zhaojun.zfile.module.user.model.request.SaveUserRequest;
import im.zhaojun.zfile.module.user.model.response.UserDetailResponse;
import im.zhaojun.zfile.module.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 用户管理接口
 *
 * @author zhaojun
 */
@Tag(name = "用户管理")
@ApiSort(6)
@RestController
@RequestMapping("/admin")
public class UserManagerController {

    @Resource
    private UserService userService;

    @ApiOperationSupport(order = 1)
    @GetMapping("/user/list")
    @Operation(summary = "用户列表")
    @ResponseBody
    public AjaxJson<Collection<UserDetailResponse>> list(QueryUserRequest queryObj) {
        List<UserDetailResponse> userList = userService.listUserDetail(queryObj);
        return AjaxJson.getSuccessData(userList);
    }

    @ApiOperationSupport(order = 2)
    @PostMapping("/user/saveOrUpdate")
    @Operation(summary = "添加用户")
    @ResponseBody
    @DemoDisable
    public AjaxJson<User> saveOrUpdate(@RequestBody SaveUserRequest saveUserRequest) {
        return AjaxJson.getSuccessData(userService.saveOrUpdate(saveUserRequest));
    }

    @ApiOperationSupport(order = 3)
    @DeleteMapping("/user/delete/{id}")
    @Operation(summary = "删除用户")
    @ResponseBody
    @DemoDisable
    public AjaxJson<Integer> delete(@PathVariable("id") Integer id) {
        userService.deleteById(id);
        return AjaxJson.getSuccessData(id);
    }

    @ApiOperationSupport(order = 5)
    @PostMapping("/user/enable/{id}")
    @Operation(summary = "启用用户")
    @ResponseBody
    @DemoDisable
    public AjaxJson<Integer> enable(@PathVariable Integer id) {
        userService.updateUserEnable(id, true);
        return AjaxJson.getSuccessData(id);
    }

    @ApiOperationSupport(order = 6)
    @PostMapping("/user/disable/{id}")
    @Operation(summary = "禁用用户")
    @ResponseBody
    @DemoDisable
    public AjaxJson<Integer> disable(@PathVariable Integer id) {
        userService.updateUserEnable(id, false);
        return AjaxJson.getSuccessData(id);
    }

    @ApiOperationSupport(order = 7)
    @GetMapping("/user/{id}")
    @Operation(summary = "获取用户信息")
    @ResponseBody
    public AjaxJson<UserDetailResponse> getUser(@PathVariable("id") Integer id) {
        UserDetailResponse user = userService.getUserDetailById(id);
        return AjaxJson.getSuccessData(user);
    }

    @ApiOperationSupport(order = 8)
    @GetMapping("/user/checkDuplicate")
    @Operation(summary = "检查用户名是否重复")
    @ResponseBody
    public AjaxJson<Boolean> checkDuplicate(CheckUserDuplicateRequest checkUserDuplicateRequest) {
        Integer id = checkUserDuplicateRequest.getId();
        String username = checkUserDuplicateRequest.getUsername();
        return AjaxJson.getSuccessData(userService.checkDuplicateUsername(id, username));
    }

    @ApiOperationSupport(order = 9)
    @Operation(summary = "复制用户", description ="复制用户配置")
    @PostMapping("/user/copy")
    @DemoDisable
    public AjaxJson<Integer> copyStorage(@RequestBody @Valid CopyUserRequest copyUserRequest) {
        Integer id = userService.copy(copyUserRequest);
        return AjaxJson.getSuccessData(id);
    }

}