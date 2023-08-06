package cn.katoumegumi.java.vertx;

import cn.katoumegumi.java.sql.resultSet.WsResultSet;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;

public class VertxResultSet implements WsResultSet {


    private final List<String> columnNames;

    private final List<JsonArray> results;

    private final int size;

    private int index = -1;

    private JsonArray cacheJsonArray;

    public VertxResultSet(ResultSet resultSet){
        this.columnNames = resultSet.getColumnNames();
        this.results = resultSet.getResults();
        this.size = results.size();
    }


    /**
     * 获取列数
     *
     * @return
     */
    @Override
    public int getColumnCount() throws SQLException {
        return columnNames.size();
    }

    /**
     * 通过列索引获取列名
     *
     * @param columnIndex
     * @return
     */
    @Override
    public String getColumnLabel(int columnIndex) throws SQLException {
        return columnNames.get(columnIndex - 1);
    }

    /**
     * 切换下一行
     *
     * @return
     */
    @Override
    public boolean next() throws SQLException {
        this.index = this.index + 1;
        if (index < size){
            cacheJsonArray = results.get(index);
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
    public Object getObject(int index) throws SQLException {
        return cacheJsonArray.getValue(index - 1);
    }
}
