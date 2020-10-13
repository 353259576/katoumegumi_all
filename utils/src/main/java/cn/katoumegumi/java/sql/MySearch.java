package cn.katoumegumi.java.sql;

/**
 * 查询条件
 * @author ws
 */
public class MySearch {
    private String fieldName;
    private SqlOperator operator;
    private Object value;

    private MySearch() {

    }

    public MySearch(String fieldName, SqlOperator operator, Object value) {
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SqlOperator getOperator() {
        return operator;
    }

    public void setOperator(SqlOperator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
