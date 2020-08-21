package cn.katoumegumi.java.vertx.sql.utils;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.entity.SelectCallable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.util.*;

/**
 * 项目里sql语句生成器的工具类
 * @author ws
 */
public class SqlUtils {


    public static <T> Handler<AsyncResult<ResultSet>> getVertxHandler(SelectCallable<T> selectCallable, SQLModelUtils sqlModelUtils){
        return asyncResult->{
            if(asyncResult.succeeded()){
                ResultSet resultSet = asyncResult.result();
                List<String> columnNameList = resultSet.getColumnNames();
                List<JsonArray> list = resultSet.getResults();
                int size = list.size();
                int columnSize = columnNameList.size();
                int mapSize = WsBeanUtils.objectToT((columnSize/0.75),int.class) + 1;
                int j = 0;
                Object o = null;
                List<Map> mapList = new ArrayList<>(list.size());
                for (JsonArray jsonArray : list) {
                    Iterator<?> iterator = jsonArray.iterator();
                    Map map = new HashMap(mapSize);
                    while (iterator.hasNext()) {
                        o = iterator.next();
                        if (o != null) {
                            map.put(columnNameList.get(j), o);
                        }
                        j++;
                    }
                    mapList.add(map);
                    j = 0;
                }
                mapList = sqlModelUtils.handleMap(mapList);
                mapList = sqlModelUtils.mergeMapList(mapList);
                List<T> valueList = sqlModelUtils.loadingObject(mapList);
                selectCallable.put(valueList);
            }
        };
    }

}
