package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.entity.SqlParameter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/***
 * @author ws
 */
public class SelectSqlEntity {

    private String selectSql;

    private String countSql;

    private List<SqlParameter> valueList;

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

    public List<SqlParameter> getValueList() {
        return valueList;
    }

    public void setValueList(List<SqlParameter> valueList) {
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
