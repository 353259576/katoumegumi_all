package cn.katoumegumi.java.sql;


import java.lang.reflect.Field;

/**
 * @author ws
 * 对象参数与数据库列名对应关系
 */
public class FieldColumnRelation {
    private boolean id;
    private String fieldName;
    private Field field;
    private String columnName;
    private Class<?> fieldClass;

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public void setFieldClass(Class<?> fieldClass) {
        this.fieldClass = fieldClass;
    }
}
