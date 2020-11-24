package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

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
    private final FieldColumnRelationMapper fieldColumnRelationMapper;

    /**
     * id的值
     */
    private final Object[] idValueList;

    /**
     * 其他值
     */
    private final Object[] columnValueList;

    /**
     * 关联的对象
     */
    private final ReturnEntity[] joinEntityList;

    /**
     * 父对象
     */
    private ReturnEntity parentReturnEntity;


    public ReturnEntity(FieldColumnRelationMapper mapper){
        fieldColumnRelationMapper = mapper;
        idValueList = new Object[mapper.getIds().size()];
        columnValueList = new Object[mapper.getFieldColumnRelations().size()];
        joinEntityList = new ReturnEntity[mapper.getFieldJoinClasses().size()];
    }



    public FieldColumnRelationMapper getFieldColumnRelationMapper() {
        return fieldColumnRelationMapper;
    }

    public Object[] getIdValueList() {
        return idValueList;
    }

    public Object[] getColumnValueList() {
        return columnValueList;
    }

    public ReturnEntity[] getJoinEntityList() {
        return joinEntityList;
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
