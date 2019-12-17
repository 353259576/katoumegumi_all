package cn.katoumegumi.java.hibernate;

public class MySearch {
    private String fieldName;
    private JpaDataHandle.Operator operator;
    private Object value;
    private String tableNickName;
    private Class tableClass;

    private MySearch(){

    }

    public MySearch(String fieldName, JpaDataHandle.Operator operator,Object value){
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    public MySearch(Class tableClass,String tableNickName,String fieldName, JpaDataHandle.Operator operator,Object value){
        this.tableClass = tableClass;
        this.tableNickName = tableNickName;
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

    public JpaDataHandle.Operator getOperator() {
        return operator;
    }

    public void setOperator(JpaDataHandle.Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getTableNickName() {
        return tableNickName;
    }

    public void setTableNickName(String tableNickName) {
        this.tableNickName = tableNickName;
    }

    public Class getTableClass() {
        return tableClass;
    }

    public void setTableClass(Class tableClass) {
        this.tableClass = tableClass;
    }
}
