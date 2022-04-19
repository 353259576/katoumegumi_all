module cn.katoumegumi.java.code.generator {
    requires java.sql;
    requires cn.katoumegumi.java.common.utils;
    requires cn.katoumegumi.java.sql.utils;
    requires freemarker;
    exports cn.katoumegumi.java.code.generator;
    exports cn.katoumegumi.java.code.generator.utils;
}