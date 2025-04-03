package im.zhaojun.zfile.module.storage.support.ftp;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpConfig;
import cn.hutool.extra.ftp.FtpMode;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import im.zhaojun.zfile.module.storage.service.impl.FtpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.nio.charset.Charset;

@Slf4j
public class FtpClientFactory extends BasePooledObjectFactory<Ftp> {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Charset charset;
    private final String ftpMode;


    public FtpClientFactory(String host, int port, String username, String password, Charset charset, String ftpMode) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.charset = charset;
        this.ftpMode = ftpMode;
    }

    @Override
    public Ftp create() throws Exception {
        FtpConfig ftpConfig = new FtpConfig(host, port, username, password, charset);
        ftpConfig.setConnectionTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        Ftp ftp = new Ftp(ftpConfig, FtpServiceImpl.FTP_MODE_ACTIVE.equals(ftpMode) ? FtpMode.Active : FtpMode.Passive);
        ftp.getClient().setFileType(FTP.BINARY_FILE_TYPE);
        ftp.getClient().setListHiddenFiles(true);
        log.debug("Creating object: {}", ftp);
        return ftp;
    }

    @Override
    public PooledObject<Ftp> wrap(Ftp ftpClient) {
        return new DefaultPooledObject(ftpClient);
    }

    @Override
    public boolean validateObject(PooledObject<Ftp> p) {
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
    public void destroyObject(PooledObject<Ftp> p) throws Exception {
        p.getObject().close();
        log.debug("Destroying object: {}", p.getObject());
    }

}