package cn.katoumegumi.java.config;

import cn.katoumegumi.java.datasource.WsJdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(value = DataSource.class)
@AutoConfigureAfter(value = DataSourceConfig.class)
public class JdbcConfig {


    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnClass(value = DataSource.class)
    public WsJdbcUtils wsJdbcUtils(JdbcTemplate jdbcTemplate){
        WsJdbcUtils wsJdbcUtils = new WsJdbcUtils();
        wsJdbcUtils.setJdbcTemplate(jdbcTemplate);
        return wsJdbcUtils;
    }



    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

}
