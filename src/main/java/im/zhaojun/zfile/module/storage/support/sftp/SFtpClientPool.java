package im.zhaojun.zfile.module.storage.support.sftp;

import cn.hutool.extra.ssh.Sftp;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class SFtpClientPool extends GenericObjectPool<Sftp> {

    public SFtpClientPool(PooledObjectFactory<Sftp> factory) {
        super(factory);
    }

    public SFtpClientPool(PooledObjectFactory<Sftp> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}