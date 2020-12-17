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
     * 主表名称
     */
    private String baseTableNickName;

    /**
     * 连接表的名称
     */
    private String nickName;

    /**
     * 连接类型
     */
    private TableJoinType joinType;
    /**
     * 主表连接字段
     */
    private String joinColumn;
    /**
     * 连接表字段
     */
    private String anotherJoinColumn;

    /**
     * 附加查询条件
     */
    private MySearchList conditionSearchList;


    public FieldJoinClass(boolean isArray,Class<?> joinClass,Field field){
        this.isArray = isArray;
        this.joinClass = joinClass;
        this.field = field;
    }


    public String getBaseTableNickName() {
        return baseTableNickName;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getNickName() {
        return nickName;
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

    public void setBaseTableNickName(String baseTableNickName) {
        this.baseTableNickName = baseTableNickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public MySearchList getConditionSearchList() {
        return conditionSearchList;
    }

    public FieldJoinClass setConditionSearchList(MySearchList conditionSearchList) {
        this.conditionSearchList = conditionSearchList;
        return this;
    }
}
