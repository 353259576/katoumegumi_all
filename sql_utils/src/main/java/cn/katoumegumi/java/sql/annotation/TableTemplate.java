package cn.katoumegumi.java.sql.annotation;

import java.lang.annotation.*;

/**
 * 设定使用的表模板
 *
 * @author 星梦苍天
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableTemplate {

    Class<?> value();
}
