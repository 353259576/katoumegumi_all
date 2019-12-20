package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ws
 */
@Slf4j
public class SQLModelUtils {


    public static void main(String[] args) {

    }






    public static String modelToSqlSelect(Class clazz){

        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(clazz);
        String tableName = fieldColumnRelationMapper.getTableName();
        log.info("获取当前表名为："+tableName);
        List<String> list = new ArrayList<>();
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelationMapper.getIdSet()){
            list.add(fieldColumnRelation.getColumnName());
        }
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelationMapper.getFieldColumnRelations()){
            list.add(fieldColumnRelation.getColumnName());
        }

        return "select "+WsStringUtils.jointListString(list,",") + " from "+tableName;

    }







    public static FieldColumnRelationMapper analysisClassRelation(Class clazz){
        Annotation annotation = clazz.getAnnotation(Entity.class);
        if(annotation != null){
            return hibernateAnalysisClassRelation(clazz);
        }
        annotation = clazz.getAnnotation(TableName.class);
        if(annotation != null){
            return myatisPlusAnalysisClassRelation(clazz);
        }
        return null;
    }


    public static FieldColumnRelationMapper hibernateAnalysisClassRelation(Class clazz){
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        Table table = (Table) clazz.getAnnotation(Table.class);
        fieldColumnRelationMapper.setTableName(table.name());
        log.info("表名为："+table.name());
        Field fields[] = WsFieldUtils.getFieldAll(clazz);
        for (Field field : fields){
            if(WsBeanUtis.isBaseType(field.getType())){
                boolean isId = false;
                Id id = field.getAnnotation(Id.class);
                if(id != null){
                    isId = true;
                }
                Column column = field.getAnnotation(Column.class);
                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation();
                fieldColumnRelation.setFieldClass(field.getType());
                fieldColumnRelation.setFieldName(field.getName());
                if(column == null){
                    fieldColumnRelation.setColumnName(WsStringUtils.camel_case(field.getName()));
                }else {
                    fieldColumnRelation.setColumnName(column.name());
                }
                fieldColumnRelation.setId(isId);
                if(isId){
                    fieldColumnRelationMapper.getIdSet().add(fieldColumnRelation);
                }else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            }else {
                fieldColumnRelationMapper.getJoinMap().put(field.getName(),field.getType());
            }
        }
        return fieldColumnRelationMapper;
    }


    public static FieldColumnRelationMapper myatisPlusAnalysisClassRelation(Class clazz){
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        TableName table = (TableName) clazz.getAnnotation(TableName.class);
        fieldColumnRelationMapper.setTableName(table.value());
        log.info("表名为："+table.value());
        Field fields[] = WsFieldUtils.getFieldAll(clazz);
        for (Field field : fields){
            if(WsBeanUtis.isBaseType(field.getType())){
                boolean isId = false;
                TableId id = field.getAnnotation(TableId.class);
                if(id != null){
                    isId = true;
                }
                TableField column = field.getAnnotation(TableField.class);
                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation();
                fieldColumnRelation.setFieldClass(field.getType());
                fieldColumnRelation.setFieldName(field.getName());
                if(column == null){
                    fieldColumnRelation.setColumnName(WsStringUtils.camel_case(field.getName()));
                }else {
                    fieldColumnRelation.setColumnName(column.value());
                }
                fieldColumnRelation.setId(isId);
                if(isId){
                    fieldColumnRelationMapper.getIdSet().add(fieldColumnRelation);
                }else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            }else {
                fieldColumnRelationMapper.getJoinMap().put(field.getName(),field.getType());
            }
        }
        return fieldColumnRelationMapper;
    }


}



