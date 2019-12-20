package cn.katoumegumi.java.config;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.properties.HibernateWsProperties;
import cn.katoumegumi.java.properties.JpaWsProperties;
import cn.katoumegumi.java.utils.WsPlatformTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

//import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;

@Configuration
//@AutoConfigureBefore(DataSourceConfig.class)
//@EnableTransactionManagement
//@EntityScan(basePackages = {"${ws.jpa.}"})
@Slf4j
@ConditionalOnClass({DataSource.class,EntityManagerFactory.class})
@AutoConfigureAfter({HibernateConfig.class})
@EnableConfigurationProperties({JpaWsProperties.class})
@ConditionalOnProperty(prefix = "megumi.jpa",name = "enable",havingValue = "true")
//@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",transactionManagerRef = "jpaTransactionManager",basePackages = "com.ws.java.lx.jpa")
public class JpaConfig {

    @Resource
    private DataSource dataSource;

    @Resource
    private HibernateWsProperties hibernateWsProperties;

    @Resource
    private JpaWsProperties jpaWsProperties;

    public JpaConfig(){

    }

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



    /*@Bean
    @ConditionalOnMissingBean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(ObjectProvider<PersistenceUnitManager> persistenceUnitManager, ObjectProvider<EntityManagerFactoryBuilderCustomizer> customizers,LocalSessionFactoryBean localSessionFactoryBean) {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(jpaWsProperties.getGenerateDdl());
        hibernateJpaVendorAdapter.setPrepareConnection(jpaWsProperties.getPrepareConnection());
        hibernateJpaVendorAdapter.setShowSql(jpaWsProperties.getShowSql());
        Map map = localSessionFactoryBean.getHibernateProperties();
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(hibernateJpaVendorAdapter,
                map, persistenceUnitManager.getIfAvailable());
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return builder;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder) {
        return factoryBuilder.dataSource(this.dataSource).packages(jpaWsProperties.getPackagesToScan())
                .jta(false).build();
    }*/




    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(jpaWsProperties.getGenerateDdl());
        hibernateJpaVendorAdapter.setPrepareConnection(jpaWsProperties.getPrepareConnection());
        hibernateJpaVendorAdapter.setShowSql(jpaWsProperties.getShowSql());
        hibernateJpaVendorAdapter.setDatabase(jpaWsProperties.getDatabase());
        if(WsStringUtils.isNotBlank(jpaWsProperties.getDatabasePlatform())) {
            hibernateJpaVendorAdapter.setDatabasePlatform(jpaWsProperties.getDatabasePlatform());
        }
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan(jpaWsProperties.getPackagesToScan().split(","));
        localContainerEntityManagerFactoryBean.setJpaProperties(createProperties());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean(name = "jpaTransactionManager")
    @Primary
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
        /*WsPlatformTransactionManager wsPlatformTransactionManager = new WsPlatformTransactionManager();
        wsPlatformTransactionManager.setJpaTransactionManager(jpaTransactionManager);
        return wsPlatformTransactionManager;*/
    }


    /*public JpaMetamodelMappingContext jpaMetamodelMappingContext(){
        new JpaMetamodelMappingContext
    }*/


    /*@Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder entityManagerFactoryBuilder){
        return new JpaTransactionManager(entityManagerFactoryBean(entityManagerFactoryBuilder).getObject());
    }*/

}
