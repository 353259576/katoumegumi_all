package cn.katoumegumi.java.lx;

import com.alibaba.cloud.dubbo.annotation.DubboTransported;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableDubbo(scanBasePackages = "cn.katoumegumi.java.lx.controller")
@EnableDubboConfig
@EnableCaching
@EnableFeignClients
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class,args);
    }

    @Bean
    @LoadBalanced
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public GlobalTransactionScanner globalTransactionScanner(){
        GlobalTransactionScanner globalTransactionScanner = new GlobalTransactionScanner("lx-client","my_test_tx_group");
        return globalTransactionScanner;
    }
}
