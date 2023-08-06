package cn.katoumegumi.java.sql.model.condition;

import cn.katoumegumi.java.sql.model.component.BaseTableColumn;
import cn.katoumegumi.java.sql.model.component.SqlStringModel;

/**
 * order by 条件
 */
public class OrderByCondition implements Condition {

    private final SqlStringModel sql;

    private final BaseTableColumn column;

    private final String type;

    public OrderByCondition(BaseTableColumn column, String type) {
        this.column = column;
        this.type = type;
        this.sql = null;
    }

    public OrderByCondition(SqlStringModel sql, String type) {
        this.sql = sql;
        this.type = type;
        this.column = null;
    }

    public BaseTableColumn getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }

    public SqlStringModel getSql() {
        return sql;
    }

    /*@Override
    public SqlStringAndParameters toSqlString() {
        String sql = SQLModelUtils.guardKeyword(column.getAlias()) + SqlCommon.SQL_COMMON_DELIMITER + SQLModelUtils.guardKeyword(column.getColumnName()) + SqlCommon.SPACE + type;
        return new SqlStringAndParameters(sql,null);
    }*/
}
