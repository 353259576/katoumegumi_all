package cn.katoumegumi.java.sql.model;

import java.util.List;

/**
 * delete 语句
 */
public class DeleteModel {

    private final TableModel from;

    private final List<JoinTableModel> joinList;

    private final RelationCondition where;


    public DeleteModel(TableModel from, List<JoinTableModel> joinList, RelationCondition where) {
        this.from = from;
        this.joinList = joinList;
        this.where = where;
        if(where == null){
            throw new IllegalArgumentException("禁止全表删除");
        }
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
}
