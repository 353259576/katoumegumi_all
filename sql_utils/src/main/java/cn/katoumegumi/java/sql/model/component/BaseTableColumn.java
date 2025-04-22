package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.SQLModelFactory;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;

/**
 * 基本表列
 *
 * @author ws
 */
public class BaseTableColumn implements TableColumn {

    private final String tableName;

    private final String tablePath;

    private final String tableAlias;

    private String columnAlias;

    private final PropertyBaseColumnRelation propertyBaseColumnRelation;


    public BaseTableColumn(PropertyBaseColumnRelation relation, String tableName, String tablePath, String tableAlias) {
        this.propertyBaseColumnRelation = relation;
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
        return SQLModelFactory.ignoreKeyword(tableNickName) + '.' + SQLModelFactory.ignoreKeyword(columnName);
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private static String createColumnNickName(String tableNickName, String columnName) {
        return SQLModelFactory.ignoreKeyword(tableNickName + '.' + columnName);
    }

    @Override
    public boolean isId() {
        return propertyBaseColumnRelation.isId();
    }

    @Override
    public BeanPropertyModel getBeanProperty() {
        return propertyBaseColumnRelation.getBeanProperty();
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
        return propertyBaseColumnRelation.getColumnName();
    }

    @Override
    public String getBeanPropertyName() {
        return propertyBaseColumnRelation.getBeanProperty().getPropertyName();
    }

    @Override
    public PropertyBaseColumnRelation getFieldColumnRelation() {
        return propertyBaseColumnRelation;
    }

    @Override
    public String getColumnAlias() {
        if (this.columnAlias == null) {
            this.columnAlias = this.tableAlias + SqlCommonConstants.SQL_COMMON_DELIMITER + this.propertyBaseColumnRelation.getAbbreviation();
        }
        return this.columnAlias;
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
