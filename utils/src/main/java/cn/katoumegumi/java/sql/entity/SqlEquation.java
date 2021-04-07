package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.MySearchList;

import java.util.ArrayList;
import java.util.List;

/**
 * 公式
 * @author 星梦苍天
 */
public class SqlEquation {

    /**
     * 1 columnName 2 符号 3 值
     */
    private final List<Integer> typeList = new ArrayList<>();

    private final List<Object> valueList = new ArrayList<>();

    public SqlEquation column(String columnName){
        typeList.add(1);
        valueList.add(columnName);
        return this;
    }

    public SqlEquation column(SqlEquation equation){
        typeList.add(1);
        valueList.add(equation);
        return this;
    }

    public SqlEquation column(String tableName,String columnName){
        if(WsStringUtils.isBlank(tableName)){
            return column(columnName);
        }
        typeList.add(1);
        valueList.add(tableName+"."+columnName);
        return this;
    }

    public <T> SqlEquation column(SFunction<T,?> columnFunction){
        typeList.add(1);
        valueList.add(WsFieldUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation column(String tableName,SFunction<T,?> columnFunction){
        if(WsStringUtils.isBlank(tableName)){
            return column(columnFunction);
        }
        typeList.add(1);
        valueList.add(tableName+"."+WsFieldUtils.getFieldName(columnFunction));
        return this;
    }

    public <T> SqlEquation sql(MySearchList mySearchList){
        typeList.add(4);
        valueList.add(mySearchList);
        return this;
    }

    public SqlEquation value(Object o){
        typeList.add(3);
        valueList.add(o);
        return this;
    }

    public SqlEquation symbol(Object o){
        typeList.add(2);
        valueList.add(o);
        return this;
    }

    public SqlEquation add(){
        return symbol(Symbol.ADD.getSymbol());
    }
    public SqlEquation subtract(){
        return symbol(Symbol.SUBTRACT.getSymbol());
    }
    public SqlEquation multiply(){
        return symbol(Symbol.MULTIPLY.getSymbol());
    }
    public SqlEquation equal(){
        return symbol(Symbol.EQUAL.getSymbol());
    }

    public SqlEquation notEqual(){
        return symbol(Symbol.NOT_EQUAL.getSymbol());
    }

    public SqlEquation in(){
        return symbol(Symbol.IN.getSymbol());
    }

    public SqlEquation notIn(){
        return symbol(Symbol.NOT_IN.getSymbol());
    }

    public SqlEquation isNull(){
        return symbol(Symbol.NULL.getSymbol());
    }

    public SqlEquation isNotNull(){
        return symbol(Symbol.NOT_NULL.getSymbol());
    }

    public SqlEquation exists(){
        return symbol(Symbol.EXISTS.getSymbol());
    }

    public SqlEquation notExists(){
        return symbol(Symbol.NOT_EXISTS.getSymbol());
    }

    public SqlEquation divide(){
        return symbol(Symbol.DIVIDE.getSymbol());
    }

    public SqlEquation gt(){
        return symbol(Symbol.GT.getSymbol());
    }
    public SqlEquation gte(){
        return symbol(Symbol.GTE.getSymbol());
    }

    public SqlEquation lt(){
        return symbol(Symbol.LT.getSymbol());
    }

    public SqlEquation lte(){
        return symbol(Symbol.LTE.getSymbol());
    }

    public SqlEquation and(){
        return symbol(Symbol.AND.getSymbol());
    }

    public SqlEquation or(){
        return symbol(Symbol.OR.getSymbol());
    }

    public SqlEquation xor(){
        return symbol(Symbol.XOR.getSymbol());
    }

    public SqlEquation not(){
        return symbol(Symbol.NOT.getSymbol());
    }

    public enum Symbol {
        /**
         * 符号
         */
        ADD(" + "),
        SUBTRACT(" - "),
        MULTIPLY(" * "),
        DIVIDE(" / "),
        EQUAL(" = "),
        NOT_EQUAL(" != "),
        NULL(" is NULL "),
        NOT_NULL(" is not NULL "),
        IN(" in "),
        NOT_IN(" not in "),
        EXISTS(" exists "),
        NOT_EXISTS(" not exists "),
        GT(" > "),
        GTE(" >= "),
        LT(" < "),
        LTE(" <= "),
        AND(" & "),
        OR(" | "),
        XOR(" ^ "),
        NOT(" ~ ")
        ;

        private final String symbol;

        private Symbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public List<Integer> getTypeList() {
        return typeList;
    }

    public List<Object> getValueList() {
        return valueList;
    }
}
