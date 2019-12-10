package com.ws.java.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.ws.java.properties.DruidDataSourceProperties;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
public class DruidDataSourceCreateFactory {

    public DataSource initDatasource(DruidDataSourceProperties properties, boolean enable){

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
            if(enable){
                log.info("启动seata代理数据源");
                DataSourceProxy proxy = new DataSourceProxy(druidDataSource);
                return proxy;
            }
            return druidDataSource;
        }catch (Exception e){
            log.info("加载数据库失败：{}",e.getMessage());
            return null;
        }
    }




}