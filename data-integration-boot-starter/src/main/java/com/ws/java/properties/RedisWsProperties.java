package com.ws.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author ws
 */
@Data
@ConfigurationProperties(value = "ws.redis",ignoreInvalidFields = true)
public class RedisWsProperties {

    private String enable;
    private String host = "localhost";
    private Integer port = 6379;
    private String password;
    private Map<String,Integer> caches;
    private Integer defultCacheTime = -1;

}
