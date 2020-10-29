package cn.katoumegumi.java.sql.common;

/**
 * sql类型
 * @author ws
 */
public enum SqlType {
    /**
     * 查询
     */
    SELECT("select"),
    /**
     * 修改
     */
    UPDATE("update"),
    /**
     * 删除
     */
    DELETE("delete"),
    /**
     * 插入
     */
    INSERT("insert");


    private String value;
    private SqlType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public SqlType setValue(String value) {
        this.value = value;
        return this;
    }
}
