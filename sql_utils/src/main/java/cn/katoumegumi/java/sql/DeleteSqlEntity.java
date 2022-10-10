package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlParameter;

import java.util.List;

/**
 * @author ws
 */
public class DeleteSqlEntity {

    private final String deleteSql;

    private final List<SqlParameter> valueList;

    public DeleteSqlEntity(String deleteSql, List<SqlParameter> valueList) {
        this.deleteSql = deleteSql;
        this.valueList = valueList;
    }

    public String getDeleteSql() {
        return deleteSql;
    }


    public List<SqlParameter> getValueList() {
        return valueList;
    }

}
