package im.zhaojun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZfileApplication.class, args);
    }

}
