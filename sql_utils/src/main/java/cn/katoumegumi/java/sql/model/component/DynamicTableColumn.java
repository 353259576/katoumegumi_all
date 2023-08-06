package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelation;
import cn.katoumegumi.java.sql.model.condition.Condition;

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
    public BeanPropertyModel getBeanProperty() {
        return originalTableColumn.getBeanProperty();
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
    public String getBeanPropertyName() {
        return originalTableColumn.getBeanPropertyName();
    }

    @Override
    public FieldColumnRelation getFieldColumnRelation() {
        return originalTableColumn.getFieldColumnRelation();
    }
}
