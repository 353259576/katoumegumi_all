package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.lang.reflect.Field;

/**
 * 列基本信息
 * @author ws
 */
public class ColumnBaseEntity {

    private Field field;

    private String tableName;

    private String tableNickName;

    private String alias;

    private String columnName;

    private String fieldName;

    private FieldColumnRelation fieldColumnRelation;

    public Field getField() {
        return field;
    }

    public ColumnBaseEntity setField(Field field) {
        this.field = field;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnBaseEntity setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getTableNickName() {
        return tableNickName;
    }

    public ColumnBaseEntity setTableNickName(String tableNickName) {
        this.tableNickName = tableNickName;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ColumnBaseEntity setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColumnBaseEntity setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ColumnBaseEntity setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public FieldColumnRelation getFieldColumnRelation() {
        return fieldColumnRelation;
    }

    public ColumnBaseEntity setFieldColumnRelation(FieldColumnRelation fieldColumnRelation) {
        this.fieldColumnRelation = fieldColumnRelation;
        return this;
    }
}
