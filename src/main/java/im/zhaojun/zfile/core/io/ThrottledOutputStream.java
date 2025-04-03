package im.zhaojun.zfile.core.io;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 使用装饰器模式, 限速输出流, 单位为字节/秒.
 *
 * @author zhaojun
 */
@Slf4j
public final class ThrottledOutputStream extends OutputStream {

    private final OutputStream originalOutputStream;
    private final RateLimiter rateLimiter;

    public ThrottledOutputStream(OutputStream out, double bytesPerSecond) {
        this.originalOutputStream = out;
        this.rateLimiter = RateLimiter.create(bytesPerSecond);
    }

    public void setRate(double bytesPerSecond) {
        rateLimiter.setRate(bytesPerSecond);
    }

    @Override
    public void write(int b) throws IOException {
        rateLimiter.acquire();
        originalOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        rateLimiter.acquire(b.length);
        originalOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        rateLimiter.acquire(len);
        originalOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        originalOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        originalOutputStream.close();
    }

}