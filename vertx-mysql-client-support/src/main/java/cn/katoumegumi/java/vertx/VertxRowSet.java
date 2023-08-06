package cn.katoumegumi.java.vertx;

import cn.katoumegumi.java.sql.resultSet.WsResultSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;

import java.sql.SQLException;
import java.util.List;

public class VertxRowSet implements WsResultSet {

    private final RowIterator<Row> rowRowIterator;

    private final List<String> columnNames;

    private Row cacheRow;

    public VertxRowSet(RowSet<Row> rowSet){
        this.rowRowIterator = rowSet.iterator();
        this.columnNames = rowSet.columnsNames();
    }

    /**
     * 获取列数
     *
     * @return
     */
    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * 通过列索引获取列名
     *
     * @param columnIndex
     * @return
     */
    @Override
    public String getColumnLabel(int columnIndex) {
        return columnNames.get(columnIndex - 1);
    }

    /**
     * 切换下一行
     *
     * @return
     */
    @Override
    public boolean next() {
        if(rowRowIterator.hasNext()){
            cacheRow = rowRowIterator.next();
            return true;
        }else {
            return false;
        }
    }

    /**
     * 获取值
     *
     * @param index
     * @return
     */
    @Override
    public Object getObject(int index) {
        return cacheRow.getValue(index - 1);
    }
}
