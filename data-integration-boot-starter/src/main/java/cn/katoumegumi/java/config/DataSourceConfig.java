package cn.katoumegumi.java.config;

import cn.katoumegumi.java.datasource.DruidDataSourceCreateFactory;
import cn.katoumegumi.java.datasource.DynamicDataSource;
import cn.katoumegumi.java.datasource.DynamicDataSourceHolder;
import cn.katoumegumi.java.datasource.annotation.DynamicDataSourceAdvisor;
import cn.katoumegumi.java.properties.DataSourcePropertiesList;
import cn.katoumegumi.java.properties.DruidDataSourceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DataSource.class})
@ConditionalOnProperty(prefix = "megumi.datasource", value = "enable", havingValue = "true")
@EnableConfigurationProperties(value = {DataSourcePropertiesList.class})
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Resource
    private DataSourcePropertiesList dataSourcePropertiesList;

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
    public DynamicDataSource defaultDataSource() {
        DruidDataSourceCreateFactory factory = new DruidDataSourceCreateFactory();
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map map = new HashMap<>();
        List<DruidDataSourceProperties> list = dataSourcePropertiesList.getDruids();
        DruidDataSourceProperties properties = list.get(0);
        DataSource druidDataSource = factory.initDatasource(properties, dataSourcePropertiesList.isSeataEnable());
        map.put(properties.getAlias(), druidDataSource);
        dynamicDataSource.setDefaultTargetDataSource(druidDataSource);
        DynamicDataSourceHolder.defaultDatasouce = properties.getAlias();
        DynamicDataSourceHolder.dataSourceNameSet.add(properties.getAlias());
        log.info("默认数据源：{}创建成功，数据源：{}", properties.getAlias(), druidDataSource.toString());
        for (int i = 1, length = list.size(); i < length; i++) {
            properties = list.get(i);
            druidDataSource = factory.initDatasource(properties, dataSourcePropertiesList.isSeataEnable());
            if (druidDataSource != null) {
                map.put(properties.getAlias(), druidDataSource);
                DynamicDataSourceHolder.dataSourceNameSet.add(properties.getAlias());
                log.info("数据源：{}创建成功,数据源：{}", properties.getAlias(), druidDataSource.toString());
                druidDataSource = null;
            } else {
                log.info("数据源：{}创建失败", properties.getAlias());
            }
        }
        dynamicDataSource.setTargetDataSources(map);
        dynamicDataSource.afterPropertiesSet();
        return dynamicDataSource;
    }


    /*@Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }*/

    /*@Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }*/

    @Bean
    public DynamicDataSourceAdvisor dynamicDataSourceAdvisor() {
        DynamicDataSourceAdvisor dynamicDataSourceAdvisor = new DynamicDataSourceAdvisor();
        dynamicDataSourceAdvisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return dynamicDataSourceAdvisor;
    }


}
