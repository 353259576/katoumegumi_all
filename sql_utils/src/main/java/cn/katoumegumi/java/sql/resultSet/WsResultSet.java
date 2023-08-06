package cn.katoumegumi.java.sql.resultSet;

import java.sql.SQLException;

public interface WsResultSet {

    /**
     * 获取列数
     *
     * @return
     */
    int getColumnCount() throws SQLException;

    /**
     * 通过列索引获取列名
     *
     * @param columnIndex
     * @return
     */
    String getColumnLabel(int columnIndex) throws SQLException;

    /**
     * 切换下一行
     *
     * @return
     */
    boolean next() throws SQLException;

    /**
     * 获取值
     *
     * @param index
     * @return
     */
    Object getObject(int index) throws SQLException;
}
