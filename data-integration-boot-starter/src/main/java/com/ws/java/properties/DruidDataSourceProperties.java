package com.ws.java.properties;

import lombok.Data;

/**
 * @author ws
 */
@Data
public class DruidDataSourceProperties {
    private String alias;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String url;
    private String username;
    private String password;
    private boolean removeAbandoned = true;
    private String connectionProperties = "druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000";
    private int minIdle = 1;
    private String validationQuery = "SELECT 1 FROM DUAL";
    private int initialSize = 5;
    private long maxWait = 600000L;
    private boolean poolPreparedStatements = false;
    private String filters = "stat,wall";
    private boolean testOnBorrow = false;
    private boolean testWhileIdle = true;
    private long minEvictableIdleTimeMillis = 300000L;
    private long timeBetweenEvictionRunsMillis = 60000L;
    private boolean testOnReturn = false;
    private int maxActive = 50;
}
