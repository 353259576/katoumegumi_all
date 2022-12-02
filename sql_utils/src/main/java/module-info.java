module cn.katoumegumi.java.sql.utils {
    requires java.sql;
    requires com.zaxxer.hikari;
    requires cn.katoumegumi.java.common.utils;
    requires java.persistence;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires com.baomidou.mybatis.plus.annotation;
    requires jakarta.persistence;

    exports cn.katoumegumi.java.sql;
    exports cn.katoumegumi.java.sql.common;
    exports cn.katoumegumi.java.sql.entity;
    exports cn.katoumegumi.java.sql.annotation;
    exports cn.katoumegumi.java.sql.model;
    exports cn.katoumegumi.java.sql.handle;
    exports cn.katoumegumi.java.sql.mapperFactory;
}