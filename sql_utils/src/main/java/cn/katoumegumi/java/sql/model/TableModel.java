package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

/**
 * 表
 */
public class TableModel {
    /**
     * 表信息
     */
    private final FieldColumnRelationMapper table;

    /**
     * 别名（一次查询中唯一）
     */
    private final String alias;

    public TableModel(FieldColumnRelationMapper table, String alias) {
        this.table = table;
        this.alias = alias;
    }

    public FieldColumnRelationMapper getTable() {
        return table;
    }

    public String getAlias() {
        return alias;
    }
}
