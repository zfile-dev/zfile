package im.zhaojun.zfile.module.storage.support.ftp;

import cn.hutool.extra.ftp.Ftp;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class FtpClientPool extends GenericObjectPool<Ftp> {

    public FtpClientPool(PooledObjectFactory<Ftp> factory) {
        super(factory);
    }

    public FtpClientPool(PooledObjectFactory<Ftp> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}