package cn.katoumegumi.java.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ws
 * 对象数据库列名关系集合
 */
public class FieldColumnRelationMapper {
    private String baseSql;
    private String nickName;
    private String tableName;
    private Class<?> clazz;
    private List<FieldColumnRelation> idSet = new ArrayList<>();
    private List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();
    private List<FieldJoinClass> fieldJoinClasses = new ArrayList<>();
    private Map<String, FieldColumnRelation> fieldColumnRelationMap = new HashMap<>();
    private Map<String, FieldColumnRelationMapper> map;


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
        /*for(FieldColumnRelation fieldColumnRelation:idSet){
            if(fieldColumnRelation.getFieldName().equals(fieldName)){
                return fieldColumnRelation;
            }
        }
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelations){
            if(fieldColumnRelation.getFieldName().equals(fieldName)){
                return fieldColumnRelation;
            }
        }*/
        FieldColumnRelation fieldColumnRelation = fieldColumnRelationMap.get(fieldName);
        if (fieldColumnRelation == null) {
            throw new RuntimeException("未发现对象含有属性：" + fieldName);
        }
        return fieldColumnRelation;
        //return null;
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


    public String getBaseSql() {
        return baseSql;
    }

    public void setBaseSql(String baseSql) {
        this.baseSql = baseSql;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<FieldColumnRelation> getIdSet() {
        return idSet;
    }

    public void setIdSet(List<FieldColumnRelation> idSet) {
        this.idSet = idSet;
    }

    public List<FieldColumnRelation> getFieldColumnRelations() {
        return fieldColumnRelations;
    }

    public void setFieldColumnRelations(List<FieldColumnRelation> fieldColumnRelations) {
        this.fieldColumnRelations = fieldColumnRelations;
    }

    public List<FieldJoinClass> getFieldJoinClasses() {
        return fieldJoinClasses;
    }

    public void setFieldJoinClasses(List<FieldJoinClass> fieldJoinClasses) {
        this.fieldJoinClasses = fieldJoinClasses;
    }

    public Map<String, FieldColumnRelationMapper> getMap() {
        return map;
    }

    public void setMap(Map<String, FieldColumnRelationMapper> map) {
        this.map = map;
    }

    public Map<String, FieldColumnRelation> getFieldColumnRelationMap() {
        return fieldColumnRelationMap;
    }

    public void setFieldColumnRelationMap(Map<String, FieldColumnRelation> fieldColumnRelationMap) {
        this.fieldColumnRelationMap = fieldColumnRelationMap;
    }

    @Override
    public String toString() {
        return baseSql;
    }
}
