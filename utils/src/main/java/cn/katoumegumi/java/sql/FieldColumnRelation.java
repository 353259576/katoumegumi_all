package cn.katoumegumi.java.sql;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * @author ws
 * 对象参数与数据库列名对应关系
 */
@Data
public class FieldColumnRelation {
    private boolean id;
    private String fieldName;
    private Field field;
    private String columnName;
    private Class<?> fieldClass;
}
