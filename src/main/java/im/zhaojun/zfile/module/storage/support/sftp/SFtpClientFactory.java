package im.zhaojun.zfile.module.storage.support.sftp;

import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;

@Slf4j
public class SFtpClientFactory extends BasePooledObjectFactory<Sftp> {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String privateKey;

    private final Charset charset;

    public SFtpClientFactory(String host, int port, String username, String password, String privateKey, Charset charset) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.charset = charset;
    }

    @Override
    public Sftp create() throws Exception {
        System.out.println("privateKey" + privateKey);
        Sftp sftp;
        if(privateKey == null || privateKey.isBlank()) {//密码登录
            FtpConfig ftpConfig = new FtpConfig(host, port, username, password, charset);
            ftpConfig.setConnectionTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
            sftp = new Sftp(ftpConfig, true);
        } else {//密钥登录
            JSch jsch = new JSch();
            JSch.setConfig("StrictHostKeyChecking", "no");
            File tempKey = File.createTempFile(host + "-" + port + "-",".tempkeyfile");
            try(FileWriter tmpFileWriter = new FileWriter(tempKey)) {
                tmpFileWriter.write(privateKey);
            }
            jsch.addIdentity(tempKey.getAbsolutePath(), password);
            tempKey.delete();
            Session session = jsch.getSession(username, host, port);

            sftp = new Sftp(session, charset, StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        }
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