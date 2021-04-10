package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.common.TableJoinType;

/**
 * 表
 *
 * @author ws
 */
public class TableEntity {

    /**
     * 类型
     */
    private final TableJoinType tableJoinType;

    /**
     * 表名称
     */
    private final String tableName;

    /**
     * 别名
     */
    private final String alias;

    /**
     * 条件
     */
    private final String condition;

    public TableEntity(TableJoinType type, String tableName, String alias, String condition) {
        this.tableJoinType = type;
        this.tableName = tableName;
        this.alias = alias;
        this.condition = condition;
    }


    public TableJoinType getTableJoinType() {
        return tableJoinType;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAlias() {
        return alias;
    }

    public String getCondition() {
        return condition;
    }
}
