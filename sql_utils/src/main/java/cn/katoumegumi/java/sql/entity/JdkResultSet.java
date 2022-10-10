package cn.katoumegumi.java.sql.entity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 转换ResultSet
 */
public class JdkResultSet implements WsResultSet {

    private final ResultSet resultSet;

    private final ResultSetMetaData resultSetMetaData;


    public JdkResultSet(ResultSet resultSet){
        this.resultSet = resultSet;
        try {
            this.resultSetMetaData = resultSet.getMetaData();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取列数
     *
     * @return
     */
    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }

    /**
     * 通过列索引获取列名
     *
     * @param columnIndex
     * @return
     */
    @Override
    public String getColumnLabel(int columnIndex) throws SQLException {
        return this.resultSetMetaData.getColumnLabel(columnIndex);
    }

    /**
     * 切换下一行
     *
     * @return
     */
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    /**
     * 获取值
     *
     * @param index
     * @return
     */
    @Override
    public Object getObject(int index) throws SQLException {
        return resultSet.getObject(index);
    }
}
