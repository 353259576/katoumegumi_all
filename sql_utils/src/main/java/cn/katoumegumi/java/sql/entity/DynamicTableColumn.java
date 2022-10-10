package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.model.Condition;
import cn.katoumegumi.java.sql.model.TableColumn;

import java.lang.reflect.Field;

/**
 * 动态表列
 */
public class DynamicTableColumn implements TableColumn {

    /**
     * 动态列
     */
    private final Condition dynamicColumn;

    /**
     * 原始列
     */
    private final BaseTableColumn originalTableColumn;


    public DynamicTableColumn(Condition dynamicColumn, BaseTableColumn originalTableColumn) {
        this.dynamicColumn = dynamicColumn;
        this.originalTableColumn = originalTableColumn;
    }

    public Condition getDynamicColumn() {
        return dynamicColumn;
    }

    public BaseTableColumn getOriginalTableColumn() {
        return originalTableColumn;
    }

    @Override
    public boolean isId() {
        return originalTableColumn.isId();
    }

    @Override
    public Field getField() {
        return originalTableColumn.getField();
    }

    @Override
    public String getTablePath() {
        return originalTableColumn.getTablePath();
    }

    @Override
    public String getTableAlias() {
        return originalTableColumn.getTableAlias();
    }

    @Override
    public String getFieldName() {
        return originalTableColumn.getFieldName();
    }

    @Override
    public FieldColumnRelation getFieldColumnRelation() {
        return originalTableColumn.getFieldColumnRelation();
    }
}
