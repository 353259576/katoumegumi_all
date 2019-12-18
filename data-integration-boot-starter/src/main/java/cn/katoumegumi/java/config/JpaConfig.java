package cn.katoumegumi.java.config;

import cn.katoumegumi.java.properties.JpaWsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

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
    private JpaWsProperties jpaWsProperties;

    public JpaConfig(){

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




    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(LocalSessionFactoryBean localSessionFactoryBean){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(jpaWsProperties.getGenerateDdl());
        hibernateJpaVendorAdapter.setPrepareConnection(jpaWsProperties.getPrepareConnection());
        hibernateJpaVendorAdapter.setShowSql(jpaWsProperties.getShowSql());
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean.setPackagesToScan(jpaWsProperties.getPackagesToScan().split(","));
        localContainerEntityManagerFactoryBean.setJpaProperties(localSessionFactoryBean.getHibernateProperties());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean(name = "jpaTransactionManager")
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }


    /*public JpaMetamodelMappingContext jpaMetamodelMappingContext(){
        new JpaMetamodelMappingContext
    }*/


    /*@Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder entityManagerFactoryBuilder){
        return new JpaTransactionManager(entityManagerFactoryBean(entityManagerFactoryBuilder).getObject());
    }*/

}
