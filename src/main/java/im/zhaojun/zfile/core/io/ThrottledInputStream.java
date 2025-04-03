package im.zhaojun.zfile.core.io;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * 使用装饰器模式, 限速输入流, 单位为字节/秒.
 *
 * @author zhaojun
 */
public final class ThrottledInputStream extends InputStream {

    private final InputStream originalInputStream;
    private final RateLimiter rateLimiter;

    public ThrottledInputStream(InputStream originalInputStream, double bytesPerSecond) {
        this.originalInputStream = originalInputStream;
        this.rateLimiter = RateLimiter.create(bytesPerSecond);
    }

    @Override
    public int read() throws IOException {
        rateLimiter.acquire();
        return originalInputStream.read();
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return originalInputStream.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        rateLimiter.acquire(len);
        return originalInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return originalInputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return originalInputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return originalInputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return originalInputStream.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        originalInputStream.skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return originalInputStream.available();
    }

    @Override
    public void close() throws IOException {
        originalInputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        originalInputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        originalInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return originalInputStream.markSupported();
    }

}