package cn.katoumegumi.java.starter.jdbc.datasource.annotation;

import cn.katoumegumi.java.starter.jdbc.datasource.DynamicDataSourceHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author ws
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        DataBase dataBase = methodInvocation.getMethod().getAnnotation(DataBase.class);
        if (dataBase == null) {
            //log.info("使用默认数据库");
            DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDatasouce);
        } else {
            if (DynamicDataSourceHolder.dataSourceNameSet.contains(dataBase.dataBaseName())) {
                DynamicDataSourceHolder.setDataSource(dataBase.dataBaseName());
            } else {
                DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDatasouce);
            }
        }
        Object object = methodInvocation.proceed();
        DynamicDataSourceHolder.clearDataSource();
        return object;
    }
}
