package com.ws.java.datasource.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBase {

    public String dataBaseName() default "master";
}
