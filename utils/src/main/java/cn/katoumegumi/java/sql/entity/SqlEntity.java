package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.common.SqlType;

import java.util.ArrayList;
import java.util.List;

/**
 * sql语句
 * @author ws
 */
public class SqlEntity {

    private SqlType sqlType;

    private List<String> columnNameList = new ArrayList<>();

    private List<String> tableNameList = new ArrayList<>();

    private List<String> conditionList = new ArrayList<>();

    /**
     * 行
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

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public SqlEntity setColumnNameList(List<String> columnNameList) {
        this.columnNameList = columnNameList;
        return this;
    }

    public List<String> getTableNameList() {
        return tableNameList;
    }

    public SqlEntity setTableNameList(List<String> tableNameList) {
        this.tableNameList = tableNameList;
        return this;
    }

    public List<String> getConditionList() {
        return conditionList;
    }

    public SqlEntity setConditionList(List<String> conditionList) {
        this.conditionList = conditionList;
        return this;
    }

    public String getColumnStr() {
        if(columnStr == null){
            columnStr = String.join(",",columnNameList);
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
}
