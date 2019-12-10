package com.ws.java.lx.controller;

import com.ws.java.lx.service.IndexService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
//@RefreshScope
public class IndexController {
    @Autowired
    private RestTemplate restTemplate;

    @Reference(protocol = "rest",version = "1.0.0",check = false)
    //@Autowired
    private IndexService indexFeign;

    /*@Value(value = "${localcachevalue:0}")
    private Integer value;*/


    @Value("${jasypt.encryptor.password:0}")
    private String value;

    @RequestMapping(value = "index")
    @GlobalTransactional(name = "index")
    public String index(){
        System.out.println(value);
        //ResponseEntity<String> strE = restTemplate.getForEntity("http://lxSpring/index",String.class);
        String str = indexFeign.index();
        return str;
    }
}
