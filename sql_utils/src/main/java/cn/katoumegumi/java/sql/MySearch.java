package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.common.SqlOperator;
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

    /**
     * 获取列
     * @return
     */
    public QueryElement getColumn() {
        return column;
    }

    /**
     * 获取符号
     * @return
     */
    public SqlOperator getOperator() {
        return operator;
    }

    /**
     * 获取值
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置值
     * @param value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
