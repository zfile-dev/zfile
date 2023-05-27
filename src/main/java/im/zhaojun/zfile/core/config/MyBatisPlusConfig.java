package im.zhaojun.zfile.core.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * mybatis-plus 配置类
 *
 * @author zhaojun
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    @Resource
    private DataSource dataSource;
    
    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriveClassName;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    /**
     * 如果是 sqlite 数据库，自动创建数据库文件所在目录
     */
    @PostConstruct
    public void init() {
        if (StrUtil.equals(datasourceDriveClassName, "org.sqlite.JDBC")) {
            String path = datasourceUrl.replace("jdbc:sqlite:", "");
            String folderPath = FileUtil.getParent(path, 1);
            log.info("SQLite 数据库文件所在目录: [{}]", folderPath);
            if (!FileUtil.exist(folderPath)) {
                FileUtil.mkdir(folderPath);
                log.info("检测到 SQLite 数据库文件所在目录不存在, 已自动创建.");
            } else {
                log.info("检测到 SQLite 数据库文件所在目录已存在, 无需自动创建.");
            }
        }
    }
    
    /**
     * mybatis plus 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() throws SQLException {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        String databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        DbType dbType = DbType.getDbType(databaseProductName);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        return interceptor;
    }

}