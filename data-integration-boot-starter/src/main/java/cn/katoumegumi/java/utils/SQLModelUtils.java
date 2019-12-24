package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.hibernate.MySearchList;
import cn.katoumegumi.java.hibernate.TableRelation;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
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

    public static Map<Class,FieldColumnRelationMapper> mapperMap = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(createJoinSql("nickName","id","jointable","joinnickname","id"));
    }




    public static void MySearchListBaseSQLProcessor(MySearchList mySearchList){
        String selectSql = modelToSqlSelect(mySearchList.getMainClass());
        List<TableRelation> list = mySearchList.getJoins();
        String baseTableName = mySearchList.getMainClass().getName();
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(mySearchList.getMainClass().getName());
        for(TableRelation tableRelation:list){
            selectSql += " inner join  `"+ tableRelation.getJoinTableName()+"` `" + tableRelation.getJoinTableNickName()+"` on `";
        }
    }




    public static String createOneSelectColumn(String nickName,String columnName,String fieldName){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('`')
                .append(nickName)
                .append('`')
                .append('.')
                .append('`')
                .append(columnName)
                .append('`')
                .append(' ')
                .append('`')
                .append(nickName)
                .append('.')
                .append(fieldName)
                .append('`');
        return stringBuffer.toString();
    }

    public static String createJoinSql(String tableNickName,String tableColumn,String joinTableName,String joinTableNickName,String joinColumn){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" inner join `")
                .append(joinTableName)
                .append('`')
                .append(' ')
                .append('`')
                .append(joinTableNickName)
                .append("` on `")
                .append(tableNickName)
                .append('`')
                .append('.')
                .append('`')
                .append(tableColumn)
                .append("` = `")
                .append(joinTableNickName)
                .append('`')
                .append('.')
                .append('`')
                .append(joinColumn)
                .append('`');
        return stringBuffer.toString();

    }


    public static String modelToSqlSelect(Class clazz){

        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(clazz);
        String tableName = fieldColumnRelationMapper.getTableName();
        String tableNickName = fieldColumnRelationMapper.getNickName();
        log.info("获取当前表名为："+tableName);
        List<String> list = new ArrayList<>();
        List<String> joinString = new ArrayList<>();
        selectJoin(tableNickName,list,joinString,fieldColumnRelationMapper);

        return "select "+WsStringUtils.jointListString(list,",") + " from "+tableName+" "+fieldColumnRelationMapper.getNickName() + " "+WsStringUtils.jointListString(joinString," ");

    }


    /**
     * 拼接查询
     * @param tableNickName
     * @param selectString
     * @param joinString
     * @param fieldColumnRelationMapper
     */
    private static void selectJoin(String tableNickName,List<String> selectString,List<String> joinString,FieldColumnRelationMapper fieldColumnRelationMapper){

        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelationMapper.getIdSet()){
            selectString.add(createOneSelectColumn(tableNickName,fieldColumnRelation.getColumnName(),fieldColumnRelation.getFieldName()));
        }
        for(FieldColumnRelation fieldColumnRelation:fieldColumnRelationMapper.getFieldColumnRelations()) {
            selectString.add(createOneSelectColumn(tableNickName, fieldColumnRelation.getColumnName(), fieldColumnRelation.getFieldName()));
        }
        String lastTableNickName;
        if(!fieldColumnRelationMapper.getFieldJoinClasses().isEmpty()){
            for (FieldJoinClass fieldJoinClass:fieldColumnRelationMapper.getFieldJoinClasses()){
                lastTableNickName = tableNickName+'.'+fieldJoinClass.getNickName();
                FieldColumnRelationMapper mapper = mapperMap.get(fieldJoinClass.getJoinClass());
                //joinString.add("inner join " + mapper.getTableName() + " `" + lastTableNickName + "` on `" + tableNickName + "`.`"+fieldJoinClass.getAnotherJoinColumn() + "` = `"+lastTableNickName+"`.`"+fieldJoinClass.getJoinColumn()+"`");
                joinString.add(createJoinSql(tableNickName,fieldJoinClass.getAnotherJoinColumn(),mapper.getTableName(),lastTableNickName,fieldJoinClass.getJoinColumn()));
                selectJoin(lastTableNickName,selectString,joinString,mapper);
            }
        }
    }







    public static FieldColumnRelationMapper analysisClassRelation(Class clazz){
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(clazz);
        if(fieldColumnRelationMapper != null){
            return fieldColumnRelationMapper;
        }
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
        fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
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
                if(analysisClassRelation(field.getType()) != null) {
                    FieldJoinClass fieldJoinClass = new FieldJoinClass();
                    fieldJoinClass.setNickName(field.getName());
                    fieldJoinClass.setJoinClass(field.getType());
                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    if(joinColumn != null){
                        String name = joinColumn.name();
                        if(name != null){
                            fieldJoinClass.setAnotherJoinColumn(name);
                        }
                        if(joinColumn.referencedColumnName() != null){
                            fieldJoinClass.setJoinColumn(joinColumn.referencedColumnName());
                        }
                    }
                    fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
                }
            }
        }
        mapperMap.put(clazz,fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }


    public static FieldColumnRelationMapper myatisPlusAnalysisClassRelation(Class clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        TableName table = (TableName) clazz.getAnnotation(TableName.class);
        fieldColumnRelationMapper.setTableName(table.value());
        fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
        log.info("表名为：" + table.value());
        Field fields[] = WsFieldUtils.getFieldAll(clazz);
        for (Field field : fields) {
            if (WsBeanUtis.isBaseType(field.getType())) {
                boolean isId = false;
                TableId id = field.getAnnotation(TableId.class);
                if (id != null) {
                    isId = true;
                }
                TableField column = field.getAnnotation(TableField.class);
                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation();
                fieldColumnRelation.setFieldClass(field.getType());
                fieldColumnRelation.setFieldName(field.getName());
                if (column == null) {
                    fieldColumnRelation.setColumnName(WsStringUtils.camel_case(field.getName()));
                } else {
                    fieldColumnRelation.setColumnName(column.value());
                }
                fieldColumnRelation.setId(isId);
                if (isId) {
                    fieldColumnRelationMapper.getIdSet().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            } else {
                if(analysisClassRelation(field.getType()) != null){
                    FieldJoinClass fieldJoinClass = new FieldJoinClass();
                    fieldJoinClass.setNickName(field.getName());
                    fieldJoinClass.setJoinClass(field.getType());
                    fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
                }
            }
        }
        mapperMap.put(clazz, fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }






}



