package cn.katoumegumi.java.config;

import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.properties.HibernateWsProperties;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ConditionalOnClass({DataSource.class,SessionFactory.class})
@AutoConfigureAfter({DataSourceConfig.class})
@ConditionalOnProperty(name = "megumi.hibernate.enable",havingValue = "true")
@EnableConfigurationProperties(value = {HibernateWsProperties.class})
public class HibernateConfig {
    @Resource
    private DataSource dataSource;

    @Resource
    private HibernateWsProperties hibernateWsProperties;


    private Properties createProperties(){
        Properties properties = new Properties();
        if(hibernateWsProperties.getHbm2ddl() != null){
            properties.setProperty("hibernate.hbm2ddl.auto",hibernateWsProperties.getHbm2ddl());
        }
        if(hibernateWsProperties.getDialect() != null){
            properties.setProperty("hibernate.dialect",hibernateWsProperties.getDialect().getName());
        }
        properties.setProperty("hibernate.show_sql",hibernateWsProperties.getShowSql().toString());
        properties.setProperty("hibernate.format_sql",hibernateWsProperties.getFormatSql().toString());
        properties.setProperty("hibernate.use_sql_comments",hibernateWsProperties.getUseSqlComments().toString());
        if(hibernateWsProperties.getDefaultSchema() != null){
            properties.setProperty("hibernate.default_schema",hibernateWsProperties.getDefaultSchema());
        }
        if(hibernateWsProperties.getFactoryClass() != null){
            properties.setProperty("transaction.factory_class",hibernateWsProperties.getFactoryClass().getName());
        }
        return properties;
    }


    @Bean
    public LocalSessionFactoryBean localSessionFactoryBean(){
        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
        localSessionFactoryBean.setDataSource(dataSource);
        /*localSessionFactoryBean.setConfigLocation(new ClassPathResource("hibernate.cfg.xml"));*/
        //localSessionFactoryBean.setPackagesToScan("com.ws.java.model.*.model.po");
        //localSessionFactoryBean.setAnnotatedPackages("com.ws.java.model.*.model.po");
        String scans[] = hibernateWsProperties.getScanPackage().toArray(new String[hibernateWsProperties.getScanPackage().size()]);
        localSessionFactoryBean.setPackagesToScan(scans);
        localSessionFactoryBean.setAnnotatedPackages(scans);
        localSessionFactoryBean.setHibernateProperties(createProperties());;
        return localSessionFactoryBean;
    }


    @Primary
    @Bean(name = "hibernateSessionFactory")
    public SessionFactory sessionFactory(@Qualifier("localSessionFactoryBean")LocalSessionFactoryBean localSessionFactoryBean){
        return localSessionFactoryBean.getObject();
    }


    @Bean
    public HibernateTemplate hibernateTemplate(@Qualifier("hibernateSessionFactory") SessionFactory sessionFactory){
        HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
        return hibernateTemplate;
    }

    @Bean
    public HibernateDao hibernateDao(HibernateTemplate hibernateTemplate){
        HibernateDao hibernateDao = new HibernateDao();
        hibernateDao.setHibernateTemplate(hibernateTemplate);
        return hibernateDao;
    }


    @Primary
    @Bean
    public HibernateTransactionManager hibernateTransactionManager(@Qualifier("hibernateSessionFactory") SessionFactory sessionFactory) {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(sessionFactory);
        return hibernateTransactionManager;
    }

}
