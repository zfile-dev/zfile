package im.zhaojun.zfile.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * mybatis-plus 配置类
 *
 * @author zhaojun
 */
@Configuration
public class MyBatisPlusConfig {

    @Resource
    private DataSource dataSource;


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