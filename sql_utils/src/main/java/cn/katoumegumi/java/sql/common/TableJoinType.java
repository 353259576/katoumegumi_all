package cn.katoumegumi.java.sql.common;

/**
 * 表管理类型
 *
 * @author ws
 */
public enum TableJoinType {

    /**
     * 主表
     */
    FROM(" from "),
    /**
     * 内联
     */
    INNER_JOIN(" inner join "),
    /**
     * 左连
     */
    LEFT_JOIN(" left join "),
    /**
     * 右连
     */
    RIGHT_JOIN(" right join ");


    private final String value;

    TableJoinType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
