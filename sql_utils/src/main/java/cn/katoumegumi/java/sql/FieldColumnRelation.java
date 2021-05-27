package cn.katoumegumi.java.sql;


import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author ws
 * 对象参数与数据库列名对应关系
 */
public class FieldColumnRelation {
    private final boolean id;
    private final String fieldName;
    private final Field field;
    private final String columnName;
    private final Class<?> fieldClass;


    public FieldColumnRelation(boolean id, String fieldName, Field field, String columnName, Class<?> fieldClass) {
        this.id = id;
        this.fieldName = fieldName;
        this.field = field;
        this.columnName = columnName;
        this.fieldClass = fieldClass;
    }

    public boolean isId() {
        return id;
    }


    public String getFieldName() {
        return fieldName;
    }


    public Field getField() {
        this.field.setAccessible(true);
        return field;
    }


    public String getColumnName() {
        return columnName;
    }


    public Class<?> getFieldClass() {
        return fieldClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldColumnRelation that = (FieldColumnRelation) o;
        return id == that.id &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(field, that.field) &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(fieldClass, that.fieldClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fieldName, field, columnName, fieldClass);
    }
}
