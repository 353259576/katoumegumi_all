package com.ws.java.lx;

import com.alibaba.cloud.dubbo.annotation.DubboTransported;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;

//@SpringBootApplication(scanBasePackages = "com.ws.java",exclude = {HibernateJpaAutoConfiguration.class})
@SpringCloudApplication
@EnableJpaRepositories(transactionManagerRef = "jpaTransactionManager",basePackages = "com.ws.java.lx.jpa")
@EnableCaching
@EnableDiscoveryClient
@EnableDubboConfig
@EnableDubbo(scanBasePackages = "com.ws.java.lx")
@MapperScan(basePackages = "com.ws.java.lx.mapper")
public class Application {

    /*@Autowired
    private DataSource dataSource;*/

    public static void main(String[] args) {


        SpringApplication.run(Application.class,args);
    }


    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource){
        //org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration
        //JpaMetamodelMappingContext
        //HibernateJpaAutoConfiguration
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public GlobalTransactionScanner globalTransactionScanner(){
        GlobalTransactionScanner globalTransactionScanner = new GlobalTransactionScanner("lx-spring","my_test_tx_group");
        return globalTransactionScanner;
    }


    @LoadBalanced
    @Bean
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
