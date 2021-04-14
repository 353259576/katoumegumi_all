package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.common.SqlCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * sql语句
 *
 * @author ws
 */
public class SqlEntity {

    /**
     * 需要查询的列的信息
     */
    private final List<ColumnBaseEntity> columnList = new ArrayList<>();

    /**
     * 查询的表的数据
     */
    //private final List<String> tableNameList = new ArrayList<>();
    private final List<TableEntity> tableNameList = new ArrayList<>();

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




/*    public List<String> getTableNameList() {
        return tableNameList;
    }*/


    public List<String> getConditionList() {
        return conditionList;
    }

    public List<TableEntity> getTableNameList() {
        return tableNameList;
    }


    public String getColumnStr() {
        if (columnStr == null) {
            if (columnList.size() == 1) {
                columnStr = SqlCommon.DISTINCT + columnList.get(0).getColumnValue();
            } else {
                columnStr = columnList.stream().map(ColumnBaseEntity::getColumnValue).collect(Collectors.joining(SqlCommon.COMMA));
            }

        }
        return columnStr;
    }


    public String getTableStr() {
        if (tableStr == null) {
            StringBuilder sb = new StringBuilder();
            for (TableEntity tableEntity : tableNameList) {
                sb.append(tableEntity.getTableJoinType().getValue()).append(SQLModelUtils.guardKeyword(tableEntity.getTableName()))
                        .append(' ')
                        .append(SQLModelUtils.guardKeyword(tableEntity.getAlias()));
                if (tableEntity.getCondition() != null) {
                    sb.append(tableEntity.getCondition());
                }
            }
            //tableStr = String.join(" ",tableNameList);
            tableStr = sb.toString();
        }
        return tableStr;
    }


    public String getCondition() {
        if (condition == null) {
            if (WsListUtils.isNotEmpty(conditionList)) {
                condition = SqlCommon.WHERE + String.join(SqlCommon.SQL_AND, conditionList);
            } else {
                condition = "";
            }

        }
        return condition;
    }


    public String getSubjoin() {
        if (subjoin == null) {
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
