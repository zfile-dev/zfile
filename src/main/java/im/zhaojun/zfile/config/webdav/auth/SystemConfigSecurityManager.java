
package im.zhaojun.zfile.config.webdav.auth;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 基于当前系统配置的WebDav鉴权管理器
 *
 * @author me
 * @date 2022/4/10
 * @see io.milton.http.fs.SimpleSecurityManager
 */
public class SystemConfigSecurityManager implements io.milton.http.SecurityManager {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigSecurityManager.class);

    private String realm = "SystemConfig";
    private Map<String, String> nameAndPasswords;

    /**
     * 根据系统配置创建安全管理器
     *
     * @param systemConfig 系统配置DTO
     */
    public SystemConfigSecurityManager(SystemConfigDTO systemConfig) {
        if (systemConfig != null) {
            this.nameAndPasswords = MapUtil.of(systemConfig.getUsername(), systemConfig.getPassword());
        }
    }

    public Object getUserByName(String name) {
        String actualPassword = nameAndPasswords.get(name);
        if (actualPassword != null) {
            return name;
        }
        return null;
    }

    /**
     * 用户名+密码身份验证
     *
     * @param user     用户
     * @param password 密码
     * @return {@link Object}
     */
    @Override
    public Object authenticate(String user, String password) {
        if (user.contains("@")) {
            user = user.substring(0, user.indexOf("@"));
        }
        String actualPassword = nameAndPasswords.get(user);
        if (actualPassword == null) {
            log.debug("user not found: " + user);
            return null;
        } else {
            //比对密码MD5摘要
            return (actualPassword.equals(SecureUtil.md5(password))) ? user : null;
        }
    }

    /**
     * 请求摘要身份验证(不进行换算)
     *
     * @param digestRequest 消化的请求
     * @return {@link Object}
     */
    @Override
    public Object authenticate(DigestResponse digestRequest) {
        String serverResponse = nameAndPasswords.get(digestRequest.getUser());
        String clientResponse = digestRequest.getResponseDigest();
        //比对密码MD5摘要
        if (serverResponse.equals(SecureUtil.md5(clientResponse))) {
            return "ok";
        } else {
            return null;
        }
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth, Resource resource) {
        if (auth == null) {
            log.trace("authorise: declining because there is no auth object");
            return false;
        } else {
            if (auth.getTag() == null) {
                log.trace("authorise: declining because there is no auth.getTag() object");
                return false;
            } else {
                log.trace("authorise: permitting because there is an authenticated user associated with this request");
                return true;
            }
        }
    }

    @Override
    public String getRealm(String host) {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setNameAndPasswords(Map<String, String> nameAndPasswords) {
        this.nameAndPasswords = nameAndPasswords;
    }


    @Override
    public boolean isDigestAllowed() {
        // 关闭请求摘要换算，client端请求时若换算为摘要，则无法和系统设置中获取的密码MD5比对
        return false;
    }

}

