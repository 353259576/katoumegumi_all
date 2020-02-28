package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.Encryption;
import cn.katoumegumi.java.common.WsBeanUtis;
import com.alibaba.fastjson.JSON;
import lombok.Data;

import javax.swing.text.html.parser.Entity;
import java.util.*;

/**
 * @author ws
 */
@Data
public class ResultMapIds {

    private List<Object> set;

    private String md5;


    public ResultMapIds(Map map){
        set = new ArrayList<>(map.size() << 1);
        Set<Map.Entry> entities = map.entrySet();
        Iterator<Map.Entry> iterable = entities.iterator();
        while (iterable.hasNext()){
            Map.Entry entry = iterable.next();
            Object o = entry.getValue();
            if(o != null){
                if(WsBeanUtis.isBaseType(o.getClass())){
                    set.add(entry.getKey());
                    set.add(entry.getValue());
                }
            }
        }
        //md5 = Encryption.md5Encoder(JSON.toJSONString(set));

        /*Collection collection = map.values();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()){
            Object o = iterator.next();
            if(o != null){
                if(WsBeanUtis.isBaseType(o.getClass())){
                    set.add(o);
                }
            }
        }*/
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof ResultMapIds){
            List set2 = ((ResultMapIds) o).getSet();
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
            //return md5.equals(((ResultMapIds) o).md5);

        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //return md5.hashCode();
        return Arrays.hashCode(set.toArray());

    }
}
