package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.SqlLimit;

import java.util.List;

/**
 * select 语句
 */
public class SelectModel {

    private final List<ColumnBaseEntity> select;

    private final TableModel from;

    private final List<JoinTableModel> joinList;

    private final ConditionRelationModel where;

    private final List<OrderByModel> orderBy;

    private final SqlLimit sqlLimit;

    public SelectModel(List<ColumnBaseEntity> select, TableModel from, List<JoinTableModel> joinList, ConditionRelationModel where, List<OrderByModel> orderBy,SqlLimit sqlLimit) {
        this.select = select;
        this.from = from;
        this.joinList = joinList;
        this.where = where;
        this.orderBy = orderBy;
        this.sqlLimit = sqlLimit;
    }

    public List<ColumnBaseEntity> getSelect() {
        return select;
    }

    public TableModel getFrom() {
        return from;
    }

    public List<JoinTableModel> getJoinList() {
        return joinList;
    }

    public ConditionRelationModel getWhere() {
        return where;
    }

    public List<OrderByModel> getOrderBy() {
        return orderBy;
    }

    public SqlLimit getSqlLimit() {
        return sqlLimit;
    }
}
