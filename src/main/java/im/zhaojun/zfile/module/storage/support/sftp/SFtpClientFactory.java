package im.zhaojun.zfile.module.storage.support.sftp;

import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SFtpClientFactory extends BasePooledObjectFactory<Sftp> {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String privateKey;
    private final String passphrase;

    private final Charset charset;

    static {
        JSch.setConfig("StrictHostKeyChecking", "no");
    }

    public SFtpClientFactory(String host, int port, String username, String password, String privateKey, String passphrase, Charset charset) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.charset = charset;
    }

    @Override
    public Sftp create() throws Exception {
        // 密码登录
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        if (StringUtils.isBlank(privateKey)) {
            session.setPassword(password);
        } else {
            byte[] passphraseBytes = null;
            if (passphrase != null && !passphrase.isEmpty()) {
                passphraseBytes = passphrase.getBytes(StandardCharsets.UTF_8);
            }
            jsch.addIdentity(username, privateKey.getBytes(StandardCharsets.UTF_8), null, passphraseBytes);
        }
        Sftp sftp = new Sftp(session, charset, StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        log.debug("Creating object: {}", sftp);
        return sftp;
    }

    @Override
    public PooledObject<Sftp> wrap(Sftp sftpClient) {
        return new DefaultPooledObject(sftpClient);
    }

    @Override
    public boolean validateObject(PooledObject<Sftp> p) {
        String pwd = null;
        try {
            pwd = p.getObject().pwd();
        } catch (Exception fex) {
            // ignore
        }
        boolean isValid = pwd != null;
        log.debug("Validating object: {} isValid: {}", p.getObject(), isValid);
        return isValid;
    }

    @Override
    public void destroyObject(PooledObject<Sftp> p) throws Exception {
        p.getObject().close();
        log.debug("Destroying object: {}", p.getObject());
    }

}