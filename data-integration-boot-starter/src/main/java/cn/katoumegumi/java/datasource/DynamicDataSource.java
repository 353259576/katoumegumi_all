package cn.katoumegumi.java.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    public DynamicDataSource(){

    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = this.getDataSource();
        log.info("使用的数据库是："+dataSource);
        return dataSource;
    }
    private String getDataSource(){
        String dataSource = DynamicDataSourceHolder.getDataSource();
        return dataSource;
    }
}
