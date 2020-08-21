package cn.katoumegumi.java.lx.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author ws
 */
@Configuration
public class MybatisConfig {


    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        return paginationInterceptor;
    }


    @Bean
    public SQLClient sqlClient(DataSource dataSource) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("provider_class", "cn.katoumegumi.java.vertx.sql.datasource.provider.DruidDataSourceProvider");
        jsonObject.put("url", "jdbc:mysql://127.0.0.1:3306/wslx?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true");
        jsonObject.put("user", "root");
        jsonObject.put("password", "199645");
        jsonObject.put("driver_class", "com.mysql.cj.jdbc.Driver");
        Vertx vertx = Vertx.vertx();
        SQLClient client = JDBCClient.create(vertx, dataSource);
        return client;
    }


}
