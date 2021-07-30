package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlWhereValue;

import java.util.List;

/***
 * @author ws
 */
public class UpdateSqlEntity {

    private String updateSql;

    private List<FieldColumnRelation> usedField;

    private List<FieldColumnRelation> idList;

    private List<SqlWhereValue> valueList;


    public String getUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public List<FieldColumnRelation> getUsedField() {
        return usedField;
    }

    public void setUsedField(List<FieldColumnRelation> usedField) {
        this.usedField = usedField;
    }

    public List<FieldColumnRelation> getIdList() {
        return idList;
    }

    public void setIdList(List<FieldColumnRelation> idList) {
        this.idList = idList;
    }

    public List<SqlWhereValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlWhereValue> valueList) {
        this.valueList = valueList;
    }
}
