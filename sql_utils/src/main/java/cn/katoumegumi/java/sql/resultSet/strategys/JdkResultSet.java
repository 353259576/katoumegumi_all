package cn.katoumegumi.java.sql.resultSet.strategys;

import cn.katoumegumi.java.sql.resultSet.WsResultSet;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 转换ResultSet
 */
public class JdkResultSet implements WsResultSet {

    private final ResultSet resultSet;

    private final ResultSetMetaData resultSetMetaData;

    private static final Map<Class<?>, BiFunction<Integer,ResultSet,Object>> GET_OBJECT_FUNCTION_MAP = new HashMap<>();

    @FunctionalInterface
    private interface ResultSetGetter {
        Object get(int index, ResultSet rs) throws SQLException;
    }

    private static BiFunction<Integer,ResultSet,Object> getBiFunction(ResultSetGetter getter) {
        return (index, resultSet) -> {
            try {
                return getter.get(index, resultSet);
            } catch (SQLException e) {
                try {
                    return resultSet.getObject(index);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    static {
        GET_OBJECT_FUNCTION_MAP.put(int.class, getBiFunction((index, rs) -> rs.getInt(index)));
        GET_OBJECT_FUNCTION_MAP.put(Integer.class, GET_OBJECT_FUNCTION_MAP.get(int.class));
        GET_OBJECT_FUNCTION_MAP.put(long.class, getBiFunction((index, rs) -> rs.getLong(index)));
        GET_OBJECT_FUNCTION_MAP.put(Long.class, GET_OBJECT_FUNCTION_MAP.get(long.class));
        GET_OBJECT_FUNCTION_MAP.put(short.class, getBiFunction((index, rs) -> rs.getShort(index)));
        GET_OBJECT_FUNCTION_MAP.put(Short.class, GET_OBJECT_FUNCTION_MAP.get(short.class));
        GET_OBJECT_FUNCTION_MAP.put(byte.class, getBiFunction((index, rs) -> rs.getByte(index)));
        GET_OBJECT_FUNCTION_MAP.put(Byte.class, GET_OBJECT_FUNCTION_MAP.get(byte.class));
        GET_OBJECT_FUNCTION_MAP.put(float.class, getBiFunction((index, rs) -> rs.getFloat(index)));
        GET_OBJECT_FUNCTION_MAP.put(Float.class, GET_OBJECT_FUNCTION_MAP.get(float.class));
        GET_OBJECT_FUNCTION_MAP.put(double.class, getBiFunction((index, rs) -> rs.getDouble(index)));
        GET_OBJECT_FUNCTION_MAP.put(Double.class, GET_OBJECT_FUNCTION_MAP.get(double.class));
        GET_OBJECT_FUNCTION_MAP.put(boolean.class, getBiFunction((index, rs) -> rs.getBoolean(index)));
        GET_OBJECT_FUNCTION_MAP.put(Boolean.class, GET_OBJECT_FUNCTION_MAP.get(boolean.class));
        GET_OBJECT_FUNCTION_MAP.put(String.class, getBiFunction((index, rs) -> rs.getString(index)));
        GET_OBJECT_FUNCTION_MAP.put(BigDecimal.class, getBiFunction((index, rs) -> rs.getBigDecimal(index)));
    }

    public JdkResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        try {
            this.resultSetMetaData = resultSet.getMetaData();
        } catch (SQLException e) {
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

    @Override
    public Object getObject(int index, Class<?> clazz) throws SQLException {
        if (clazz == null) {
            return resultSet.getObject(index);
        }
        
        BiFunction<Integer,ResultSet,Object> function = GET_OBJECT_FUNCTION_MAP.get(clazz);
        if (function == null) {
            return resultSet.getObject(index);
        }
        return function.apply(index, resultSet);
    }
}
