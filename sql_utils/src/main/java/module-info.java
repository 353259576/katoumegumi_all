module cn.katoumegumi.java.sql.utils {
    requires java.sql;
    requires com.zaxxer.hikari;
    requires cn.katoumegumi.java.common.utils;
    requires logback.core;
    requires mybatis.plus.annotation;
    requires java.persistence;
    requires org.slf4j;

    exports cn.katoumegumi.java.sql;
    exports cn.katoumegumi.java.sql.common;
    exports cn.katoumegumi.java.sql.entity;
    exports cn.katoumegumi.java.sql.annotation;
    exports cn.katoumegumi.java.sql.model;
}