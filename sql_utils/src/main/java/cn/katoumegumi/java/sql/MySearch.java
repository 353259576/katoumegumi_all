package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.model.query.QueryColumn;
import cn.katoumegumi.java.sql.model.query.QueryElement;

/**
 * 查询条件
 *
 * @author ws
 */
public class MySearch {
    private final QueryElement column;
    private final SqlOperator operator;
    private Object value;


    public MySearch(QueryElement column, SqlOperator operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }


    public QueryElement getColumn() {
        return column;
    }

    public SqlOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
