package im.zhaojun.zfile.module.storage.support.sftp;

import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ssh.Sftp;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.charset.Charset;

@Slf4j
public class SFtpClientFactory extends BasePooledObjectFactory<Sftp> {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Charset charset;

    public SFtpClientFactory(String host, int port, String username, String password, Charset charset) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.charset = charset;
    }

    @Override
    public Sftp create() throws Exception {
        FtpConfig ftpConfig = new FtpConfig(host, port, username, password, charset);
        ftpConfig.setConnectionTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        Sftp sftp = new Sftp(ftpConfig, true);
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