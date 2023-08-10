package cn.katoumegumi.java.sql.mapper.model;

import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.common.TableJoinType;

/**
 * 属性与对象列的关系
 * @author ws
 */
public class PropertyObjectColumnJoinRelation {

    /**
     * 是否是数组
     */
    private final boolean isArray;

    /**
     * 关联的实体的class
     */
    private final Class<?> joinEntityClass;

    /**
     * 属性信息
     */
    private final BeanPropertyModel beanProperty;

    /**
     * 关联实体在主实体的属性名称
     */
    private String joinEntityPropertyName;

    /**
     * 关联类型
     */
    private TableJoinType joinType;

    /**
     * 主表列名
     */
    private String mainTableColumnName;

    /**
     * 关联表列名
     */
    private String joinTableColumnName;


    public PropertyObjectColumnJoinRelation(BeanPropertyModel beanPropertyModel) {
        this.isArray = WsReflectUtils.isArrayType(beanPropertyModel.getPropertyClass());
        this.beanProperty = beanPropertyModel;
        this.joinEntityClass = this.isArray?beanPropertyModel.getGenericClass():beanPropertyModel.getPropertyClass();
    }

    public boolean isArray() {
        return isArray;
    }

    public String getJoinEntityPropertyName() {
        return joinEntityPropertyName;
    }

    public void setJoinEntityPropertyName(String joinEntityPropertyName) {
        this.joinEntityPropertyName = joinEntityPropertyName;
    }

    public Class<?> getJoinEntityClass() {
        return joinEntityClass;
    }

    public TableJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(TableJoinType joinType) {
        this.joinType = joinType;
    }

    public String getMainTableColumnName() {
        return mainTableColumnName;
    }

    public void setMainTableColumnName(String mainTableColumnName) {
        this.mainTableColumnName = mainTableColumnName;
    }

    public String getJoinTableColumnName() {
        return joinTableColumnName;
    }

    public void setJoinTableColumnName(String joinTableColumnName) {
        this.joinTableColumnName = joinTableColumnName;
    }

    public BeanPropertyModel getBeanProperty() {
        return beanProperty;
    }

}
