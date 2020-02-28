package cn.katoumegumi.java.sql;

import lombok.Data;

import java.util.*;

/**
 * @author ws
 */
@Data
public class FieldColumnRelationMapper {
    private String baseSql;
    private String nickName;
    private String tableName;
    private Class<?> clazz;
    private List<FieldColumnRelation> idSet = new ArrayList<>();
    private List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();
    private List<FieldJoinClass> fieldJoinClasses = new ArrayList<>();
    private Map<String,FieldColumnRelationMapper> map;




    public FieldColumnRelation getFieldColumnRelationByColumn(String column){
        for(FieldColumnRelation fieldColumnRelation:idSet){
            if(fieldColumnRelation.getColumnName().equals(column)){
                return fieldColumnRelation;
            }
        }
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelations){
            if(fieldColumnRelation.getColumnName().equals(column)){
                return fieldColumnRelation;
            }
        }
        return null;
    }

    public FieldColumnRelation getFieldColumnRelationByField(String fieldName){
        for(FieldColumnRelation fieldColumnRelation:idSet){
            if(fieldColumnRelation.getFieldName().equals(fieldName)){
                return fieldColumnRelation;
            }
        }
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelations){
            if(fieldColumnRelation.getFieldName().equals(fieldName)){
                return fieldColumnRelation;
            }
        }
        return null;
    }


    public FieldJoinClass getFieldJoinClassByColumn(String column){
        for(FieldJoinClass fieldJoinClass:fieldJoinClasses) {
            if (fieldJoinClass.getAnotherJoinColumn().equals(column)) {
                return fieldJoinClass;
            }
        }
        return null;
    }

    public FieldJoinClass getFieldJoinClassByFieldName(String fieldName){
        for(FieldJoinClass fieldJoinClass:fieldJoinClasses){
            if(fieldJoinClass.getNickName().equals(fieldName)){
                return fieldJoinClass;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return baseSql;
    }
}
