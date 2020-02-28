package cn.katoumegumi.java.properties;

import cn.katoumegumi.java.hibernate.ExtendedMySQLDialect;
import lombok.Data;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.dialect.Dialect;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Class<? extends Dialect> dialect = ExtendedMySQLDialect.class;
    private Boolean showSql = false;
    private Boolean formatSql = false;
    private Boolean useSqlComments = true;
    private String defaultSchema;
    private Class factoryClass;
    public Class<? extends PhysicalNamingStrategy> physicalStrategy = PhysicalNamingStrategyStandardImpl.class;
    public Class<? extends ImplicitNamingStrategy> implicitStrategy = ImplicitNamingStrategyComponentPathImpl.class;
    public Map<String,String> otherConfig = new HashMap<>();

}
