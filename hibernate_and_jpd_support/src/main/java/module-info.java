module cn.katoumegumi.java.hibernate.and.jpd.support {
    requires java.compiler;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.context.support;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.data.redis;
    requires spring.orm;
    requires spring.tx;
    requires cn.katoumegumi.java.common.utils;
    requires cn.katoumegumi.java.sql.utils;
    requires java.annotation;
    requires mybatis.plus.extension;
    requires mybatis.plus.core;
    requires org.mybatis;
    requires java.persistence;

    exports cn.katoumegumi.java.starter.jpa.config;
    exports cn.katoumegumi.java.starter.jpa.hibernate;
    exports cn.katoumegumi.java.starter.jpa.mybatis;
    exports cn.katoumegumi.java.starter.jpa.properties;
    exports cn.katoumegumi.java.starter.jpa.utils;
}