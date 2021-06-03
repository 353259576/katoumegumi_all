package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.common.TableJoinType;

import java.lang.reflect.Field;

/**
 * @author ws
 */
public class FieldJoinClass {

    private final boolean isArray;

    private final Class<?> joinClass;

    private final Field field;

    /**
     * 主表实体名称
     */
    private String baseTableNickName;

    /**
     * 关联表实体名称
     */
    private String nickName;

    /**
     * 关联类型
     */
    private TableJoinType joinType;

    /**
     * 主表表中字段名称
     */
    private String joinColumn;

    /**
     * 关联表表中字段名称
     */
    private String anotherJoinColumn;

    /**
     * 附加条件
     */
    private MySearchList conditionSearchList;


    public FieldJoinClass(boolean isArray, Class<?> joinClass, Field field) {
        this.isArray = isArray;
        this.joinClass = joinClass;
        this.field = field;
    }


    public String getBaseTableNickName() {
        return baseTableNickName;
    }

    public void setBaseTableNickName(String baseTableNickName) {
        this.baseTableNickName = baseTableNickName;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Class<?> getJoinClass() {
        return joinClass;
    }

    public TableJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(TableJoinType joinType) {
        this.joinType = joinType;
    }

    public String getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(String joinColumn) {
        this.joinColumn = joinColumn;
    }

    public String getAnotherJoinColumn() {
        return anotherJoinColumn;
    }

    public void setAnotherJoinColumn(String anotherJoinColumn) {
        this.anotherJoinColumn = anotherJoinColumn;
    }

    public Field getField() {
        field.setAccessible(true);
        return field;
    }

    public MySearchList getConditionSearchList() {
        return conditionSearchList;
    }

    public FieldJoinClass setConditionSearchList(MySearchList conditionSearchList) {
        this.conditionSearchList = conditionSearchList;
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
