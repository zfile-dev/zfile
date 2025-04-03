package im.zhaojun.zfile.core.config.mybatis;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * MyBatis 数据库 ID Provider, 用于判断当前数据库类型来执行不同的 SQL 语句. <br>
 * 可在 xml 中使用 <code>&lt;if test="_databaseId = 'mysql'"&gt; </code> 来判断数据库类型. <br>
 * 也可以在外层使用，如 <code>&lt;delete id="xxx" databaseId="sqlite"&gt;</code> 来判断数据库类型.
 *
 * @author zhaojun
 */
@Component
public class MyDatabaseIdProvider implements DatabaseIdProvider {

    private static final String DATABASE_MYSQL = "MySQL";
    private static final String DATABASE_SQLITE = "SQLite";

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        String dbName = conn.getMetaData().getDatabaseProductName();
        String dbAlias = "";
        switch (dbName) {
            case DATABASE_MYSQL:
                dbAlias = "mysql";
                break;
            case DATABASE_SQLITE:
                dbAlias = "sqlite";
                break;
            default:
                break;
        }
        return dbAlias;
    }

}