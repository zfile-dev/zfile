package im.zhaojun.zfile;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@ServletComponentScan(basePackages = "im.zhaojun.zfile.*.filter")
@ComponentScan(basePackages = "im.zhaojun.zfile.*")
public class ZfileApplication {


    public static void main(String[] args) {
        SpringApplication.run(ZfileApplication.class, args);
    }

    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriveClassName;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;


    @PostConstruct
    public void init() {
        if (StrUtil.equals(datasourceDriveClassName, "org.sqlite.JDBC")) {
            String path = datasourceUrl.replace("jdbc:sqlite:", "");
            String folderPath = FileUtil.getParent(path, 1);
            if (!FileUtil.exist(folderPath)) {
                FileUtil.mkdir(folderPath);
            }
        }
    }

}