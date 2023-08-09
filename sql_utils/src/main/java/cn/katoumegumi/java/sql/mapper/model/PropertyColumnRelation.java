package cn.katoumegumi.java.sql.mapper.model;


import cn.katoumegumi.java.common.model.BeanPropertyModel;

/**
 * @author ws
 * 对象参数与数据库列名对应关系
 */
public class PropertyColumnRelation {
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



    public PropertyColumnRelation(boolean id, String columnName, BeanPropertyModel beanPropertyModel) {
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
