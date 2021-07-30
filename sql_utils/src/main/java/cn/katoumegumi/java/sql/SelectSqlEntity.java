package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlWhereValue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/***
 * @author 王松
 */
public class SelectSqlEntity {

    private String selectSql;

    private String countSql;

    private List<SqlWhereValue> valueList;

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public String getCountSql() {
        return countSql;
    }

    public void setCountSql(String countSql) {
        this.countSql = countSql;
    }

    public List<SqlWhereValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlWhereValue> valueList) {
        this.valueList = valueList;
    }

    public Map<Integer, Object> getValueMap() {
        Map<Integer, Object> map = new TreeMap<>();
        for (int i = 0; i < valueList.size(); i++) {
            map.put(i, valueList.get(i));
        }
        return map;
    }
}
