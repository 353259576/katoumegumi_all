package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.model.condition.RelationCondition;
import cn.katoumegumi.java.sql.model.result.TableModel;

/**
 * 关联表
 */
public class JoinTableModel {

    private final TableModel table;

    private final TableModel joinTable;

    private final TableJoinType joinType;

    private final RelationCondition on;

    private final boolean queryColumn;

    public JoinTableModel(TableModel table, TableModel joinTable, TableJoinType joinType, RelationCondition on,boolean queryColumn) {
        this.table = table;
        this.joinTable = joinTable;
        this.joinType = joinType == null ? TableJoinType.INNER_JOIN : joinType;
        this.on = on;
        this.queryColumn = queryColumn;
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

    public boolean isQueryColumn() {
        return queryColumn;
    }
}
