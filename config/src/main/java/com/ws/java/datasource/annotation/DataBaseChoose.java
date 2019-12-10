package com.ws.java.datasource.annotation;


import com.ws.java.datasource.DynamicDataSourceHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(1)
@Slf4j
public class DataBaseChoose {



    @Around("@annotation(dataBase)")
    //@Around("pointCut()")
    public Object checkAuth(ProceedingJoinPoint proceedingJoinPoint, DataBase dataBase) throws Throwable{
        //获取方法名称
       /* String methodName = proceedingJoinPoint.getSignature().getName();
        log.info("方法名称为："+methodName);
        Class<?> annotation = proceedingJoinPoint.getTarget().getClass();
        Class<?> par[] = ((MethodSignature)proceedingJoinPoint.getSignature()).getParameterTypes();
        Method thisMethod = annotation.getMethod(methodName,par);
        DataBase dataBase = thisMethod.<DataBase>getAnnotation(DataBase.class);*/
        if(dataBase==null){
            //log.info("使用默认数据库");
            DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDatasouce);
        }else {
            if(DynamicDataSourceHolder.dataSourceNameSet.contains(dataBase.dataBaseName())){
                DynamicDataSourceHolder.setDataSource(dataBase.dataBaseName());
            }else {
                DynamicDataSourceHolder.setDataSource(DynamicDataSourceHolder.defaultDatasouce);
            }
        }
        Object object = proceedingJoinPoint.proceed();
        DynamicDataSourceHolder.clearDataSource();
        return object;
    }
}

