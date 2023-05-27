package im.zhaojun.zfile.module.config.event;

import im.zhaojun.zfile.module.config.model.entity.SystemConfig;

/**
 * 系统设置修改事件
 *
 * @author zhaojun
 */
public interface ISystemConfigModifyHandler {

    /**
     * 修改系统设置时会触发此事件
     *
     */
    void modify(SystemConfig originalSystemConfig, SystemConfig newSystemConfig);

    /**
     * 判断是否匹配当前处理器
     *
     * @param   name
     *          配置项名称
     *
     * @return  是否匹配
     */
    boolean matches(String name);

}
