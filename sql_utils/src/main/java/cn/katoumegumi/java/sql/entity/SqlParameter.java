package cn.katoumegumi.java.sql.entity;

/**
 * @author 星梦苍天
 * sql语句动态参数
 */
public class SqlParameter {
    /**
     * 别名
     */
    private final String alias;

    /**
     * 值
     */
    private final Object value;


    public SqlParameter(String alias, Object value){
        this.alias = alias;
        this.value = value;
    }

    public SqlParameter(Object value){
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
