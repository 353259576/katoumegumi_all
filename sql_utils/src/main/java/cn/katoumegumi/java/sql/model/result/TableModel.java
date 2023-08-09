package cn.katoumegumi.java.sql.model.result;

import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

/**
 * 表
 */
public class TableModel {
    /**
     * 表信息
     */
    private final PropertyColumnRelationMapper table;

    /**
     * 别名（一次查询中唯一）
     */
    private final String alias;

    public TableModel(PropertyColumnRelationMapper table, String alias) {
        this.table = table;
        this.alias = alias;
    }

    public PropertyColumnRelationMapper getTable() {
        return table;
    }

    public String getAlias() {
        return alias;
    }
}
