package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.common.SqlOperator;

import java.util.List;

/**
 * and与or
 */
public class ConditionRelationModel implements Condition {

    private final List<Condition> conditionList;

    private final SqlOperator relation;

    public ConditionRelationModel(List<Condition> conditionList, SqlOperator relation) {
        this.conditionList = conditionList;
        this.relation = relation;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public SqlOperator getRelation() {
        return relation;
    }
}
