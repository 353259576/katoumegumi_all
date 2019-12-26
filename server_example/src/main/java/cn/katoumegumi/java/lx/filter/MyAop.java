package cn.katoumegumi.java.lx.filter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ws
 */
@Aspect
@Component
@Order(1)
public class MyAop {



    @Around(value = "@annotation(transactional)")
    public Object checkAuth(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable{
        System.out.println(transactional.transactionManager());
        System.out.println(transactional.value());
        Object o = proceedingJoinPoint.proceed();
        return o;
    }


}
