package cn.katoumegumi.java.code.generator.utils;

import cn.katoumegumi.java.common.WsStringUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * mysql table转换为java bean
 *
 * @author ws
 */
public class SqlTableToBeanUtils {

    private final DataSource dataSource;

    private final String dbName;

    private final String prefix;


    public SqlTableToBeanUtils(DataSource dataSource, String dbName, String prefix) {
        this.dataSource = dataSource;
        this.dbName = dbName;
        this.prefix = prefix;
    }


    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    public List<Column> selectTableColumns(String tableName) {

        String sql = "select COLUMN_NAME,COLUMN_COMMENT,COLUMN_TYPE,COLUMN_KEY from INFORMATION_SCHEMA.Columns WHERE TABLE_SCHEMA = ? AND TABLE_NAME=?";
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, dbName);
            preparedStatement.setString(2, tableName);
            resultSet = preparedStatement.executeQuery();
            List<Column> columnList = new ArrayList<>();
            while (resultSet.next()) {
                String colName = resultSet.getString("COLUMN_NAME");
                String colRemark = resultSet.getString("COLUMN_COMMENT");
                String colType = resultSet.getString("COLUMN_TYPE");
                String colKey = resultSet.getString("COLUMN_KEY");
                Column column = new Column(colName, colRemark, colType, colKey);
                columnList.add(column);
            }
            return columnList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        } finally {
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if(preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }

    public List<Table> selectTables(String tableName) {

        String sql = "select TABLE_SCHEMA,TABLE_NAME,TABLE_COMMENT from INFORMATION_SCHEMA.`TABLES` WHERE TABLE_SCHEMA = ?";
        if (WsStringUtils.isNotBlank(tableName)) {
            sql += " and TABLE_NAME = ?";
        }
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, dbName);
            if (WsStringUtils.isNotBlank(tableName)) {
                preparedStatement.setString(2, tableName);
            }
            resultSet = preparedStatement.executeQuery();
            List<Table> tableList = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("TABLE_NAME");
                String remark = resultSet.getString("TABLE_COMMENT");
                String entityName = WsStringUtils.camelCase(name);
                if (WsStringUtils.isNotBlank(prefix) && entityName.startsWith(prefix)) {
                    entityName = entityName.substring(prefix.length());
                    entityName = entityName.substring(0, 1).toLowerCase() + entityName.substring(1);
                }
                Table table = new Table(name, remark, entityName, selectTableColumns(name));
                tableList.add(table);
            }
            return tableList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        } finally {
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if(preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }


    public static class Column {
        private static final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

        static {
            classMap.put("varchar", String.class);
            classMap.put("int", Integer.class);
            classMap.put("int unsigned", Integer.class);
            classMap.put("bigint", Long.class);
            classMap.put("float", Float.class);
            classMap.put("double", Double.class);
            classMap.put("decimal", BigDecimal.class);
            classMap.put("tinyint", Integer.class);
            classMap.put("blob", String.class);
            classMap.put("timestamp", Date.class);
            classMap.put("datetime", Date.class);

        }

        /**
         * 字段名
         */
        private final String columnName;
        /**
         * 字段备注
         */
        private final String columnRemark;
        /**
         * 字段类型
         */
        private final String columnType;
        /**
         * 键类型
         */
        private final String columnKey;
        /**
         * java bean field名称
         */
        private final String beanFieldName;
        private Class<?> columnClass;

        public Column(String columnName, String columnRemark, String columnType, String columnKey) {
            this.columnName = columnName;
            this.columnRemark = columnRemark;
            this.columnKey = columnKey;
            this.beanFieldName = WsStringUtils.camelCase(columnName);
            if (WsStringUtils.isNotBlank(columnType)) {
                int index = columnType.indexOf("(");
                if (index > 0) {
                    columnType = columnType.substring(0, index);
                }
            }
            this.columnType = columnType;
            this.columnClass = classMap.get(columnType);
            if (this.columnClass == null) {
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

    public static class Table {
        private final String tableName;

        private final String tableRemark;

        private final String entityName;

        private final String firstLowerEntityName;
        private final List<Class<?>> classList;
        private final List<Column> columnList;
        private Column pkColumn;

        public Table(String tableName, String tableRemark, String firstLowerEntityName, List<Column> columnList) {
            this.tableName = tableName;
            this.tableRemark = tableRemark;
            this.entityName = firstLowerEntityName.substring(0, 1).toUpperCase() + firstLowerEntityName.substring(1);
            this.firstLowerEntityName = firstLowerEntityName;
            this.columnList = columnList;
            this.classList = columnList.stream().map(Column::getColumnClass).distinct().collect(Collectors.toList());
            for (Column column : columnList) {
                if (column.getColumnKey().equals("PRI")) {
                    pkColumn = column;
                    break;
                }
            }
            if (pkColumn == null) {
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
