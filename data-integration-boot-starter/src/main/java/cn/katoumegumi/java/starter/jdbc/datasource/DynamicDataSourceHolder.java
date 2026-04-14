package cn.katoumegumi.java.starter.jdbc.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataSourceHolder {

    public static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();
    public static String defaultDataSource;
    public static final Map<String,Boolean> dataSourceNameSet = new ConcurrentHashMap<>();

    public static String getDataSource() {


        String datasource = THREAD_LOCAL.get();
        if (datasource == null) {
            return defaultDataSource;
        }
        return datasource;
    }

    public static void setDataSource(String dataSource) {
        THREAD_LOCAL.set(dataSource);
    }

    public static void clearDataSource() {
        THREAD_LOCAL.remove();
    }


}
