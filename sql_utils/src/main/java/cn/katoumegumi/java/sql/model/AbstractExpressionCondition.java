package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.common.ValueType;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.SqlEquation;

import java.util.Collection;

/**
 * 表达式条件
 *
 */
public abstract class AbstractExpressionCondition implements Condition {

    protected final int[] types;

    protected final Object[] values;

    protected AbstractExpressionCondition(int length){
        this.types = new int[length];
        this.values = new Object[length];
    }

    public AbstractExpressionCondition(Object[] values) {
        this.types = new int[values.length];
        this.values = values;
        for (int i = 0; i < values.length; i++){
            this.types[i] = getValueType(values[i]);
        }
    }

    /**
     * 0 空 1 基本类型 2 集合 3 数组 4 内联查询语句 6 列名 7 sql语句 8 关系条件 9 符号
     * @param value
     * @return
     */
    protected int getValueType(Object value){
        if(value == null){
            return ValueType.NULL_TYPE;
        }else if(value instanceof ColumnBaseEntity){
            return ValueType.COLUMN_NAME_TYPE;
        }else if (value instanceof SqlEquation.Symbol){
            return ValueType.SYMBOL_TYPE;
        }else if(WsBeanUtils.isBaseType(value.getClass())){
            return ValueType.BASE_VALUE_TYPE;
        } else if(value instanceof Collection){
            return ValueType.COLLECTION_TYPE;
        }else if(WsBeanUtils.isArray(value.getClass())){
            return ValueType.ARRAY_TYPE;
        }else if(value instanceof SqlStringModel){
            return ValueType.SQL_STRING_MODEL_TYPE;
        } else if(value instanceof SelectModel){
            return ValueType.SELECT_MODEL_TYPE;
        } else if(value instanceof ConditionRelationModel){
            return ValueType.CONDITION_RELATION_MODEL_TYPE;
        }else if (value instanceof SingleExpressionCondition){
            return ValueType.SINGLE_EXPRESSION_CONDITION_MODEL;
        }else if (value instanceof MultiExpressionCondition){
            return ValueType.MULTI_EXPRESSION_CONDITION_MODEL;
        }
        /*else if (value instanceof SqlEquation){
            return 5;
        }*/
        throw new IllegalArgumentException("不支持的类型:"+value.getClass());
    }

    public int[] getTypes() {
        return types;
    }

    public Object[] getValues() {
        return values;
    }
}
