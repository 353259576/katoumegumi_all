package cn.katoumegumi.java.sql.handler.model;

import java.util.List;

/***
 * @author ws
 */
public class UpdateSqlEntity {

    private String updateSql;

    private List<SqlParameter> valueList;


    public String getUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public List<SqlParameter> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlParameter> valueList) {
        this.valueList = valueList;
    }
}
