package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlWhereValue;

import java.util.List;

/**
 * @author ws
 */
public class DeleteSqlEntity {

    private String deleteSql;

    private List<SqlWhereValue> valueList;

    public String getDeleteSql() {
        return deleteSql;
    }

    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    public List<SqlWhereValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlWhereValue> valueList) {
        this.valueList = valueList;
    }
}
