package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.model.result.TableModel;

/**
 * 主表
 */
public class FromTableModel extends TableModel {

    public FromTableModel(FieldColumnRelationMapper table, String alias) {
        super(table, alias);
    }
}
