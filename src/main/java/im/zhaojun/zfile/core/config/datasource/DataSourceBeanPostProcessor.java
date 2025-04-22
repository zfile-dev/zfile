package im.zhaojun.zfile.core.config.datasource;


import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariDataSource;
import im.zhaojun.zfile.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 在 Spring 容器初始化时, 对数据源进行处理.
 * <br/>
 * 1. 针对 DataSource 进行处理，仅针对 sqlite：
 * <ul>
 *     <li>提前创建 sqlite 数据文件所在目录.</li>
 *     <li>检测到版本更新时(pom.xml -> project.version)自动备份原数据库.</li>
 * </ul>
 * <br/>
 * 2. 针对 Flyway 进行处理，根据数据库类型, 配置不同的 Flyway Migration Location：
 * <ul>
 *     <li>SQLite 数据库使用 migration-sqlite 目录.</li>
 *     <li>MySQL 数据库使用 migration-mysql 目录.</li>
 * </ul>
 *
 * @author zhaojun
 */
@Slf4j
@Component
public class DataSourceBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    public static final String ZFILE_VERSION_PROPERTIES = "zfile.db.version";

    public static final String DRIVE_CLASS_NAME_PROPERTIES = "spring.datasource.driver-class-name";

    public static final String DATA_SOURCE_BEAN_NAME = "dataSource";

    public static final String SQLITE_DRIVE_CLASS_NAME = "org.sqlite.JDBC";

    public static final String MYSQL_DRIVE_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 如果更改了数据源类型这里要修改
        if (bean instanceof HikariDataSource dataSource && DATA_SOURCE_BEAN_NAME.equals(beanName)) {
            processSqliteDataSource(dataSource);
        } else if (bean instanceof FlywayProperties flywayProperties) {
            processFlywayLocations(flywayProperties);
        }
        return bean;
    }

    /**
     * 如果是 sqlite 数据库, 提前创建数据库文件所在目录. <br/>
     *
     * 如果检测到版本更新, 自动备份原数据库文件.
     *
     * @param   dataSource
     *          数据源
     */
    private void processSqliteDataSource(HikariDataSource dataSource) {
        String driverClassName = dataSource.getDriverClassName();
        String jdbcUrl = dataSource.getJdbcUrl();
        if (StringUtils.equals(driverClassName, SQLITE_DRIVE_CLASS_NAME)) {
            String path = jdbcUrl.replace("jdbc:sqlite:", "");
            String folderPath = FileUtil.getAbsolutePath(new File(path).getParentFile());
            log.info("SQLite 数据库文件所在目录: [{}]", folderPath);
            File file = new File(folderPath);
            if (!file.exists()) {
                log.info("检测到 SQLite 数据库文件所在目录不存在, 已自动创建.");
                if (!file.mkdirs()) {
                    log.error("SQLite 数据库文件创建失败.");
                }
            } else {
                log.info("检测到 SQLite 数据库文件所在目录已存在, 无需自动创建.");

                // 更新版本时, 先自动备份数据库文件
                String version = SpringUtil.getProperty(ZFILE_VERSION_PROPERTIES);
                if (StringUtils.isNotEmpty(version)) {
                    String backupPath = folderPath + "/zfile-update-" + version + "-backup.db";
                    if (!FileUtil.exist(path)) {
                        log.error("检测到 SQLite 数据库文件不存在, 一般为初始化状态，无需备份.");
                        return;
                    }
                    if (FileUtil.exist(backupPath)) {
                        log.info("检测到 SQLite 数据库备份文件 [{}] 已存在, 无需再次备份.", backupPath);
                    } else {
                        FileUtil.copy(path, backupPath, false);
                        log.info("自动备份 SQLite 数据库文件到: [{}]", backupPath);
                    }
                }
            }
        }
    }

    /**
     * 根据使用的不同数据库, 配置使用不同的 migration location
     *
     * @param   flywayProperties
     *          flyway 配置项
     */
    private void processFlywayLocations(FlywayProperties flywayProperties) {
        String driveClassName = SpringUtil.getProperty(DRIVE_CLASS_NAME_PROPERTIES);
        if (SQLITE_DRIVE_CLASS_NAME.equals(driveClassName)) {
            flywayProperties.setLocations(List.of("classpath:db/migration-sqlite"));
        } else if (MYSQL_DRIVE_CLASS_NAME.equals(driveClassName)) {
            flywayProperties.setLocations(List.of("classpath:db/migration-mysql"));
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}