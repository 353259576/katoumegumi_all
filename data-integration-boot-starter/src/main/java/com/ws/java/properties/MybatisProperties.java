package com.ws.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/4 10:20
 */
@Data
//@ConfigurationProperties(prefix = "ws.mybatis",ignoreInvalidFields = true)
public class MybatisProperties {

    private boolean enable;
    private String typeAliasesPackage;
    private String mapperXmlLocations = "classpath:mybatisMapper/*.xml";
    private String basePackage;


}
