package cn.katoumegumi.java.sql.handler.model;

import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelation;

import java.util.List;

/***
 * @author ws
 */
public class InsertSqlEntity {

    private final String insertSql;

    private List<FieldColumnRelation> usedField;

    private List<FieldColumnRelation> idList;

    private List<SqlParameter> valueList;

    public InsertSqlEntity(String insertSql) {
        this.insertSql = insertSql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    /*public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }*/

    public List<SqlParameter> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlParameter> valueList) {
        this.valueList = valueList;
    }

    public List<FieldColumnRelation> getIdList() {
        return idList;
    }

    public void setIdList(List<FieldColumnRelation> idList) {
        this.idList = idList;
    }

    public List<FieldColumnRelation> getUsedField() {
        return usedField;
    }

    public void setUsedField(List<FieldColumnRelation> usedField) {
        this.usedField = usedField;
    }
}
