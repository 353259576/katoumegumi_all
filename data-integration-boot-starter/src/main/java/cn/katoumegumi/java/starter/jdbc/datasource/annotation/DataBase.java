package cn.katoumegumi.java.starter.jdbc.datasource.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBase {

    String dataBaseName() default "master";
}
