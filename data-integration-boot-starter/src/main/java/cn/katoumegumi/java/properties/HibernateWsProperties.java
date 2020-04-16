package cn.katoumegumi.java.properties;

import cn.katoumegumi.java.hibernate.ExtendedMySQLDialect;
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
@ConfigurationProperties(prefix = "megumi.hibernate", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class HibernateWsProperties {
    public Class<? extends PhysicalNamingStrategy> physicalStrategy = PhysicalNamingStrategyStandardImpl.class;
    public Class<? extends ImplicitNamingStrategy> implicitStrategy = ImplicitNamingStrategyComponentPathImpl.class;
    public Map<String, String> otherConfig = new HashMap<>();
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<String> getScanPackage() {
        return scanPackage;
    }

    public void setScanPackage(List<String> scanPackage) {
        this.scanPackage = scanPackage;
    }

    public List<String> getInterfaceScan() {
        return interfaceScan;
    }

    public void setInterfaceScan(List<String> interfaceScan) {
        this.interfaceScan = interfaceScan;
    }

    public String getHbm2ddl() {
        return hbm2ddl;
    }

    public void setHbm2ddl(String hbm2ddl) {
        this.hbm2ddl = hbm2ddl;
    }

    public Class<? extends Dialect> getDialect() {
        return dialect;
    }

    public void setDialect(Class<? extends Dialect> dialect) {
        this.dialect = dialect;
    }

    public Boolean getShowSql() {
        return showSql;
    }

    public void setShowSql(Boolean showSql) {
        this.showSql = showSql;
    }

    public Boolean getFormatSql() {
        return formatSql;
    }

    public void setFormatSql(Boolean formatSql) {
        this.formatSql = formatSql;
    }

    public Boolean getUseSqlComments() {
        return useSqlComments;
    }

    public void setUseSqlComments(Boolean useSqlComments) {
        this.useSqlComments = useSqlComments;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public Class getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(Class factoryClass) {
        this.factoryClass = factoryClass;
    }

    public Class<? extends PhysicalNamingStrategy> getPhysicalStrategy() {
        return physicalStrategy;
    }

    public void setPhysicalStrategy(Class<? extends PhysicalNamingStrategy> physicalStrategy) {
        this.physicalStrategy = physicalStrategy;
    }

    public Class<? extends ImplicitNamingStrategy> getImplicitStrategy() {
        return implicitStrategy;
    }

    public void setImplicitStrategy(Class<? extends ImplicitNamingStrategy> implicitStrategy) {
        this.implicitStrategy = implicitStrategy;
    }

    public Map<String, String> getOtherConfig() {
        return otherConfig;
    }

    public void setOtherConfig(Map<String, String> otherConfig) {
        this.otherConfig = otherConfig;
    }
}
