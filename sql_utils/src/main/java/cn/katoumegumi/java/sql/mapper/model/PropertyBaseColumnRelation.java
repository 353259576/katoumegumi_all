package cn.katoumegumi.java.sql.mapper.model;


import cn.katoumegumi.java.common.model.BeanPropertyModel;

/**
 * 属性与基本列关系
 * @author ws
 */
public class PropertyBaseColumnRelation {
    /**
     * 是否为id
     */
    private final boolean id;

    /**
     * 实体字段名称
     */
    private final BeanPropertyModel beanProperty;

    /**
     * 对应数据库列名
     */
    private final String columnName;



    public PropertyBaseColumnRelation(boolean id, String columnName, BeanPropertyModel beanPropertyModel) {
        this.id = id;
        this.columnName = columnName;
        this.beanProperty = beanPropertyModel;

    }

    public boolean isId() {
        return id;
    }


    public String getColumnName() {
        return columnName;
    }

    public BeanPropertyModel getBeanProperty() {
        return beanProperty;
    }
}
