package cn.katoumegumi.java.sql.model.condition;

import cn.katoumegumi.java.sql.common.OrderByTypeEnums;
import cn.katoumegumi.java.sql.model.component.BaseTableColumn;
import cn.katoumegumi.java.sql.model.component.SqlStringModel;

/**
 * order by 条件
 */
public class OrderByCondition implements Condition {

    private final SqlStringModel sql;

    private final BaseTableColumn column;

    private final OrderByTypeEnums type;

    public OrderByCondition(BaseTableColumn column, OrderByTypeEnums type) {
        this.column = column;
        this.type = type;
        this.sql = null;
    }

    public OrderByCondition(SqlStringModel sql, OrderByTypeEnums type) {
        this.sql = sql;
        this.type = type;
        this.column = null;
    }

    public BaseTableColumn getColumn() {
        return column;
    }

    public OrderByTypeEnums getType() {
        return type;
    }

    public SqlStringModel getSql() {
        return sql;
    }

    /*@Override
    public SqlStringModel toSqlString() {
        String sql = SQLModelFactory.guardKeyword(column.getAlias()) + SqlCommon.SQL_COMMON_DELIMITER + SQLModelFactory.guardKeyword(column.getColumnName()) + SqlCommon.SPACE + type;
        return new SqlStringModel(sql,null);
    }*/
}
