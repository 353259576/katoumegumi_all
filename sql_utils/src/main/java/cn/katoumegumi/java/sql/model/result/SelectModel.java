package cn.katoumegumi.java.sql.model.result;

import cn.katoumegumi.java.sql.model.component.JoinTableModel;
import cn.katoumegumi.java.sql.model.component.SqlLimit;
import cn.katoumegumi.java.sql.model.component.TableColumn;
import cn.katoumegumi.java.sql.model.condition.OrderByCondition;
import cn.katoumegumi.java.sql.model.condition.RelationCondition;

import java.util.List;

/**
 * select 语句
 */
public class SelectModel {

    private final List<TableColumn> select;

    private final TableModel from;

    private final List<JoinTableModel> joinList;

    private final RelationCondition where;

    private final List<OrderByCondition> orderBy;

    private final SqlLimit sqlLimit;

    public SelectModel(List<TableColumn> select, TableModel from, List<JoinTableModel> joinList, RelationCondition where, List<OrderByCondition> orderBy, SqlLimit sqlLimit) {
        this.select = select;
        this.from = from;
        this.joinList = joinList;
        this.where = where;
        this.orderBy = orderBy;
        this.sqlLimit = sqlLimit;
    }

    public List<TableColumn> getSelect() {
        return select;
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

    public List<OrderByCondition> getOrderBy() {
        return orderBy;
    }

    public SqlLimit getSqlLimit() {
        return sqlLimit;
    }
}
