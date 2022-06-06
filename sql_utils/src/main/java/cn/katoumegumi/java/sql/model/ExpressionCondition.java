package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.SqlEquation;

import java.util.Collection;

/**
 * 表达式条件
 *
 */
public class ExpressionCondition implements Condition {

    /**
     * o 空 1 基本类型 2 集合类型 3 数组 4 子查询 5 SqlEquation 6 ColumnBaseEntity 7 sql语句  8 ConditionRelationModel
     */
    private final int leftType;
    private final Object left;

    private final SqlEquation.Symbol symbol;

    private final int rightType;
    private final Object right;

    public ExpressionCondition(Object left, SqlEquation.Symbol symbol, Object right) {
        this.left = left;
        this.symbol = symbol;
        this.right = right;
        this.leftType = getValueType(left);
        this.rightType = getValueType(right);
    }

    private int getValueType(Object value){
        if(value == null){
            return 0;
        }else if(value instanceof ColumnBaseEntity){
            return 6;
        }else if(WsBeanUtils.isBaseType(value.getClass())){
            return 1;
        } else if(value instanceof Collection){
            return 2;
        }else if(WsBeanUtils.isArray(value.getClass())){
            return 3;
        }else if(value instanceof SqlStringModel){
            return 7;
        } else if(value instanceof SelectModel){
            return 4;
        } else if(value instanceof ConditionRelationModel){
            return 8;
        }else if (value instanceof SqlEquation){
            return 5;
        }
        throw new IllegalArgumentException("不支持的类型:"+value.getClass());
    }

    public int getLeftType() {
        return leftType;
    }

    public Object getLeft() {
        return left;
    }

    public SqlEquation.Symbol getSymbol() {
        return symbol;
    }

    public int getRightType() {
        return rightType;
    }

    public Object getRight() {
        return right;
    }
}
