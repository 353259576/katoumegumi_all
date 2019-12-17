package cn.katoumegumi.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author ws
 */
@Data
@ConfigurationProperties(value = "ws.datasource")
public class DruidDataSourcePropertiesList {
    private boolean enable = false;
    private boolean seataEnable = false;
    private List<DruidDataSourceProperties> druids;

}
