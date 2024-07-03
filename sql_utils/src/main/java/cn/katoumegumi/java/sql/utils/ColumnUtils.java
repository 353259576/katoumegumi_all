package cn.katoumegumi.java.sql.utils;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;

public class ColumnUtils {

    /**
     * 获取列名
     * @param tableName
     * @param columnName
     * @return
     */
    public static String name(String tableName,String columnName) {
        if (WsStringUtils.isBlank(columnName)){
            throw new IllegalArgumentException("columnName is null or empty");
        }
        if (WsStringUtils.isBlank(tableName)){
            return columnName;
        }else {
            return tableName + "." + columnName;
        }
    }

    /**
     * 获取列名
     * @param tableName
     * @param columnName
     * @return
     */
    public static String name(String tableName, SFunction<?, ?> columnName) {
        if (columnName==null){
            throw new IllegalArgumentException("columnName is null");
        }
        return name(tableName, WsReflectUtils.getFieldName(columnName));
    }

    /**
     * 获取列名
     * @param columnName
     * @return
     */
    public static String name(SFunction<?, ?> columnName) {
        if (columnName==null){
            throw new IllegalArgumentException("columnName is null");
        }
        return WsReflectUtils.getFieldName(columnName);
    }

    /**
     * 获取列名
     * @param columnName
     * @return
     */
    public static String name(String columnName) {
        if (WsStringUtils.isBlank(columnName)){
            throw new IllegalArgumentException("columnName is null or empty");
        }
        return columnName;
    }

}
