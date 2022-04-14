package cn.katoumegumi.java.starter.jdbc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author ws
 */

@ConfigurationProperties(value = "megumi.datasource")
public class DataSourcePropertiesList {
    private boolean enable = false;
    private boolean seataEnable = false;
    private List<DruidDataSourceProperties> druids;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isSeataEnable() {
        return seataEnable;
    }

    public void setSeataEnable(boolean seataEnable) {
        this.seataEnable = seataEnable;
    }

    public List<DruidDataSourceProperties> getDruids() {
        return druids;
    }

    public void setDruids(List<DruidDataSourceProperties> druids) {
        this.druids = druids;
    }
}
