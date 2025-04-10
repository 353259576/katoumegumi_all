package cn.katoumegumi.java.starter.jdbc.datasource;

import cn.katoumegumi.java.starter.jdbc.properties.DruidDataSourceProperties;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.sql.DataSource;

public class DruidDataSourceCreateFactory {

    private static final Log log = LogFactory.getLog(DruidDataSourceCreateFactory.class);

    public DataSource initDatasource(DruidDataSourceProperties properties, boolean enable) {

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(properties.getDriverClassName());
        druidDataSource.setUrl(properties.getUrl());
        druidDataSource.setUsername(properties.getUsername());
        druidDataSource.setPassword(properties.getPassword());
        druidDataSource.setRemoveAbandoned(properties.isRemoveAbandoned());
        druidDataSource.setConnectionProperties(properties.getConnectionProperties());
        druidDataSource.setMinIdle(properties.getMinIdle());
        druidDataSource.setValidationQuery(properties.getValidationQuery());
        druidDataSource.setInitialSize(properties.getInitialSize());
        druidDataSource.setMaxWait(properties.getMaxWait());
        druidDataSource.setPoolPreparedStatements(properties.isPoolPreparedStatements());
        druidDataSource.setTestOnBorrow(properties.isTestOnBorrow());
        druidDataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis());
        druidDataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setTestWhileIdle(properties.isTestWhileIdle());
        druidDataSource.setTestOnReturn(properties.isTestOnReturn());
        druidDataSource.setMaxActive(properties.getMaxActive());
        try {
            druidDataSource.setFilters(properties.getFilters());
            druidDataSource.init();
            log.info("加载数据源完成");
            /*if (enable) {
                log.info("启动seata代理数据源");
                return new DataSourceProxy(druidDataSource);
            }*/
            return druidDataSource;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.info(String.format("加载数据库失败：%s", e.getMessage()),e);
            }

            return null;
        }
    }


}
