package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.SQLModelFactory;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;

/**
 * 基本表列
 *
 * @author ws
 */
public class BaseTableColumn implements TableColumn {

    private final String tableName;

    private final String tablePath;

    private final String tableAlias;

    private final PropertyColumnRelation propertyColumnRelation;


    public BaseTableColumn(PropertyColumnRelation relation, String tableName, String tablePath, String tableAlias) {
        this.propertyColumnRelation = relation;
        this.tableName = tableName;
        this.tablePath = tablePath;
        this.tableAlias = tableAlias;
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private static String createColumnName(String tableNickName, String columnName) {
        return SQLModelFactory.guardKeyword(tableNickName) + '.' + SQLModelFactory.guardKeyword(columnName);
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private static String createColumnNickName(String tableNickName, String columnName) {
        return SQLModelFactory.guardKeyword(tableNickName + '.' + columnName);
    }

    @Override
    public boolean isId() {
        return propertyColumnRelation.isId();
    }

    @Override
    public BeanPropertyModel getBeanProperty() {
        return propertyColumnRelation.getBeanProperty();
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
        return propertyColumnRelation.getColumnName();
    }

    @Override
    public String getBeanPropertyName() {
        return propertyColumnRelation.getBeanProperty().getPropertyName();
    }

    @Override
    public PropertyColumnRelation getFieldColumnRelation() {
        return propertyColumnRelation;
    }

    /**
     * 获取显示的column的值
     *
     * @return
     */
    public String getColumnValue() {
        return createColumnName(getTableAlias(), getColumnName()) + " " + createColumnNickName(getTableAlias(), getBeanPropertyName());
    }
}
