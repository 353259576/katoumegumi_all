package cn.katoumegumi.java.sql.model.result;

import cn.katoumegumi.java.sql.model.component.JoinTableModel;
import cn.katoumegumi.java.sql.model.condition.Condition;
import cn.katoumegumi.java.sql.model.condition.RelationCondition;

import java.util.List;

/**
 * 修改语句
 */
public class UpdateModel {

    private final TableModel from;

    private final List<JoinTableModel> joinList;

    private final RelationCondition where;

    private final List<Condition> updateConditionList;

    public UpdateModel(TableModel from, List<JoinTableModel> joinList, List<Condition> updateConditionList, RelationCondition where) {
        this.from = from;
        this.joinList = joinList;
        this.updateConditionList = updateConditionList;
        this.where = where;
    }

    public TableModel getFrom() {
        return from;
    }

    public List<JoinTableModel> getJoinList() {
        return joinList;
    }

    public RelationCondition getWhere() {
        return where;
    }

    public List<Condition> getUpdateConditionList() {
        return updateConditionList;
    }
}
