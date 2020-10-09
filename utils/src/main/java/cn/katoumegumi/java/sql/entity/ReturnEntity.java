package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回数据集合
 * @author ws
 */
public class ReturnEntity {
    /**
     * id
     */
    private ReturnEntityId returnEntityId;

    /**
     * 对象
     */
    private Object value;

    /**
     * 对象关系数据
     */
    private FieldColumnRelationMapper fieldColumnRelationMapper;

    /**
     * id的值
     */
    private Object[] idValueList;

    /**
     * 其他值
     */
    private Object[] columnValueList;

    /**
     * 关联的对象
     */
    private ReturnEntity[] joinEntityList;

    /**
     * 父对象
     */
    private ReturnEntity parentReturnEntity;



    public FieldColumnRelationMapper getFieldColumnRelationMapper() {
        return fieldColumnRelationMapper;
    }

    public void setFieldColumnRelationMapper(FieldColumnRelationMapper fieldColumnRelationMapper) {
        this.fieldColumnRelationMapper = fieldColumnRelationMapper;
    }

    public Object[] getIdValueList() {
        return idValueList;
    }

    public void setIdValueList(Object[] idValueList) {
        this.idValueList = idValueList;
    }

    public Object[] getColumnValueList() {
        return columnValueList;
    }

    public void setColumnValueList(Object[] columnValueList) {
        this.columnValueList = columnValueList;
    }

    public ReturnEntity[] getJoinEntityList() {
        return joinEntityList;
    }

    public void setJoinEntityList(ReturnEntity[] joinEntityList) {
        this.joinEntityList = joinEntityList;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ReturnEntity getParentReturnEntity() {
        return parentReturnEntity;
    }

    public void setParentReturnEntity(ReturnEntity parentReturnEntity) {
        this.parentReturnEntity = parentReturnEntity;
    }

    public ReturnEntityId getReturnEntityId() {
        return returnEntityId;
    }

    public void setReturnEntityId(ReturnEntityId returnEntityId) {
        this.returnEntityId = returnEntityId;
    }

}
