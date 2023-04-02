package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.entity.SqlEquation;

/**
 * 单一表达式条件
 * 格式：Left Symbol Right (例如： a = b)
 */
public class SingleExpressionCondition extends AbstractExpressionCondition {

    public SingleExpressionCondition(Object left, SqlEquation.Symbol symbol, Object right) {
        super(3);
        this.values[0] = left;
        this.values[1] = symbol;
        this.values[2] = right;
        this.types[0] = getValueType(left);
        this.types[1] = getValueType(symbol);
        this.types[2] = getValueType(right);

    }


    public int getLeftType() {
        return this.types[0];
    }

    public Object getLeft() {
        return this.values[0];
    }

    public SqlEquation.Symbol getSymbol() {
        return (SqlEquation.Symbol) this.values[1];
    }

    public int getRightType() {
        return this.types[2];
    }

    public Object getRight() {
        return this.values[2];
    }
}
