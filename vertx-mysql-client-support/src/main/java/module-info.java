module vertx.mysql.client.support {
    requires java.sql;
    requires sql.utils;
    requires io.vertx.core;
    requires io.vertx.client.jdbc;
    requires io.vertx.client.sql;

    exports cn.katoumegumi.java.vertx;
}