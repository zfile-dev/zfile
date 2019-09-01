package im.zhaojun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZfileApplication.class, args);
    }

}
