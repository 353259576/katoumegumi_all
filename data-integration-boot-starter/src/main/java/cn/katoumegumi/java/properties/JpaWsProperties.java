package cn.katoumegumi.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.orm.jpa.vendor.Database;

/**
 * @author ws
 */
@Data
@ConfigurationProperties(prefix = "megumi.jpa",ignoreInvalidFields = true)
public class JpaWsProperties {
    private boolean enable = false;
    private String packagesToScan;
    private String mappingResources;
    private Boolean generateDdl = true;
    private Boolean prepareConnection = true;
    private Boolean showSql = false;
    private Database database = Database.MYSQL;
    private String databasePlatform;

}
