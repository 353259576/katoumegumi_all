package com.ws.java.config;

import com.ws.java.common.WsStringUtils;
import com.ws.java.properties.JpaWsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;

//import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;

@Configuration
//@AutoConfigureBefore(DataSourceConfig.class)
//@EnableTransactionManagement
//@EntityScan(basePackages = {"${ws.jpa.}"})
@Slf4j
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter({HibernateConfig.class})
@EnableConfigurationProperties({JpaWsProperties.class})
@ConditionalOnProperty(prefix = "ws.jpa",name = "enable",havingValue = "true")
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
