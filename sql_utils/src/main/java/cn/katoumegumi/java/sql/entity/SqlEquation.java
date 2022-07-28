package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.common.SqlCommon;
import cn.katoumegumi.java.sql.common.ValueType;

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
        typeList.add(ValueType.COLUMN_NAME_TYPE);
        valueList.add(columnName);
        return this;
    }

    public SqlEquation column(SqlEquation equation) {
        typeList.add(ValueType.SQL_EQUATION_MODEL);
        valueList.add(equation);
        return this;
    }

    public SqlEquation column(SqlFunction function) {
        typeList.add(ValueType.SQL_FUNCTION_MODEL);
        valueList.add(function);
        return this;
    }

    public SqlEquation column(String tableName, String columnName) {
        if (WsStringUtils.isBlank(tableName)) {
            return column(columnName);
        }
        typeList.add(ValueType.COLUMN_NAME_TYPE);
        valueList.add(tableName + "." + columnName);
        return this;
    }

    public <T> SqlEquation column(SFunction<T, ?> columnFunction) {
        typeList.add(ValueType.COLUMN_NAME_TYPE);
        valueList.add(WsFieldUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation column(String tableName, SFunction<T, ?> columnFunction) {
        if (WsStringUtils.isBlank(tableName)) {
            return column(columnFunction);
        }
        typeList.add(ValueType.COLUMN_NAME_TYPE);
        valueList.add(tableName + "." + WsFieldUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation sql(MySearchList mySearchList) {
        typeList.add(ValueType.SEARCH_LIST_TYPE);
        valueList.add(mySearchList);
        return this;
    }

    public SqlEquation value(Object o) {
        int valueType;
        if(WsBeanUtils.isBaseType(o.getClass())){
            valueType = ValueType.BASE_VALUE_TYPE;
        }else if(o instanceof Collection){
            valueType = ValueType.COLLECTION_TYPE;
        }else if(WsBeanUtils.isArray(o.getClass())){
            valueType = ValueType.ARRAY_TYPE;
        }else {
            throw new IllegalArgumentException("不支持的值");
        }
        typeList.add(valueType);
        valueList.add(o);
        return this;
    }

    public SqlEquation symbol(Object o) {
        typeList.add(ValueType.SYMBOL_TYPE);
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
        ADD(SqlCommon.ADD),
        SUBTRACT(SqlCommon.SUBTRACT),
        MULTIPLY(SqlCommon.MULTIPLY),
        DIVIDE(SqlCommon.DIVIDE),
        EQUAL(SqlCommon.EQ),
        NOT_EQUAL(SqlCommon.NEQ),
        NULL(SqlCommon.NULL),
        NOT_NULL(SqlCommon.NOT_NULL),
        IN(SqlCommon.IN),
        NOT_IN(SqlCommon.NOT_IN),
        EXISTS(SqlCommon.EXISTS),
        NOT_EXISTS(SqlCommon.NOT_EXISTS),
        GT(SqlCommon.GT),
        GTE(SqlCommon.GTE),
        LT(SqlCommon.LT),
        LTE(SqlCommon.LTE),
        AND(SqlCommon.AND),
        OR(SqlCommon.OR),
        XOR(SqlCommon.XOR),
        NOT(SqlCommon.NOT),
        LIKE(SqlCommon.LIKE),
        BETWEEN(SqlCommon.BETWEEN),
        NOT_BETWEEN(SqlCommon.NOT_BETWEEN),
        SET(SqlCommon.SET),
        SQL(null)
        ;

        private final String symbol;

        private Symbol(String symbol) {
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
