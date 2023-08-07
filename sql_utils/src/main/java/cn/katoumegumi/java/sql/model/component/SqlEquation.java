package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.common.NullValue;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 公式
 *
 * @author 星梦苍天
 */
public class SqlEquation {

    /**
     * 1 columnName 2 符号 3 值 4 MySearchList
     */
    private final List<Integer> typeList = new ArrayList<>();

    private final List<Object> valueList = new ArrayList<>();

    public SqlEquation column(String columnName) {
        typeList.add(ValueTypeConstants.COLUMN_NAME_TYPE);
        valueList.add(columnName);
        return this;
    }

    public SqlEquation column(SqlEquation equation) {
        typeList.add(ValueTypeConstants.SQL_EQUATION_MODEL);
        valueList.add(equation);
        return this;
    }

    public SqlEquation column(SqlFunction function) {
        typeList.add(ValueTypeConstants.SQL_FUNCTION_MODEL);
        valueList.add(function);
        return this;
    }

    public SqlEquation column(String tableName, String columnName) {
        if (WsStringUtils.isBlank(tableName)) {
            return column(columnName);
        }
        typeList.add(ValueTypeConstants.COLUMN_NAME_TYPE);
        valueList.add(tableName + "." + columnName);
        return this;
    }

    public <T> SqlEquation column(SFunction<T, ?> columnFunction) {
        typeList.add(ValueTypeConstants.COLUMN_NAME_TYPE);
        valueList.add(WsReflectUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation column(String tableName, SFunction<T, ?> columnFunction) {
        if (WsStringUtils.isBlank(tableName)) {
            return column(columnFunction);
        }
        typeList.add(ValueTypeConstants.COLUMN_NAME_TYPE);
        valueList.add(tableName + "." + WsReflectUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation sql(MySearchList mySearchList) {
        typeList.add(ValueTypeConstants.SEARCH_LIST_TYPE);
        valueList.add(mySearchList);
        return this;
    }

    public SqlEquation value(Object o) {
        int valueType;
        if (o == null) {
            valueType = ValueTypeConstants.NULL_VALUE_MODEL;
            o = SqlCommonConstants.NULL_VALUE;
        } else if (WsBeanUtils.isBaseType(o.getClass())) {
            valueType = ValueTypeConstants.BASE_VALUE_TYPE;
        } else if (o instanceof Collection) {
            valueType = ValueTypeConstants.COLLECTION_TYPE;
        } else if (WsBeanUtils.isArray(o.getClass())) {
            valueType = ValueTypeConstants.ARRAY_TYPE;
        } else if (o instanceof NullValue) {
            valueType = ValueTypeConstants.NULL_VALUE_MODEL;
        } else {
            throw new IllegalArgumentException("不支持的值");
        }
        typeList.add(valueType);
        valueList.add(o);
        return this;
    }

    public SqlEquation symbol(Object o) {
        typeList.add(ValueTypeConstants.SYMBOL_TYPE);
        valueList.add(o);
        return this;
    }

    public SqlEquation add() {
        return symbol(Symbol.ADD);
    }

    public SqlEquation subtract() {
        return symbol(Symbol.SUBTRACT);
    }

    public SqlEquation multiply() {
        return symbol(Symbol.MULTIPLY);
    }

    public SqlEquation equal() {
        return symbol(Symbol.EQUAL);
    }

    public SqlEquation notEqual() {
        return symbol(Symbol.NOT_EQUAL);
    }

    public SqlEquation in() {
        return symbol(Symbol.IN);
    }

    public SqlEquation notIn() {
        return symbol(Symbol.NOT_IN);
    }

    public SqlEquation isNull() {
        return symbol(Symbol.NULL);
    }

    public SqlEquation isNotNull() {
        return symbol(Symbol.NOT_NULL);
    }

    public SqlEquation exists() {
        return symbol(Symbol.EXISTS);
    }

    public SqlEquation notExists() {
        return symbol(Symbol.NOT_EXISTS);
    }

    public SqlEquation divide() {
        return symbol(Symbol.DIVIDE);
    }

    public SqlEquation gt() {
        return symbol(Symbol.GT);
    }

    public SqlEquation gte() {
        return symbol(Symbol.GTE);
    }

    public SqlEquation lt() {
        return symbol(Symbol.LT);
    }

    public SqlEquation lte() {
        return symbol(Symbol.LTE);
    }

    public SqlEquation and() {
        return symbol(Symbol.AND);
    }

    public SqlEquation or() {
        return symbol(Symbol.OR);
    }

    public SqlEquation xor() {
        return symbol(Symbol.XOR);
    }

    public SqlEquation not() {
        return symbol(Symbol.NOT);
    }

    public List<Integer> getTypeList() {
        return typeList;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public enum Symbol {
        /**
         * 符号
         */
        ADD(SqlCommonConstants.ADD),
        SUBTRACT(SqlCommonConstants.SUBTRACT),
        MULTIPLY(SqlCommonConstants.MULTIPLY),
        DIVIDE(SqlCommonConstants.DIVIDE),
        EQUAL(SqlCommonConstants.EQ),
        NOT_EQUAL(SqlCommonConstants.NEQ),
        NULL(SqlCommonConstants.NULL),
        NOT_NULL(SqlCommonConstants.NOT_NULL),
        IN(SqlCommonConstants.IN),
        NOT_IN(SqlCommonConstants.NOT_IN),
        EXISTS(SqlCommonConstants.EXISTS),
        NOT_EXISTS(SqlCommonConstants.NOT_EXISTS),
        GT(SqlCommonConstants.GT),
        GTE(SqlCommonConstants.GTE),
        LT(SqlCommonConstants.LT),
        LTE(SqlCommonConstants.LTE),
        AND(SqlCommonConstants.AND),
        OR(SqlCommonConstants.OR),
        XOR(SqlCommonConstants.XOR),
        NOT(SqlCommonConstants.NOT),
        LIKE(SqlCommonConstants.LIKE),
        BETWEEN(SqlCommonConstants.BETWEEN),
        NOT_BETWEEN(SqlCommonConstants.NOT_BETWEEN),
        SET(SqlCommonConstants.SET),
        SQL(null);

        private final String symbol;

        Symbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return this.getSymbol();
        }
    }
}
