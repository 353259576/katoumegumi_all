package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.common.SqlType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * sql语句
 * @author ws
 */
public class SqlEntity {

    private SqlType sqlType;

    /**
     * 需要查询的列的信息
     */
    private final List<ColumnBaseEntity> columnList = new ArrayList<>();

    /**
     * 查询的表的数据
     */
    private final List<String> tableNameList = new ArrayList<>();

    /**
     * 查询条件数据
     */
    private final List<String> conditionList = new ArrayList<>();

    /**
     * 查询语句
     */
    private String columnStr;

    /**
     * 表
     */
    private String tableStr;

    /**
     * 条件
     */
    private String condition;

    /**
     * 附加
     */
    private String subjoin;


    public SqlType getSqlType() {
        return sqlType;
    }

    public SqlEntity setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
        return this;
    }


    public List<String> getTableNameList() {
        return tableNameList;
    }


    public List<String> getConditionList() {
        return conditionList;
    }


    public String getColumnStr() {
        if(columnStr == null){
            columnStr = columnList.stream().map(ColumnBaseEntity::getColumnValue).collect(Collectors.joining(","));
        }
        return columnStr;
    }


    public String getTableStr() {
        if(tableStr == null){
            tableStr = String.join(" ",tableNameList);
        }
        return tableStr;
    }


    public String getCondition() {
        if(condition == null){
            if(WsListUtils.isNotEmpty(conditionList)){
                condition = "where " + String.join(" and ",conditionList);
            }else {
                condition = "";
            }

        }
        return condition;
    }


    public String getSubjoin() {
        if(subjoin == null){
            return "";
        }
        return subjoin;
    }

    public SqlEntity setSubjoin(String subjoin) {
        this.subjoin = subjoin;
        return this;
    }

    public List<ColumnBaseEntity> getColumnList() {
        return columnList;
    }
}
