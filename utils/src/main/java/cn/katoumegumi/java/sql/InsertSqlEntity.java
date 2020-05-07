package cn.katoumegumi.java.sql;

import java.lang.reflect.Field;
import java.util.List;

/***
 * @author 王松
 */
public class InsertSqlEntity {

    private String insertSql;
    private List<Field> usedField;
    private List<List> valueList;

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public List<Field> getUsedField() {
        return usedField;
    }

    public void setUsedField(List<Field> usedField) {
        this.usedField = usedField;
    }

    public List<List> getValueList() {
        return valueList;
    }

    public void setValueList(List<List> valueList) {
        this.valueList = valueList;
    }
}
