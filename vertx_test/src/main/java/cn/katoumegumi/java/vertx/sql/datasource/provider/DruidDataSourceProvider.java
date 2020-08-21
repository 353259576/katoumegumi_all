package cn.katoumegumi.java.vertx.sql.datasource.provider;

import cn.katoumegumi.java.common.WsStringUtils;
import com.alibaba.druid.pool.DruidDataSource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.spi.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * DruidDatasourceProvider
 * @author ws
 */
public class DruidDataSourceProvider implements DataSourceProvider {
    @Override
    public int maximumPoolSize(DataSource dataSource, JsonObject config) throws SQLException {
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            return druidDataSource.getMaxActive();
        }
        return -1;
    }

    @Override
    public DataSource getDataSource(JsonObject config) throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        String url = config.getString("url");
        String rowStreamFetchSize = config.getString("row_stream_fetch_size");
        String driverClass = config.getString("driver_class");
        String user = config.getString("user");
        String password = config.getString("password");
        Integer maxPoolSize = config.getInteger("max_pool_size");
        Integer initialPoolSize = config.getInteger("initial_pool_size");
        Integer minPoolSize = config.getInteger("min_pool_size");
        Integer maxStatements = config.getInteger("max_statements");
        Integer maxStatementsPerConnection = config.getInteger("max_statements_per_connection");

        if (WsStringUtils.isNotBlank(url)) {
            druidDataSource.setUrl(url);
        }
        if (WsStringUtils.isNotBlank(user)) {
            druidDataSource.setUsername(user);
        }
        if (WsStringUtils.isNotBlank(password)) {
            druidDataSource.setPassword(password);
        }
        if (WsStringUtils.isNotBlank(driverClass)) {
            druidDataSource.setDriverClassName(driverClass);
        }
        if (maxPoolSize != null) {
            druidDataSource.setMaxActive(maxPoolSize);
        }
        if (initialPoolSize != null) {
            druidDataSource.setInitialSize(initialPoolSize);
        }
        if (maxPoolSize != null) {
            druidDataSource.setMinIdle(minPoolSize);
        }
        if (maxStatements != null) {
            druidDataSource.setMaxOpenPreparedStatements(maxStatements);
        }
        if (maxStatementsPerConnection != null) {
            druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(maxStatementsPerConnection);
        }
        druidDataSource.init();
        return druidDataSource;
    }

    @Override
    public void close(DataSource dataSource) throws SQLException {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
    }
}
