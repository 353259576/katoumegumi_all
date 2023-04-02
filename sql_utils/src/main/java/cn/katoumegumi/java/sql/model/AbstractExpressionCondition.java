package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;
import cn.katoumegumi.java.sql.entity.BaseTableColumn;
import cn.katoumegumi.java.sql.entity.NullValue;
import cn.katoumegumi.java.sql.entity.SqlEquation;

import java.util.Collection;

/**
 * 抽象表达式条件
 */
public abstract class AbstractExpressionCondition implements Condition {

    protected final int[] types;

    protected final Object[] values;

    protected AbstractExpressionCondition(int length) {
        this.types = new int[length];
        this.values = new Object[length];
    }

    public AbstractExpressionCondition(Object[] values) {
        this.types = new int[values.length];
        this.values = values;
        for (int i = 0; i < values.length; i++) {
            this.types[i] = getValueType(values[i]);
        }
    }

    protected int getValueType(Object value) {
        if (value == null) {
            return ValueTypeConstants.NULL_TYPE;
        } else if (value instanceof BaseTableColumn) {
            return ValueTypeConstants.COLUMN_NAME_TYPE;
        } else if (value instanceof SqlEquation.Symbol) {
            return ValueTypeConstants.SYMBOL_TYPE;
        } else if (WsBeanUtils.isBaseType(value.getClass())) {
            return ValueTypeConstants.BASE_VALUE_TYPE;
        } else if (value instanceof Collection) {
            return ValueTypeConstants.COLLECTION_TYPE;
        } else if (WsBeanUtils.isArray(value.getClass())) {
            return ValueTypeConstants.ARRAY_TYPE;
        } else if (value instanceof SqlStringModel) {
            return ValueTypeConstants.SQL_STRING_MODEL_TYPE;
        } else if (value instanceof SelectModel) {
            return ValueTypeConstants.SELECT_MODEL_TYPE;
        } else if (value instanceof RelationCondition) {
            return ValueTypeConstants.CONDITION_RELATION_MODEL_TYPE;
        } else if (value instanceof SingleExpressionCondition) {
            return ValueTypeConstants.SINGLE_EXPRESSION_CONDITION_MODEL;
        } else if (value instanceof MultiExpressionCondition) {
            return ValueTypeConstants.MULTI_EXPRESSION_CONDITION_MODEL;
        } else if (value instanceof SqlFunctionCondition) {
            return ValueTypeConstants.SQL_FUNCTION_CONDITION;
        } else if (value instanceof NullValue) {
            return ValueTypeConstants.NULL_VALUE_MODEL;
        }
        /*else if (value instanceof SqlEquation){
            return 5;
        }*/
        throw new IllegalArgumentException("不支持的类型:" + value.getClass());
    }

    public int[] getTypes() {
        return types;
    }

    public Object[] getValues() {
        return values;
    }
}
