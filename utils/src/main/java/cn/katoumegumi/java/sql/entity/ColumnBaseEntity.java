package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.SQLModelUtils;

import java.lang.reflect.Field;

/**
 * 列基本信息
 *
 * @author ws
 */
public class ColumnBaseEntity {

    private final String tableName;

    private final String tableNickName;

    private final String alias;

    private final FieldColumnRelation fieldColumnRelation;


    public ColumnBaseEntity(FieldColumnRelation relation, String tableName, String tableNickName, String alias) {
        this.fieldColumnRelation = relation;
        this.tableName = tableName;
        this.tableNickName = tableNickName;
        this.alias = alias;
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private static String createColumnName(String tableNickName, String columnName) {
        return SQLModelUtils.guardKeyword(tableNickName) + '.' + SQLModelUtils.guardKeyword(columnName);
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private static String createColumnNickName(String tableNickName, String columnName) {
        return SQLModelUtils.guardKeyword(tableNickName + '.' + columnName);
    }

    public boolean isId() {
        return fieldColumnRelation.isId();
    }

    public Field getField() {
        return fieldColumnRelation.getField();
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableNickName() {
        return tableNickName;
    }

    public String getAlias() {
        return alias;
    }

    public String getColumnName() {
        return fieldColumnRelation.getColumnName();
    }

    public String getFieldName() {
        return fieldColumnRelation.getFieldName();
    }

    public FieldColumnRelation getFieldColumnRelation() {
        return fieldColumnRelation;
    }

    /**
     * 获取显示的column的值
     *
     * @return
     */
    public String getColumnValue() {
        return createColumnName(getAlias(), getColumnName()) + " " + createColumnNickName(getAlias(), getFieldName());
    }

}
