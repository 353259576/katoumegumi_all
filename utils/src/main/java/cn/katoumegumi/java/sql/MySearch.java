package cn.katoumegumi.java.sql;

/**
 * 查询条件
 * @author ws
 */
public class MySearch {
    private final String fieldName;
    private final SqlOperator operator;
    private Object value;


    public MySearch(String fieldName, SqlOperator operator, Object value) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }


    public String getFieldName() {
        return fieldName;
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
