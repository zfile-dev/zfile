package im.zhaojun.zfile.module.log.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.link.model.request.QueryLoginLogRequest;
import im.zhaojun.zfile.module.log.model.entity.LoginLog;
import im.zhaojun.zfile.module.log.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * 用户登录日志接口
 *
 * @author zhaojun
 */
@Tag(name = "登录日志管理")
@ApiSort(7)
@Controller
@RequestMapping("/admin/login/log")
public class LoginLogController {
    
    @Resource
    private LoginLogService loginLogService;

    @ApiOperationSupport(order = 1)
    @GetMapping("/list")
    @Operation(summary = "登录日志列表")
    @ResponseBody
    public AjaxJson<List<LoginLog>> list(QueryLoginLogRequest queryLoginLogRequest) {
        // 分页和排序
        boolean asc = Objects.equals(queryLoginLogRequest.getOrderDirection(), "asc");
        OrderItem orderItem = asc ? OrderItem.asc(queryLoginLogRequest.getOrderBy()) : OrderItem.desc(queryLoginLogRequest.getOrderBy());
        Page<LoginLog> pages = new Page<LoginLog>(queryLoginLogRequest.getPage(), queryLoginLogRequest.getLimit())
                .addOrder(orderItem);

        LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<LoginLog>()
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getUsername()), LoginLog::getUsername, queryLoginLogRequest.getUsername())
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getPassword()), LoginLog::getPassword, queryLoginLogRequest.getPassword())
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getIp()), LoginLog::getIp, queryLoginLogRequest.getIp())
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getUserAgent()), LoginLog::getUserAgent, queryLoginLogRequest.getUserAgent())
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getReferer()), LoginLog::getReferer, queryLoginLogRequest.getReferer())
                .like(StringUtils.isNotEmpty(queryLoginLogRequest.getResult()), LoginLog::getResult, queryLoginLogRequest.getResult())
                .ge(ObjUtil.isNotEmpty(queryLoginLogRequest.getDateFrom()), LoginLog::getCreateTime, queryLoginLogRequest.getDateFrom())
                .le(ObjUtil.isNotEmpty(queryLoginLogRequest.getDateTo()), LoginLog::getCreateTime, queryLoginLogRequest.getDateTo());

        Page<LoginLog> selectResult = loginLogService.selectPage(pages, queryWrapper);
        return AjaxJson.getPageData(selectResult.getTotal(), selectResult.getRecords());
    }
    
}