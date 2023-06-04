package cn.katoumegumi.java.starter.jdbc.datasource.annotation;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author ws
 */
public class DynamicDataSourceAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {


    private final Advice advice;

    private final Pointcut pointcut;

    public DynamicDataSourceAdvisor() {
        pointcut = buildPointcut();
        advice = new DynamicDataSourceAnnotationInterceptor();
    }


    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    private Pointcut buildPointcut() {
        Pointcut cpc = new AnnotationMatchingPointcut(DataBase.class, true);
        Pointcut mpc = AnnotationMatchingPointcut.forMethodAnnotation(DataBase.class);
        return new ComposablePointcut(cpc).union(mpc);
    }
}
