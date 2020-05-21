package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;

import java.util.*;

/**
 * @author ws
 */
public class ResultMapIds {

    private List<Object> set;

    private String md5;

    /**
     * 构造函数
     *
     * @param map    map对象
     * @param idList 使用关键词合并对象 使用全部传空
     */
    public ResultMapIds(Map map, List<String> idList) {
        set = new ArrayList<>(map.size() << 1);
        if (idList == null) {
            Set<Map.Entry> entities = map.entrySet();
            Iterator<Map.Entry> iterable = entities.iterator();
            while (iterable.hasNext()) {
                Map.Entry entry = iterable.next();
                Object o = entry.getValue();
                if (o != null) {
                    if (WsBeanUtils.isBaseType(o.getClass())) {
                        set.add(entry.getKey());
                        set.add(entry.getValue());
                    }
                }
            }
        } else {
            for (String idName : idList) {
                Object o = map.get(idName);
                if (o != null) {
                    set.add(idName);
                    set.add(o);
                }
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof ResultMapIds) {
            List set2 = ((ResultMapIds) o).getSet();
            if (set2.size() != set.size()) {
                return false;
            }
            if (set.isEmpty() && set2.isEmpty()) {
                return true;
            }
            Iterator iterator1 = set.iterator();
            Iterator iterator2 = set.iterator();
            while (iterator1.hasNext()) {
                if (!iterator1.next().equals(iterator2.next())) {
                    return false;
                }
            }
            return true;
            //return md5.equals(((ResultMapIds) o).md5);

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //return md5.hashCode();
        return Arrays.hashCode(set.toArray());

    }

    public List<Object> getSet() {
        return set;
    }

    public void setSet(List<Object> set) {
        this.set = set;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
