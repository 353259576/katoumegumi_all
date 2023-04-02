package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

/**
 * 主表
 */
public class FromTableModel extends TableModel {

    public FromTableModel(FieldColumnRelationMapper table, String alias) {
        super(table, alias);
    }
}
