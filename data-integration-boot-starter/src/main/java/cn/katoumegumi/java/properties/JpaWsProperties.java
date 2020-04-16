package cn.katoumegumi.java.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.vendor.Database;

/**
 * @author ws
 */
@ConfigurationProperties(prefix = "megumi.jpa", ignoreInvalidFields = true)
public class JpaWsProperties {
    private boolean enable = false;
    private String packagesToScan;
    private String mappingResources;
    private Boolean generateDdl = true;
    private Boolean prepareConnection = true;
    private Boolean showSql = false;
    private Database database = Database.MYSQL;
    private String databasePlatform;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getPackagesToScan() {
        return packagesToScan;
    }

    public void setPackagesToScan(String packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public String getMappingResources() {
        return mappingResources;
    }

    public void setMappingResources(String mappingResources) {
        this.mappingResources = mappingResources;
    }

    public Boolean getGenerateDdl() {
        return generateDdl;
    }

    public void setGenerateDdl(Boolean generateDdl) {
        this.generateDdl = generateDdl;
    }

    public Boolean getPrepareConnection() {
        return prepareConnection;
    }

    public void setPrepareConnection(Boolean prepareConnection) {
        this.prepareConnection = prepareConnection;
    }

    public Boolean getShowSql() {
        return showSql;
    }

    public void setShowSql(Boolean showSql) {
        this.showSql = showSql;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getDatabasePlatform() {
        return databasePlatform;
    }

    public void setDatabasePlatform(String databasePlatform) {
        this.databasePlatform = databasePlatform;
    }
}
