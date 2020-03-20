package cn.katoumegumi.java.sql;

/**
 * @author 10480
 */

public enum SqlOperator {

    /**
     * 等于
     */
    EQ,
    /**
     * 模糊查询
     */
    LIKE,
    /**
     * 大于
     */
    GT,
    /**
     * 小于
     */
    LT,
    /**
     * 大于等于
     */
    GTE,
    /**
     * 小于等于
     */
    LTE,
    /**
     * in
     */
    IN,
    /**
     * not in
     */
    NIN,
    /**
     * not null
     */
    NOTNULL,
    /**
     * is null
     */
    NULL,
    /**
     * 不等于
     */
    NE,
    /**
     * 嵌入sql 只有hibernate支持
     */
    SQL,
    /**
     * 排序
     */
    SORT,
    /**
     * and
     */
    AND,
    /**
     * or
     */
    OR;

}
