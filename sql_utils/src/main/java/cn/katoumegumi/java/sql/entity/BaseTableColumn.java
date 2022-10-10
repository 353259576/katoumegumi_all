package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.model.TableColumn;

import java.lang.reflect.Field;

/**
 * 基本表列
 * @author ws
 */
public class BaseTableColumn implements TableColumn {

    private final String tableName;

    private final String tablePath;

    private final String tableAlias;

    private final FieldColumnRelation fieldColumnRelation;


    public BaseTableColumn(FieldColumnRelation relation, String tableName, String tablePath, String tableAlias) {
        this.fieldColumnRelation = relation;
        this.tableName = tableName;
        this.tablePath = tablePath;
        this.tableAlias = tableAlias;
    }

    /**
     * 创建table column name
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

    @Override
    public boolean isId() {
        return fieldColumnRelation.isId();
    }

    @Override
    public Field getField() {
        return fieldColumnRelation.getField();
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String getTablePath() {
        return tablePath;
    }

    @Override
    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumnName() {
        return fieldColumnRelation.getColumnName();
    }

    @Override
    public String getFieldName() {
        return fieldColumnRelation.getFieldName();
    }

    @Override
    public FieldColumnRelation getFieldColumnRelation() {
        return fieldColumnRelation;
    }

    /**
     * 获取显示的column的值
     * @return
     */
    public String getColumnValue() {
        return createColumnName(getTableAlias(), getColumnName()) + " " + createColumnNickName(getTableAlias(), getFieldName());
    }
}
