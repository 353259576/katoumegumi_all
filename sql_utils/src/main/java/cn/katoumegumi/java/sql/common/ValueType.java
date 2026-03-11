package cn.katoumegumi.java.sql.common;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.model.component.BaseTableColumn;
import cn.katoumegumi.java.sql.model.component.SqlEquation;
import cn.katoumegumi.java.sql.model.component.SqlStringModel;
import cn.katoumegumi.java.sql.model.condition.MultiExpressionCondition;
import cn.katoumegumi.java.sql.model.condition.RelationCondition;
import cn.katoumegumi.java.sql.model.condition.SingleExpressionCondition;
import cn.katoumegumi.java.sql.model.condition.SqlFunctionCondition;
import cn.katoumegumi.java.sql.model.result.SelectModel;

import java.util.Collection;

/**
 * 值类型常量
 */
public class ValueType {

    /**
     * 空值
     */
    public static final int NULL_TYPE = 0;

    /**
     * 字段名称
     */
    public static final int COLUMN_NAME_TYPE = 1;

    /**
     * 符号
     */
    public static final int SYMBOL_TYPE = 2;

    /**
     * 基本对象类型
     */
    public static final int BASE_VALUE_TYPE = 3;

    /**
     * MySearchList
     */
    public static final int SEARCH_LIST_TYPE = 4;

    /**
     * 集合类型
     */
    public static final int COLLECTION_TYPE = 5;

    /**
     * 数组类型
     */
    public static final int ARRAY_TYPE = 6;

    /**
     * SelectModel
     */
    public static final int SELECT_MODEL_TYPE = 7;

    /**
     * SqlStringModel
     */
    public static final int SQL_STRING_MODEL_TYPE = 8;

    /**
     * ConditionRelationModel
     */
    public static final int CONDITION_RELATION_MODEL_TYPE = 9;

    /**
     * SqlEquation
     */
    public static final int SQL_EQUATION_MODEL = 10;

    /**
     * SqlFunction
     */
    public static final int SQL_FUNCTION_MODEL = 11;

    public static final int SINGLE_EXPRESSION_CONDITION_MODEL = 12;

    public static final int MULTI_EXPRESSION_CONDITION_MODEL = 13;

    public static final int SQL_WHERE_VALUE_MODEL = 14;

    public static final int SQL_FUNCTION_CONDITION = 15;

    /**
     * 空值
     */
    public static final int NULL_VALUE_MODEL = 16;

    /**
     * sql 字符串
     */
    public static final int SQL_STRING_VALUE_MODEL = 17;


    public static  int getValueType(Object value) {
        if (value == null) {
            return ValueType.NULL_TYPE;
        } else if (value instanceof BaseTableColumn) {
            return ValueType.COLUMN_NAME_TYPE;
        } else if (value instanceof SqlEquation.Symbol) {
            return ValueType.SYMBOL_TYPE;
        } else if (WsBeanUtils.isBaseType(value.getClass())) {
            return ValueType.BASE_VALUE_TYPE;
        } else if (value instanceof Collection) {
            return ValueType.COLLECTION_TYPE;
        }  else if (value instanceof SqlStringModel) {
            return ValueType.SQL_STRING_MODEL_TYPE;
        } else if (value instanceof SelectModel) {
            return ValueType.SELECT_MODEL_TYPE;
        } else if (value instanceof RelationCondition) {
            return ValueType.CONDITION_RELATION_MODEL_TYPE;
        } else if (value instanceof SingleExpressionCondition) {
            return ValueType.SINGLE_EXPRESSION_CONDITION_MODEL;
        } else if (value instanceof MultiExpressionCondition) {
            return ValueType.MULTI_EXPRESSION_CONDITION_MODEL;
        } else if (value instanceof SqlFunctionCondition) {
            return ValueType.SQL_FUNCTION_CONDITION;
        } else if (value.equals(SqlCommonConstants.NULL_VALUE)) {
            return ValueType.NULL_VALUE_MODEL;
        } else if (value.getClass().isArray()) {
            return ValueType.ARRAY_TYPE;
        }
        throw new IllegalArgumentException("不支持的类型:" + value.getClass());
    }
}
