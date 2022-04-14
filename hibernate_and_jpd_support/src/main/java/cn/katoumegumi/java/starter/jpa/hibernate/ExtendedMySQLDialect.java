package cn.katoumegumi.java.starter.jpa.hibernate;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class ExtendedMySQLDialect extends MySQL8Dialect {
    public ExtendedMySQLDialect() {
        super();
        registerFunction("date_add", new SQLFunctionTemplate(StandardBasicTypes.STRING,
                "date_add(?1, INTERVAL ?2 DAY)"));
    }
}
