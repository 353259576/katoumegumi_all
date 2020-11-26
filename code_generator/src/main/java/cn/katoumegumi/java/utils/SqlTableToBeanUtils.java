package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.common.WsStringUtils;

import javax.persistence.Column;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * table转换为java bean
 * @author ws
 */
public class SqlTableToBeanUtils {

    private DataSource dataSource;

    private String dbName;


    public SqlTableToBeanUtils(DataSource dataSource,String dbName){
        this.dataSource = dataSource;
        this.dbName = dbName;
    }


    private Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    public List<Column> selectTableColumns(String tableName){

        String sql = "select COLUMN_NAME,COLUMN_COMMENT,COLUMN_TYPE,COLUMN_KEY from INFORMATION_SCHEMA.Columns WHERE TABLE_SCHEMA = ? AND TABLE_NAME=?";
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,dbName);
            preparedStatement.setString(2,tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Column> columnList = new ArrayList<>();
            while (resultSet.next()){
                String colName = resultSet.getString("COLUMN_NAME");
                String colRemark = resultSet.getString("COLUMN_COMMENT");
                String colType = resultSet.getString("COLUMN_TYPE");
                String colKey = resultSet.getString("COLUMN_KEY");
                Column column = new Column(colName, colRemark, colType, colKey);
                columnList.add(column);
            }
            resultSet.close();
            preparedStatement.close();
            return columnList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }

    public List<Table> selectTables(String tableName){

        String sql = "select TABLE_SCHEMA,TABLE_NAME,TABLE_COMMENT from INFORMATION_SCHEMA.`TABLES` WHERE TABLE_SCHEMA = ?";
        if(WsStringUtils.isNotBlank(tableName)){
            sql += " and TABLE_NAME = ?";
        }
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,dbName);
            if(WsStringUtils.isNotBlank(tableName)){
                preparedStatement.setString(2,tableName);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Table> tableList = new ArrayList<>();
            while (resultSet.next()){
                String name = resultSet.getString("TABLE_NAME");
                String remark = resultSet.getString("TABLE_COMMENT");
                Table table = new Table(name, remark,WsStringUtils.camelCase(name),selectTableColumns(name));
                tableList.add(table);
            }
            resultSet.close();
            preparedStatement.close();
            return tableList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }




    public static class Column{
        /**
         * 字段名
         */
        private String columnName;

        /**
         * 字段备注
         */
        private String columnRemark;

        /**
         * 字段类型
         */
        private String columnType;

        /**
         * 键类型
         */
        private String columnKey;

        /**
         * java bean field名称
         */
        private String beanFieldName;

        private Class<?> columnClass;


        private static Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

        static {
            classMap.put("varchar",String.class);
            classMap.put("int",Integer.class);
            classMap.put("bigint",Long.class);
            classMap.put("float",Float.class);
            classMap.put("double",Double.class);
            classMap.put("decimal", BigDecimal.class);
            classMap.put("tinyint",Integer.class);
            classMap.put("blob",String.class);
            classMap.put("timestamp", Date.class);
            classMap.put("datetime",Date.class);

        }

        public Column(String columnName,String columnRemark,String columnType,String columnKey){
            this.columnName = columnName;
            this.columnRemark = columnRemark;
            this.columnKey = columnKey;
            this.beanFieldName = WsStringUtils.camelCase(columnName.toLowerCase());
            if(WsStringUtils.isNotBlank(columnType)){
                int index = columnType.indexOf("(");
                if(index > 0){
                    columnType = columnType.substring(0,index);
                }
            }
            this.columnType = columnType;
            this.columnClass = classMap.get(columnType);
            if(this.columnClass == null){
                this.columnClass = Object.class;
            }
        }

        public String getColumnName() {
            return columnName;
        }


        public String getColumnRemark() {
            return columnRemark;
        }

        public String getColumnType() {
            return columnType;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public String getBeanFieldName() {
            return beanFieldName;
        }

        public Class<?> getColumnClass() {
            return columnClass;
        }
    }

    public static class Table{
        private final String tableName;

        private final String tableRemark;

        private final String entityName;

        private final String firstLowerEntityName;

        private Column pkColumn;

        private final List<Class<?>> classList;

        private final List<Column> columnList;

        public Table(String tableName,String tableRemark,String firstLowerEntityName,List<Column> columnList){
            this.tableName = tableName;
            this.tableRemark = tableRemark;
            this.entityName = firstLowerEntityName.substring(0,1).toUpperCase()+firstLowerEntityName.substring(1);
            this.firstLowerEntityName = firstLowerEntityName;
            this.columnList = columnList;
            this.classList = columnList.stream().map(Column::getColumnClass).distinct().collect(Collectors.toList());
            for(Column column:columnList){
                if(column.getColumnKey().equals("PRI")){
                    pkColumn = column;
                    break;
                }
            }
            if(pkColumn == null){
                pkColumn = columnList.get(0);
            }
        }

        public String getTableName() {
            return tableName;
        }

        public String getTableRemark() {
            return tableRemark;
        }

        public List<Column> getColumnList() {
            return columnList;
        }

        public String getEntityName() {
            return entityName;
        }

        public String getFirstLowerEntityName() {
            return firstLowerEntityName;
        }

        public Column getPkColumn() {
            return pkColumn;
        }

        public List<Class<?>> getClassList() {
            return classList;
        }
    }
}
