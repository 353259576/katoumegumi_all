package cn.katoumegumi.java.config;

import cn.katoumegumi.java.datasource.WsJdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = DataSource.class)
//@AutoConfigureAfter(value = DataSourceConfig.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class JdbcConfig {

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnClass(value = JdbcTemplate.class)
    public WsJdbcUtils wsJdbcUtils(JdbcTemplate jdbcTemplate) {
        WsJdbcUtils wsJdbcUtils = new WsJdbcUtils();
        wsJdbcUtils.setJdbcTemplate(jdbcTemplate);
        return wsJdbcUtils;
    }


    @Bean
    @ConditionalOnBean(value = DataSource.class)
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

}
