package cn.katoumegumi.java.starter.jdbc.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class DynamicDataSource extends AbstractRoutingDataSource {


    private final static Log log = LogFactory.getLog(DynamicDataSource.class);

    public DynamicDataSource() {

    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = this.getDataSource();
        if (log.isInfoEnabled()){
            log.info(String.format("使用的数据库是：%s",dataSource));
        }

        return dataSource;
    }

    private String getDataSource() {
        return DynamicDataSourceHolder.getDataSource();
    }
}
