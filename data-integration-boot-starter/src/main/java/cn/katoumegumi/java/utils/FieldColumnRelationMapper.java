package cn.katoumegumi.java.utils;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author ws
 */
@Data
public class FieldColumnRelationMapper {
    private String nickName;
    private String tableName;
    private List<FieldColumnRelation> idSet = new ArrayList<>();
    private List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();
    private List<FieldJoinClass> fieldJoinClasses = new ArrayList<>();




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





}
