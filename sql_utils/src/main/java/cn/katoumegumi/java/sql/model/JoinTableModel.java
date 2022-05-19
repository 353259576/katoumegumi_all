package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.common.TableJoinType;

import javax.persistence.criteria.JoinType;

public class JoinTableModel{

    private final TableModel table;

    private final TableModel joinTable;

    private final TableJoinType joinType;

    private final ConditionRelationModel on;

    public JoinTableModel(TableModel table, TableModel joinTable, TableJoinType joinType, ConditionRelationModel on) {
        this.table = table;
        this.joinTable = joinTable;
        this.joinType = joinType;
        this.on = on;
    }

    public TableModel getTable() {
        return table;
    }

    public TableModel getJoinTable() {
        return joinTable;
    }

    public TableJoinType getJoinType() {
        return joinType;
    }

    public ConditionRelationModel getOn() {
        return on;
    }
}
