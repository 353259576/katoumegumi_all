package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.common.SqlOperator;

import java.util.List;

/**
 * and与or
 */
public class ConditionRelationModel {

    private final List<ExpressionCondition> expressionConditionList;

    private final SqlOperator relation;

    public ConditionRelationModel(List<ExpressionCondition> expressionConditionList, SqlOperator relation) {
        this.expressionConditionList = expressionConditionList;
        this.relation = relation;
    }

    public List<ExpressionCondition> getExpressionConditionList() {
        return expressionConditionList;
    }

    public SqlOperator getRelation() {
        return relation;
    }
}
