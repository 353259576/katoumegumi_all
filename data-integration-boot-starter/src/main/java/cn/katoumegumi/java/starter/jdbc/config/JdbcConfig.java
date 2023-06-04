package cn.katoumegumi.java.starter.jdbc.config;

import cn.katoumegumi.java.starter.jdbc.datasource.WsJdbcUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@AutoConfiguration
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
