package cn.katoumegumi.java.properties;

import cn.katoumegumi.java.hibernate.ExtendedMySQLDialect;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author ws
 */
@Data
@ConfigurationProperties(prefix = "megumi.hibernate",ignoreUnknownFields = true,ignoreInvalidFields = true)
public class HibernateWsProperties {
    private boolean enable = false;
    private List<String> scanPackage;
    private List<String> interfaceScan;
    private String hbm2ddl = "update";
    private Class dialect = ExtendedMySQLDialect.class;
    private Boolean showSql = false;
    private Boolean formatSql = false;
    private Boolean useSqlComments = true;
    private String defaultSchema;
    private Class factoryClass;
}
