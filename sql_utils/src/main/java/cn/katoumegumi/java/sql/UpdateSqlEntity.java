package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlParameter;

import java.util.List;

/***
 * @author ws
 */
public class UpdateSqlEntity {

    private String updateSql;

    private List<FieldColumnRelation> usedField;

    private List<FieldColumnRelation> idList;

    private List<SqlParameter> valueList;


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

    public List<SqlParameter> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlParameter> valueList) {
        this.valueList = valueList;
    }
}
