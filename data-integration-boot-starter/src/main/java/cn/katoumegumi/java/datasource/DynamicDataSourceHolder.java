package cn.katoumegumi.java.datasource;

import java.util.HashSet;
import java.util.Set;

public class DynamicDataSourceHolder {

    public static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>();
    public static String defaultDatasouce;
    public static Set<String> dataSourceNameSet = new HashSet<>();

    public static String getDataSource() {


        String datasource = THREAD_LOCAL.get();
        if (datasource == null) {
            return defaultDatasouce;
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
