package cn.katoumegumi.java.sql.model.condition;

import cn.katoumegumi.java.sql.common.ValueType;


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
            this.types[i] = ValueType.getValueType(values[i]);
        }
    }

    public int[] getTypes() {
        return types;
    }

    public Object[] getValues() {
        return values;
    }
}
