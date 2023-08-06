package cn.katoumegumi.java.sql.model.condition;

import cn.katoumegumi.java.sql.common.SqlOperator;

import java.util.List;

/**
 * 关系条件(and和or)
 */
public class RelationCondition implements Condition {

    private final List<Condition> conditionList;

    private final SqlOperator relation;

    public RelationCondition(List<Condition> conditionList, SqlOperator relation) {
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
