package cn.katoumegumi.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ws
 */
@Data
@ConfigurationProperties(prefix = "ws.jpa",ignoreInvalidFields = true)
public class JpaWsProperties {
    private boolean enable;
    private String packagesToScan;
    private String mappingResources;
    private Boolean generateDdl = true;
    private Boolean prepareConnection = true;
    private Boolean showSql = false;
}
