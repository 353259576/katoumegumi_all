package cn.katoumegumi.java.sql.entity;

/**
 * @author 星梦苍天
 */
public class SqlWhereValue {

    private final String alias;

    private final Object value;


    public SqlWhereValue(String alias,Object value){
        this.alias = alias;
        this.value = value;
    }

    public SqlWhereValue(Object value){
        this.alias = null;
        this.value = value;
    }



    public String getAlias() {
        return alias;
    }

    public Object getValue() {
        return value;
    }
}
