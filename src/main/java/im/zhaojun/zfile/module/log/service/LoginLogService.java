package im.zhaojun.zfile.module.log.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.module.log.mapper.LoginLogMapper;
import im.zhaojun.zfile.module.log.model.entity.LoginLog;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    public void save(LoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }

    public Page<LoginLog> selectPage(Page<LoginLog> pages, Wrapper<LoginLog> queryWrapper) {
        return loginLogMapper.selectPage(pages, queryWrapper);
    }
}
