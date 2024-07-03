module cn.katoumegumi.java.sql.utils {
    requires java.sql;
    requires com.zaxxer.hikari;
    requires cn.katoumegumi.java.common.utils;
    requires java.persistence;
    requires org.slf4j;
    requires com.baomidou.mybatis.plus.annotation;
    requires jakarta.persistence;

    exports cn.katoumegumi.java.sql;
    exports cn.katoumegumi.java.sql.common;
    exports cn.katoumegumi.java.sql.annotation;
    exports cn.katoumegumi.java.sql.handler;
    exports cn.katoumegumi.java.sql.resultSet;
    exports cn.katoumegumi.java.sql.resultSet.strategys;
    exports cn.katoumegumi.java.sql.page;
    exports cn.katoumegumi.java.sql.handler.model;
    exports cn.katoumegumi.java.sql.mapper.factory;
    exports cn.katoumegumi.java.sql.mapper.model;
    exports cn.katoumegumi.java.sql.model.result;
    exports cn.katoumegumi.java.sql.model.component;
    exports cn.katoumegumi.java.sql.model.condition;
    exports cn.katoumegumi.java.sql.mapper.factory.strategys;
    exports cn.katoumegumi.java.sql.model.base;
}