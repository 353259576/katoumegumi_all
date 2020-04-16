package cn.katoumegumi.java.sql;

import javax.persistence.criteria.JoinType;

/**
 * @author ws
 */

public class TableRelation {

    private JoinType joinType;

    private Class<?> joinTableClass;

    private String tableNickName;

    private String tableColumn;

    private String joinTableName;

    private String joinTableNickName;

    private String joinTableColumn;


    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public Class<?> getJoinTableClass() {
        return joinTableClass;
    }

    public void setJoinTableClass(Class<?> joinTableClass) {
        this.joinTableClass = joinTableClass;
    }

    public String getTableNickName() {
        return tableNickName;
    }

    public void setTableNickName(String tableNickName) {
        this.tableNickName = tableNickName;
    }

    public String getTableColumn() {
        return tableColumn;
    }

    public void setTableColumn(String tableColumn) {
        this.tableColumn = tableColumn;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    public String getJoinTableNickName() {
        return joinTableNickName;
    }

    public void setJoinTableNickName(String joinTableNickName) {
        this.joinTableNickName = joinTableNickName;
    }

    public String getJoinTableColumn() {
        return joinTableColumn;
    }

    public void setJoinTableColumn(String joinTableColumn) {
        this.joinTableColumn = joinTableColumn;
    }
}
