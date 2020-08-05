package cn.katoumegumi.java.sql;

import javax.persistence.criteria.JoinType;
import java.lang.reflect.Field;

/**
 * @author ws
 */
public class FieldJoinClass {
    private boolean isArray;
    private String baseTableNickName;
    private String nickName;
    private Class joinClass;
    private JoinType joinType;
    private String joinColumn;
    private String anotherJoinColumn;
    private Field field;
    private MySearchList conditionSearchList;

    public String getBaseTableNickName() {
        return baseTableNickName;
    }

    public void setBaseTableNickName(String baseTableNickName) {
        this.baseTableNickName = baseTableNickName;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Class getJoinClass() {
        return joinClass;
    }

    public void setJoinClass(Class joinClass) {
        this.joinClass = joinClass;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
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
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public MySearchList getConditionSearchList() {
        return conditionSearchList;
    }

    public FieldJoinClass setConditionSearchList(MySearchList conditionSearchList) {
        this.conditionSearchList = conditionSearchList;
        return this;
    }
}
