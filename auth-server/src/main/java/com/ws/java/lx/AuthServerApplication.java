package com.ws.java.lx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author ws
 * @date Created by Administrator on 2019/11/27 11:29
 */
@SpringBootApplication
public class AuthServerApplication{


    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class,args);
    }

}
