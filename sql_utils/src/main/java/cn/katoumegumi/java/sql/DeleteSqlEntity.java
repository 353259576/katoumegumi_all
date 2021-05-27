package cn.katoumegumi.java.sql;

import java.util.List;

/**
 * @author ws
 */
public class DeleteSqlEntity {

    private String deleteSql;

    private List valueList;

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    public List getValueList() {
        return valueList;
    }

    public void setValueList(List valueList) {
        this.valueList = valueList;
    }
}
