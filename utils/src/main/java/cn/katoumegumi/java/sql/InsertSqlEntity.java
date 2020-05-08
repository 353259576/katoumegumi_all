package cn.katoumegumi.java.sql;

import java.lang.reflect.Field;
import java.util.List;

/***
 * @author 王松
 */
public class InsertSqlEntity {

    private String insertSql;

    private List<FieldColumnRelation> usedField;

    private List<FieldColumnRelation> idList;

    private List valueList;

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public List getValueList() {
        return valueList;
    }

    public void setValueList(List valueList) {
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
