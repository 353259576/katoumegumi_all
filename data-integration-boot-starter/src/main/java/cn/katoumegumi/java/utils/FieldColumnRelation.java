package cn.katoumegumi.java.utils;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * @author ws
 */
@Data
public class FieldColumnRelation {
    private boolean id;
    private String fieldName;
    private Field field;
    private String columnName;
    private Class fieldClass;
}
