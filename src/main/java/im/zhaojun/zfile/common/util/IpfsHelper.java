package im.zhaojun.zfile.common.util;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * IPFS 库的扩展
 *
 * @author wswm152
 */
public class IpfsHelper {
    /**
     * ipfs 实例
     */
    private final IPFS ipfs;
    /**
     * 从IPFS库反射的方法
     */
    private final Method m_retrieveMap, m_retrieve;

    public final IPFS.File file;
    /**
     * IPFS库的扩展实现，不是标准的接口实现
     */
    public final Files files = new Files();


    public IpfsHelper(String apiAddr) {

        IPFS ipfs = new IPFS(apiAddr);
        this.ipfs = ipfs;
        file = ipfs.file;


        try {
            //私有函数的反射
            Class<?> ipfs_p = Class.forName("io.ipfs.api.IPFS");
            m_retrieveMap = ipfs_p.getDeclaredMethod("retrieveMap", String.class);
            m_retrieveMap.setAccessible(true);
            m_retrieve = ipfs_p.getDeclaredMethod("retrieve", String.class);
            m_retrieve.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public class Files {
        public Map ls(String path) throws IOException {
            return retrieveMap("files/ls?arg=" + path);
        }

        public Map stat(String path) throws IOException {
            return retrieveMap("files/stat?arg=" + path);
        }

        public String mkdir(String path) throws IOException {
            return new String(retrieve("files/mkdir?arg=" + path + "&parents=true"));
        }

        public String rm(String path) throws IOException {
            return new String(retrieve("files/rm?arg=" + path + "&recursive=true"));
        }

        public String mv(String source, String dest) throws IOException {
            return new String(retrieve("files/mv?arg=" + source + "&arg=" + dest));
        }

        public String cp(String source, String dest) throws IOException {
            return new String(retrieve("files/cp?arg=" + source + "&arg=" + dest));
        }

        public String upload(InputStream stream, String dest) throws IOException {
            NamedStreamable.InputStreamWrapper inputsteam = new NamedStreamable.InputStreamWrapper(stream);
            String hash = add(inputsteam).get(0).hash.toString();
            return cp("/ipfs/".concat(hash), dest);
        }

        public Long download(String path, OutputStream outputStream) {
            return HttpUtil.downloadFile(path,outputStream);
        }
    }

    public List<MerkleNode> add(NamedStreamable file) throws IOException {
        return ipfs.add(file);
    }

    private Map retrieveMap(String path) throws IOException {
        try {
            return (Map) m_retrieveMap.invoke(ipfs, path);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (IOException) e.getTargetException();
        }
    }

    private byte[] retrieve(String path) throws IOException {
        try {
            return (byte[]) m_retrieve.invoke(ipfs, path);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (IOException) e.getTargetException();
        }
    }
}

