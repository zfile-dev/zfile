package im.zhaojun.zfile.module.storage.support.webdav;

import com.github.sardine.impl.SardineImpl;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;

import java.io.IOException;
import java.time.Duration;

/**
 * 自定义 Sardine 实现，支持设置连接超时和读取超时时间
 */
public class CustomSardine extends SardineImpl {

    private final Duration connectTimeout;

    private final Duration readTimeout;

    public CustomSardine(String username, String password, Duration connectTimeout, Duration readTimeout) {
        super();
        setCredentials(username, password);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    protected <T> T execute(HttpClientContext context, HttpRequestBase request, ResponseHandler<T> responseHandler) throws IOException {
        RequestConfig.Builder configBuilder = request.getConfig() != null ? RequestConfig.copy(request.getConfig()) : RequestConfig.custom();

        if (connectTimeout != null && connectTimeout.compareTo(Duration.ZERO) > 0) {
            configBuilder.setConnectTimeout((int) (1000 * connectTimeout.getSeconds() + connectTimeout.getNano() / 1000000));
            configBuilder.setConnectionRequestTimeout((int) (1000 * connectTimeout.getSeconds() + connectTimeout.getNano() / 1000000));
        }
        if (readTimeout != null && readTimeout.compareTo(Duration.ZERO) > 0) {
            configBuilder.setSocketTimeout((int) (1000 * readTimeout.getSeconds() + readTimeout.getNano() / 1000000));
        }

        request.setConfig(configBuilder.build());
        return super.execute(context, request, responseHandler);
    }

}