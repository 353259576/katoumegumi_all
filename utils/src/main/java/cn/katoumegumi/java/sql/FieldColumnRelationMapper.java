package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ws
 * 对象数据库列名关系集合
 */
public class FieldColumnRelationMapper {
    private final String nickName;
    private final String tableName;
    private final Class<?> clazz;

    /**
     * id
     */
    private final List<FieldColumnRelation> idSet = new ArrayList<>();

    /**
     * 非Id
     */
    private final List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();

    /**
     * 关联对象
     */
    private final List<FieldJoinClass> fieldJoinClasses = new ArrayList<>();
    private final Map<String, FieldColumnRelation> fieldColumnRelationMap = new HashMap<>();

    /**
     * 位置
     */
    private final Map<Object,Integer> locationMap = new HashMap<>();

    private Map<String, FieldColumnRelationMapper> map;


    public FieldColumnRelationMapper(String nickName,String tableName,Class<?> clazz){
        this.nickName = nickName;
        this.tableName = tableName;
        this.clazz = clazz;
    }


    public FieldColumnRelation getFieldColumnRelationByColumn(String column) {
        for (FieldColumnRelation fieldColumnRelation : idSet) {
            if (fieldColumnRelation.getColumnName().equals(column)) {
                return fieldColumnRelation;
            }
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelations) {
            if (fieldColumnRelation.getColumnName().equals(column)) {
                return fieldColumnRelation;
            }
        }
        return null;
    }

    public FieldColumnRelation getFieldColumnRelationByField(String fieldName) {
        FieldColumnRelation fieldColumnRelation = fieldColumnRelationMap.get(fieldName);
        if (fieldColumnRelation == null) {
            throw new RuntimeException("未发现对象含有属性：" + fieldName);
        }
        return fieldColumnRelation;
    }


    public FieldJoinClass getFieldJoinClassByColumn(String column) {
        for (FieldJoinClass fieldJoinClass : fieldJoinClasses) {
            if (fieldJoinClass.getAnotherJoinColumn().equals(column)) {
                return fieldJoinClass;
            }
        }
        return null;
    }

    public FieldJoinClass getFieldJoinClassByFieldName(String fieldName) {
        for (FieldJoinClass fieldJoinClass : fieldJoinClasses) {
            if (fieldJoinClass.getNickName().equals(fieldName)) {
                return fieldJoinClass;
            }
        }
        return null;
    }



    public String getNickName() {
        return nickName;
    }

    /*public void setNickName(String nickName) {
        this.nickName = nickName;
    }*/

    public String getTableName() {
        return tableName;
    }

    /*public void setTableName(String tableName) {
        this.tableName = tableName;
    }*/

    public Class<?> getClazz() {
        return clazz;
    }

    /*public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }*/

    public List<FieldColumnRelation> getIdSet() {
        return idSet;
    }

    /*public void setIdSet(List<FieldColumnRelation> idSet) {
        this.idSet = idSet;
    }*/

    public List<FieldColumnRelation> getFieldColumnRelations() {
        return fieldColumnRelations;
    }

   /* public void setFieldColumnRelations(List<FieldColumnRelation> fieldColumnRelations) {
        this.fieldColumnRelations = fieldColumnRelations;
    }*/

    public List<FieldJoinClass> getFieldJoinClasses() {
        return fieldJoinClasses;
    }

    /*public void setFieldJoinClasses(List<FieldJoinClass> fieldJoinClasses) {
        this.fieldJoinClasses = fieldJoinClasses;
    }*/

    public Map<String, FieldColumnRelationMapper> getMap() {
        return map;
    }

    public void setMap(Map<String, FieldColumnRelationMapper> map) {
        this.map = map;
    }

    public Map<String, FieldColumnRelation> getFieldColumnRelationMap() {
        return fieldColumnRelationMap;
    }

    /*public void setFieldColumnRelationMap(Map<String, FieldColumnRelation> fieldColumnRelationMap) {
        this.fieldColumnRelationMap = fieldColumnRelationMap;
    }*/

    public void markSignLocation(){
        for(int i = 0; i < idSet.size(); i++){
            locationMap.put(idSet.get(i),i);
        }
        for(int i = 0; i < fieldColumnRelations.size(); i++){
            locationMap.put(fieldColumnRelations.get(i),i);
        }
        if(WsListUtils.isNotEmpty(fieldJoinClasses)){
            for(int i = 0; i < fieldJoinClasses.size(); i++){
                locationMap.put(fieldJoinClasses.get(i),i);
            }
        }
    }

    public Integer getLocation(Object o){
        return locationMap.get(o);
    }

    @Override
    public String toString() {
        return nickName+"_"+tableName;
    }
}
