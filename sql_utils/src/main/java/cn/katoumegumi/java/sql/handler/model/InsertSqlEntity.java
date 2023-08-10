package cn.katoumegumi.java.sql.handler.model;

import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;

import java.util.List;

/***
 * @author ws
 */
public class InsertSqlEntity {

    private final String insertSql;

    private List<PropertyBaseColumnRelation> usedField;

    private List<PropertyBaseColumnRelation> idList;

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

    public List<PropertyBaseColumnRelation> getIdList() {
        return idList;
    }

    public void setIdList(List<PropertyBaseColumnRelation> idList) {
        this.idList = idList;
    }

    public List<PropertyBaseColumnRelation> getUsedField() {
        return usedField;
    }

    public void setUsedField(List<PropertyBaseColumnRelation> usedField) {
        this.usedField = usedField;
    }
}
