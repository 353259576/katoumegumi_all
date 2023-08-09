package cn.katoumegumi.java.sql.handler.model;

import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;

import java.util.List;

/***
 * @author ws
 */
public class InsertSqlEntity {

    private final String insertSql;

    private List<PropertyColumnRelation> usedField;

    private List<PropertyColumnRelation> idList;

    private List<SqlParameter> valueList;

    public InsertSqlEntity(String insertSql) {
        this.insertSql = insertSql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public List<SqlParameter> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlParameter> valueList) {
        this.valueList = valueList;
    }

    public List<PropertyColumnRelation> getIdList() {
        return idList;
    }

    public void setIdList(List<PropertyColumnRelation> idList) {
        this.idList = idList;
    }

    public List<PropertyColumnRelation> getUsedField() {
        return usedField;
    }

    public void setUsedField(List<PropertyColumnRelation> usedField) {
        this.usedField = usedField;
    }
}
