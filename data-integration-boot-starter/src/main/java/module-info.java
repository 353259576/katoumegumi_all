module cn.katoumegumi.java.data.integration.boot.starter {
    requires java.naming;
    requires java.management;
    requires java.sql;
    requires druid;
    //requires seata.all;
    requires spring.aop;
    requires spring.beans;
    //requires spring.boot;
    //requires spring.boot.autoconfigure;
    requires spring.context;
    //requires spring.data.commons;
    requires spring.jdbc;
    requires spring.tx;
    requires cn.katoumegumi.java.common.utils;
    requires cn.katoumegumi.java.sql.utils;
    requires org.slf4j;
    requires spring.core;
    requires com.baomidou.mybatis.plus.core;
    requires com.baomidou.mybatis.plus.extension;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    exports cn.katoumegumi.java.starter.jdbc.utils;
    exports cn.katoumegumi.java.starter.jdbc.config;
    exports cn.katoumegumi.java.starter.jdbc.datasource;
    exports cn.katoumegumi.java.starter.jdbc.properties;
    exports cn.katoumegumi.java.starter.jdbc.datasource.annotation;
}