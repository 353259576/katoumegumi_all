package com.ws.java.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ws.java.common.WsFieldUtils;
import com.ws.java.common.WsStringUtils;
import com.ws.java.hibernate.MySearchList;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ws
 * @date Created by ws on 2019/11/19 11:03
 */
@Slf4j
public class SQLModelUtils {


    public static void main(String[] args) {

    }


    public static String modelToSqlSelect(Class clazz){

        String tableName = getTableName(clazz);
        log.info("获取当前表名为："+tableName);
        Map<String,List<String>> map = getModelSqlIdAndColumn(clazz);
        String ids = WsStringUtils.jointListString(map.get("id"),",");
        String columns = WsStringUtils.jointListString(map.get("column"),",");
        return "select "+ids+","+columns+" from "+tableName;

    }

    public static String modelToSqlInsert(Class clazz){

        String tableName = getTableName(clazz);
        log.info("获取当前表名为："+tableName);
        Map<String,List<String>> map = getModelSqlIdAndColumn(clazz);
        String ids = WsStringUtils.jointListString(map.get("id"),",");
        String columns = WsStringUtils.jointListString(map.get("columns"),",");
        return "select "+ids+","+columns;

    }




    public static String getTableName(Class clazz){
        Annotation annotation = clazz.getAnnotation(TableName.class);
        if(annotation == null){
            throw new RuntimeException("未知的表");
        }
        TableName tableNameA = (TableName)annotation;
        return tableNameA.value();
    }


    public static Map<String,List<String>> getModelSqlIdAndColumn(Class clazz){
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        if(fields == null || fields.length == 0){
            throw new RuntimeException("没有字段");
        }
        List<String> columns = new ArrayList<>(fields.length - 1);
        List<String> ids = new ArrayList<>(1);
        for(Field field : fields){
            TableField tableField = field.getAnnotation(TableField.class);
            if(tableField == null){
                TableId tableId = field.getAnnotation(TableId.class);
                if(tableId == null) {
                    continue;
                }
                if(tableId.value() == null){
                    ids.add(field.getName());
                }else {
                    ids.add(tableId.value());
                }
                continue;

            }
            columns.add(tableField.value());
        }
        Map<String,List<String>> map = new HashMap<>();
        map.put("id",ids);
        map.put("column",columns);
        return map;
    }








}
