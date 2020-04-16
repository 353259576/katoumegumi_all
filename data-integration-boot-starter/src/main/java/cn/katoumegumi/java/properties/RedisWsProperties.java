package cn.katoumegumi.java.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author ws
 */

@ConfigurationProperties(value = "megumi.redis", ignoreInvalidFields = true)
public class RedisWsProperties {

    private String enable;
    private String host = "localhost";
    private Integer port = 6379;
    private String password;
    private Map<String, Integer> caches;
    private Integer defultCacheTime = -1;


    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Integer> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, Integer> caches) {
        this.caches = caches;
    }

    public Integer getDefultCacheTime() {
        return defultCacheTime;
    }

    public void setDefultCacheTime(Integer defultCacheTime) {
        this.defultCacheTime = defultCacheTime;
    }
}
