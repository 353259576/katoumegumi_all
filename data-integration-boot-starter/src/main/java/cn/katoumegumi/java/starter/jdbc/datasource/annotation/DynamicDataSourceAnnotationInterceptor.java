package cn.katoumegumi.java.starter.jdbc.datasource.annotation;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cn.katoumegumi.java.starter.jdbc.datasource.DynamicDataSourceHolder;

/**
 * @author ws
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        DataBase dataBase = methodInvocation.getMethod().getAnnotation(DataBase.class);
        try {
            if (dataBase == null) {
                // log.info("使用默认数据库");
                DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDataSource);
            } else {
                if (DynamicDataSourceHolder.dataSourceNameSet.containsKey(dataBase.dataBaseName())) {
                    DynamicDataSourceHolder.setDataSource(dataBase.dataBaseName());
                } else {
                    DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDataSource);
                }
            }
            Object object = methodInvocation.proceed();
            return object;
        } finally {
            DynamicDataSourceHolder.clearDataSource();
        }
    }
}
