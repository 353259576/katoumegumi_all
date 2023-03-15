package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import java.util.HashMap;
import java.util.Map;

public class MapperDictTree {


    private FieldColumnRelationMapper currentMapper;

    private Map<Integer,MapperDictTree> childMap;

    public MapperDictTree() {
        this.childMap = new HashMap<>();
    }

    public void add(int[] mapperNameArray, FieldColumnRelationMapper mapper){
        MapperDictTree current = this;
        int length = mapperNameArray.length;
        for (int mapperName : mapperNameArray) {
            current = current.getChildMap().computeIfAbsent(mapperName, name -> new MapperDictTree());
        }
        current.setCurrentMapper(mapper);
    }


    public FieldColumnRelationMapper getCurrentMapper() {
        return currentMapper;
    }

    public void setCurrentMapper(FieldColumnRelationMapper currentMapper) {
        this.currentMapper = currentMapper;
    }

    public Map<Integer, MapperDictTree> getChildMap() {
        return childMap;
    }

    public void setChildMap(Map<Integer, MapperDictTree> childMap) {
        this.childMap = childMap;
    }
}
