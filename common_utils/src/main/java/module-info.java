module cn.katoumegumi.java.common.utils {
    requires java.desktop;
    requires java.sql;
    requires java.xml;
    requires jdk.unsupported;

    exports cn.katoumegumi.java.common;
    exports cn.katoumegumi.java.common.model;
    exports cn.katoumegumi.java.common.convert;
}