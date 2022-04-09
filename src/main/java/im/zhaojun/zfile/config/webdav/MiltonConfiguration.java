package im.zhaojun.zfile.config.webdav;

import im.zhaojun.zfile.config.webdav.adapter.WebDavUrlAdapterImpl;
import im.zhaojun.zfile.config.webdav.auth.SystemConfigSecurityManager;
import im.zhaojun.zfile.config.webdav.resolver.WebDavRedirectViewResolver;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.service.SystemConfigService;
import io.milton.http.ResourceFactory;
import io.milton.http.SecurityManager;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.fs.NullSecurityManager;
import io.milton.servlet.DefaultMiltonConfigurator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Milton(webDav)配置
 *
 * @author me
 * @date 2022/4/9
 */
@Configuration
public class MiltonConfiguration extends DefaultMiltonConfigurator implements InitializingBean {
    /**
     * 安全管理器
     */
    private static SecurityManager securityManager = new NullSecurityManager();

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 构建milton初始化配置
     */
    @Override
    protected void build() {
        builder.setSecurityManager(securityManager);
        builder.setContextPath(ZFileConstant.WEB_DAV_PREFIX);
        builder.setUrlAdapter(new WebDavUrlAdapterImpl());
        final ResourceFactory resourceFactory = builder.getResourceFactory();
        if (resourceFactory instanceof AnnotationResourceFactory) {
            ((AnnotationResourceFactory) resourceFactory).setViewResolver(new WebDavRedirectViewResolver());
        }
        super.build();
    }

    /**
     * 属性初始化完成后，更新安全管理器，使用系统配置鉴权
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        if (systemConfig != null) {
            securityManager = new SystemConfigSecurityManager(systemConfig);
        }
    }
}
