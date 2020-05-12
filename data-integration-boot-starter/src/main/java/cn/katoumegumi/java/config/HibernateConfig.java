package cn.katoumegumi.java.config;

import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.properties.HibernateWsProperties;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({DataSource.class, SessionFactory.class})
@AutoConfigureAfter({DataSourceConfig.class})
@EnableConfigurationProperties(value = {HibernateWsProperties.class})
public class HibernateConfig {
    /*@Resource
    private DataSource dataSource;*/

    @Resource
    private HibernateWsProperties hibernateWsProperties;


    private Properties createProperties() {
        Properties properties = new Properties();
        if (hibernateWsProperties.getHbm2ddl() != null) {
            properties.setProperty("hibernate.hbm2ddl.auto", hibernateWsProperties.getHbm2ddl());
        }
        if (hibernateWsProperties.getDialect() != null) {
            properties.setProperty("hibernate.dialect", hibernateWsProperties.getDialect().getName());
        }
        properties.setProperty("hibernate.show_sql", hibernateWsProperties.getShowSql().toString());
        properties.setProperty("hibernate.format_sql", hibernateWsProperties.getFormatSql().toString());
        properties.setProperty("hibernate.use_sql_comments", hibernateWsProperties.getUseSqlComments().toString());
        properties.setProperty("hibernate.physical_naming_strategy", hibernateWsProperties.physicalStrategy.getName());
        properties.setProperty("hibernate.implicit_naming_strategy", hibernateWsProperties.implicitStrategy.getName());
        //properties.setProperty("implicit_naming_strategy",)
        if (hibernateWsProperties.getDefaultSchema() != null) {
            properties.setProperty("hibernate.default_schema", hibernateWsProperties.getDefaultSchema());
        }
        if (hibernateWsProperties.getFactoryClass() != null) {
            properties.setProperty("transaction.factory_class", hibernateWsProperties.getFactoryClass().getName());
        }
        if (hibernateWsProperties.getOtherConfig().size() > 0) {
            hibernateWsProperties.getOtherConfig().forEach((key, value) -> {
                properties.setProperty(key, value);
            });
        }
        return properties;
    }


    //@Primary
    @Bean
    @ConditionalOnProperty(name = "megumi.hibernate.enable", havingValue = "true")
    public LocalSessionFactoryBean localSessionFactoryBean(DataSource dataSource) {
        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
        localSessionFactoryBean.setDataSource(dataSource);
        String scans[] = hibernateWsProperties.getScanPackage().toArray(new String[hibernateWsProperties.getScanPackage().size()]);
        localSessionFactoryBean.setPackagesToScan(scans);
        localSessionFactoryBean.setAnnotatedPackages(scans);
        try {
            localSessionFactoryBean.setPhysicalNamingStrategy(hibernateWsProperties.getPhysicalStrategy().getConstructor().newInstance());
            localSessionFactoryBean.setImplicitNamingStrategy(hibernateWsProperties.getImplicitStrategy().getConstructor().newInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
            ignored.printStackTrace();
        }

        localSessionFactoryBean.setHibernateProperties(createProperties());
        ;
        return localSessionFactoryBean;
    }


    /*@Primary
    @Bean(name = "hibernateSessionFactory")
    @ConditionalOnBean({LocalSessionFactoryBean.class})
    public SessionFactory sessionFactory(@Qualifier("localSessionFactoryBean")LocalSessionFactoryBean localSessionFactoryBean){
        return localSessionFactoryBean.getObject();
    }*/


    @Bean
    @ConditionalOnBean({LocalSessionFactoryBean.class})
    public HibernateTemplate hibernateTemplate(SessionFactory sessionFactory) {
        HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
        return hibernateTemplate;
    }

    @Bean
    @ConditionalOnBean({LocalSessionFactoryBean.class})
    public HibernateDao hibernateDao(HibernateTemplate hibernateTemplate) {
        HibernateDao hibernateDao = new HibernateDao();
        hibernateDao.setHibernateTemplate(hibernateTemplate);
        return hibernateDao;
    }


    @Bean(name = "hibernateTransactionManager")
    @ConditionalOnBean({LocalSessionFactoryBean.class})
    public HibernateTransactionManager hibernateTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

}
