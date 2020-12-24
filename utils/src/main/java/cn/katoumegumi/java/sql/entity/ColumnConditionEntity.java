package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.SQLModelUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 列条件基本信息
 * @author ws
 */
public class ColumnConditionEntity {

    private final ColumnBaseEntity column;

    private final List<Object> valueList;

    private final String condition;

    public ColumnConditionEntity(ColumnBaseEntity column,List<Object> valueList,String condition){
        this.column = column;
        this.valueList = valueList;
        this.condition = condition;
    }

    public ColumnBaseEntity getColumn() {
        return column;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public String getCondition() {
        return condition;
    }
}
