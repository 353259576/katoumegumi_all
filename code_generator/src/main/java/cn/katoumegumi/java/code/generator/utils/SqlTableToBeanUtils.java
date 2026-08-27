package cn.katoumegumi.java.code.generator.utils;

import cn.katoumegumi.java.common.WsStringUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        throw new NullPointerException("获取数据库连接失败");
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
            throw new RuntimeException("查询表字段失败: " + tableName, throwables);
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
                if (WsStringUtils.isNotBlank(prefix) && name.startsWith(prefix)) {
                    entityName = WsStringUtils.camelCase(name.substring(prefix.length()));
                    if (!entityName.isEmpty()) {
                        entityName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
                    }
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
        private static final Map<String, Class<?>> classMap = new HashMap<>();

        static {
            classMap.put("varchar", String.class);
            classMap.put("char", String.class);
            classMap.put("text", String.class);
            classMap.put("mediumtext", String.class);
            classMap.put("longtext", String.class);
            classMap.put("json", String.class);
            classMap.put("int", Integer.class);
            classMap.put("int unsigned", Integer.class);
            classMap.put("mediumint", Integer.class);
            classMap.put("bigint", Long.class);
            classMap.put("bigint unsigned", Long.class);
            classMap.put("smallint", Short.class);
            classMap.put("float", Float.class);
            classMap.put("double", Double.class);
            classMap.put("decimal", BigDecimal.class);
            classMap.put("tinyint", Integer.class);
            classMap.put("bool", Boolean.class);
            classMap.put("boolean", Boolean.class);
            classMap.put("blob", String.class);
            classMap.put("timestamp", Date.class);
            classMap.put("datetime", Date.class);
            classMap.put("date", LocalDate.class);

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
            this.entityName = firstLowerEntityName.isEmpty()
                    ? ""
                    : Character.toUpperCase(firstLowerEntityName.charAt(0)) + firstLowerEntityName.substring(1);
            this.firstLowerEntityName = firstLowerEntityName;
            this.columnList = columnList;
            this.classList = columnList.stream().map(Column::getColumnClass).distinct().collect(Collectors.toList());
            for (Column column : columnList) {
                if ("PRI".equals(column.getColumnKey())) {
                    pkColumn = column;
                    break;
                }
            }
            if (pkColumn == null && !columnList.isEmpty()) {
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
