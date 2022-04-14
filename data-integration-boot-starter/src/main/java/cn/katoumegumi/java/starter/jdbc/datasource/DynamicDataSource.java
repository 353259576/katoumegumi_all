package cn.katoumegumi.java.starter.jdbc.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


public class DynamicDataSource extends AbstractRoutingDataSource {


    private final static Logger log = LoggerFactory.getLogger(DynamicDataSource.class);

    public DynamicDataSource() {

    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = this.getDataSource();
        log.info("使用的数据库是：" + dataSource);
        return dataSource;
    }

    private String getDataSource() {
        String dataSource = DynamicDataSourceHolder.getDataSource();
        return dataSource;
    }
}
