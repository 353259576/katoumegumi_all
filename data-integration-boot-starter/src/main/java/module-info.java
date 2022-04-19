module data.integration.boot.starter {
    requires java.compiler;
    requires java.sql;
    requires druid;
    requires seata.all;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.data.commons;
    requires spring.jdbc;
    requires spring.tx;
    requires common.utils;
    requires sql.utils;
    requires org.slf4j;
    requires java.annotation;
    requires spring.core;
    requires mybatis.plus.core;
    requires mybatis.plus.extension;
    exports cn.katoumegumi.java.starter.jdbc.utils;
    exports cn.katoumegumi.java.starter.jdbc.config;
    exports cn.katoumegumi.java.starter.jdbc.datasource;
    exports cn.katoumegumi.java.starter.jdbc.properties;
}