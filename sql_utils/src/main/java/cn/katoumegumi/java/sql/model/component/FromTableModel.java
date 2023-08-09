package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;
import cn.katoumegumi.java.sql.model.result.TableModel;

/**
 * 主表
 */
public class FromTableModel extends TableModel {

    public FromTableModel(PropertyColumnRelationMapper table, String alias) {
        super(table, alias);
    }
}
