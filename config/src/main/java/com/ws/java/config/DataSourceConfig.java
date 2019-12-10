package com.ws.java.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.ws.java.datasource.DruidDataSourceCreateFactory;
import com.ws.java.datasource.DynamicDataSource;
import com.ws.java.datasource.DynamicDataSourceHolder;
import com.ws.java.properties.DruidDataSourceProperties;
import com.ws.java.properties.DruidDataSourcePropertiesList;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Configuration
//@AutoConfigureAfter(DruidDBConfig.class)
@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})
//@AutoConfigureBefore({DataSourceAutoConfiguration.class})
//@ConditionalOnExpression("'${ws.datasource.enable}'.equals('true')")
@ConditionalOnProperty(prefix = "ws.datasource",value = "enable",havingValue = "true")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(value = {DruidDataSourcePropertiesList.class})
public class DataSourceConfig {

    @Resource
    private DruidDataSourcePropertiesList druidDataSourcePropertiesList;

    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DataSource dataSource = defaultDataSource();
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy();
        proxy.setTargetDataSource(dataSource);
        proxy.setReobtainTransactionalConnections(true);
        return proxy;
    }


    //@Bean(name = "dynamicDataSource")
    public DynamicDataSource defaultDataSource(){
        DruidDataSourceCreateFactory factory = new DruidDataSourceCreateFactory();
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map map = new HashMap<>();
        List<DruidDataSourceProperties> list = druidDataSourcePropertiesList.getDruids();
        DruidDataSourceProperties properties = list.get(0);
        DataSource druidDataSource = factory.initDatasource(properties,druidDataSourcePropertiesList.isSeataEnable());
        map.put(properties.getAlias(),druidDataSource);
        dynamicDataSource.setDefaultTargetDataSource(druidDataSource);
        DynamicDataSourceHolder.defaultDatasouce = properties.getAlias();
        DynamicDataSourceHolder.dataSourceNameSet.add(properties.getAlias());
        log.info("默认数据源：{}创建成功，数据源：{}",properties.getAlias(),druidDataSource.toString());
        for(int i = 1,length = list.size(); i < length; i++ ) {
            properties = list.get(i);
            druidDataSource = factory.initDatasource(properties,druidDataSourcePropertiesList.isSeataEnable());
            if (druidDataSource != null) {
                map.put(properties.getAlias(), druidDataSource);
                DynamicDataSourceHolder.dataSourceNameSet.add(properties.getAlias());
                log.info("数据源：{}创建成功,数据源：{}",properties.getAlias(),druidDataSource.toString());
                druidDataSource = null;
            }else {
                log.info("数据源：{}创建失败",properties.getAlias());
            }
        }
        dynamicDataSource.setTargetDataSources(map);
        dynamicDataSource.afterPropertiesSet();
        return dynamicDataSource;
    }


    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource){
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }



}
