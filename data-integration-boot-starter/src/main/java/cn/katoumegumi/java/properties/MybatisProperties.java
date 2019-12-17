package cn.katoumegumi.java.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ws
 */
@Data
//@ConfigurationProperties(prefix = "ws.mybatis",ignoreInvalidFields = true)
public class MybatisProperties {

    private boolean enable;
    private String typeAliasesPackage;
    private String mapperXmlLocations = "classpath:mybatisMapper/*.xml";
    private String basePackage;


}
