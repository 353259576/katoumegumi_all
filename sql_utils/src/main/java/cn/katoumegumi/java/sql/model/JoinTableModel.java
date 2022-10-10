package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.common.TableJoinType;

/**
 * 关联表
 */
public class JoinTableModel{

    private final TableModel table;

    private final TableModel joinTable;

    private final TableJoinType joinType;

    private final RelationCondition on;

    public JoinTableModel(TableModel table, TableModel joinTable, TableJoinType joinType, RelationCondition on) {
        this.table = table;
        this.joinTable = joinTable;
        this.joinType = joinType == null?TableJoinType.INNER_JOIN:joinType;
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

    public RelationCondition getOn() {
        return on;
    }
}
