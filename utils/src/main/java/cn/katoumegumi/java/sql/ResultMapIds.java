package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtis;
import lombok.Data;

import java.util.*;

/**
 * @author ws
 */
@Data
public class ResultMapIds {

    private Set<Object> set = new HashSet<>();


    public ResultMapIds(Map map){
        Collection collection = map.values();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()){
            Object o = iterator.next();
            if(WsBeanUtis.isBaseType(o.getClass())){
                set.add(o);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof ResultMapIds){
            Set set2 = ((ResultMapIds) o).getSet();
            if(set2.size() != set.size()){
                return false;
            }
            if(set.isEmpty() && set2.isEmpty()){
                return true;
            }
            Iterator iterator1 = set.iterator();
            Iterator iterator2 = set.iterator();
            while (iterator1.hasNext()){
                if(!iterator1.next().equals(iterator2.next())){
                    return false;
                }
            }
            return true;

        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new ArrayList(set).toArray());

    }
}
