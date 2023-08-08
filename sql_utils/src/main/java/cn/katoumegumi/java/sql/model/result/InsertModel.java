package cn.katoumegumi.java.sql.model.result;

import cn.katoumegumi.java.sql.model.component.TableColumn;

import java.util.List;

/**
 * insert语句
 */
public class InsertModel {

    private final TableModel from;

    private final List<TableColumn> idList;

    private final List<TableColumn> columnList;


    public InsertModel(TableModel from, List<TableColumn> idList, List<TableColumn> columnList) {
        this.from = from;
        this.idList = idList;
        this.columnList = columnList;
    }


    public TableModel getFrom() {
        return from;
    }

    public List<TableColumn> getIdList() {
        return idList;
    }

    public List<TableColumn> getColumnList() {
        return columnList;
    }
}
