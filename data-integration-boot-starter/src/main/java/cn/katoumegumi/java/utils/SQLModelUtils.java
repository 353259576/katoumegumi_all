package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.hibernate.MySearch;
import cn.katoumegumi.java.hibernate.MySearchList;
import cn.katoumegumi.java.hibernate.TableRelation;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author ws
 */
@Slf4j
public class SQLModelUtils {

    public static Map<Class<?>,FieldColumnRelationMapper> mapperMap = new HashMap<>();

    private Map<String,FieldColumnRelationMapper> map = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(createJoinSql("nickName","id","jointable","joinnickname","id"));
    }




    public String searchListBaseSQLProcessor(MySearchList mySearchList){
        Map<String,FieldColumnRelationMapper> map = new HashMap<>();
        StringBuilder selectSql = new StringBuilder(modelToSqlSelect(mySearchList.getMainClass()));
        List<TableRelation> list = mySearchList.getJoins();
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(mySearchList.getMainClass());
        String baseTableName = fieldColumnRelationMapper.getNickName();
        for(TableRelation tableRelation:list) {
            selectSql.append(createJoinSql(baseTableName + "." + tableRelation.getTableNickName(), tableRelation.getTableColumn(), baseTableName + "." + tableRelation.getJoinTableName(), tableRelation.getJoinTableNickName(), tableRelation.getJoinTableColumn()));
        }
        if(!(mySearchList.getAll().isEmpty()&&mySearchList.getAnds().isEmpty()&&mySearchList.getOrs().isEmpty())){
            selectSql.append(" where ");
            List<String> whereStrings = searchListWhereSqlProcessor(mySearchList,baseTableName,map);
            selectSql.append(WsStringUtils.jointListString(whereStrings, " and "));
        }
        List<MySearch> orderSearches = mySearchList.getOrderSearches();
        List<String> list1 = new ArrayList<>();
        for(MySearch mySearch:orderSearches){
            list1.add(createWhereColumn(baseTableName,mySearch) +" "+ mySearch.getValue());
        }
        if(list1.size() > 0){
            selectSql.append("order by ")
                    .append(WsStringUtils.jointListString(list1," and "));
        }
        return selectSql.toString();

    }

    public String createWhereColumn(String prefix,MySearch mySearch){
        String tableColumn;
        String prefixString;
        String fieldName;
        FieldColumnRelationMapper mapper;
        if(mySearch.getFieldName().contains(".")){
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append('`');
            StringBuilder fieldPrefix = new StringBuilder();
                    fieldPrefix.append(prefix);
            String[] strs = mySearch.getFieldName().split("[.]");
            int i = 0;
            for(; i < strs.length - 1;i++){
                fieldPrefix.append('.');
                fieldPrefix.append(strs[i]);
            }
            prefixString = fieldPrefix.toString();
            fieldName = strs[i];
            mapper = map.get(prefixString);
            stringBuffer.append(fieldPrefix);
            stringBuffer.append('`');
            stringBuffer.append('.')
                    .append('`')
                    .append(mapper.getFieldColumnRelationByField(fieldName).getColumnName())
                    .append('`');
            tableColumn = stringBuffer.toString();
        }else {
            prefixString = prefix;
            fieldName = mySearch.getFieldName();
            StringBuilder stringBuffer = new StringBuilder();
            mapper = map.get(prefixString);
            stringBuffer.append('`')
                    .append(prefix)
                    .append('`')
                    .append('.')
                    .append('`')
                    .append(mapper.getFieldColumnRelationByField(fieldName).getColumnName())
                    .append('`');
            tableColumn = stringBuffer.toString();
        }
        return tableColumn;
    }


    public List<String> searchListWhereSqlProcessor(MySearchList mySearchList,String prefix,Map<String,FieldColumnRelationMapper> map){
        Iterator<MySearch> iterator = mySearchList.iterator();
        List<String> stringList = new ArrayList<>();
        while (iterator.hasNext()) {
            MySearch mySearch = iterator.next();
            switch (mySearch.getOperator()) {
                case EQ:
                    stringList.add(createWhereColumn(prefix,mySearch) + " = ? ");
                    break;
                case LIKE:
                    stringList.add(createWhereColumn(prefix,mySearch) + " = ? ");
                    break;
                case GT:
                    stringList.add(createWhereColumn(prefix,mySearch) + " > ? ");
                    break;
                case LT:
                    stringList.add(createWhereColumn(prefix,mySearch) + " < ? ");
                    break;
                case GTE:
                    stringList.add(createWhereColumn(prefix,mySearch) + " >= ? ");
                    break;
                case LTE:
                    stringList.add(createWhereColumn(prefix,mySearch) + " <= ? ");
                    break;
                case IN:
                    stringList.add(createWhereColumn(prefix,mySearch) + " in(?) ");
                    break;
                case NIN:
                    stringList.add(createWhereColumn(prefix,mySearch) + " not in(?) ");
                    break;
                case NULL:
                    stringList.add(createWhereColumn(prefix,mySearch) + " is null ");
                    break;
                case NOTNULL:
                    stringList.add(createWhereColumn(prefix,mySearch) + " is not null ");
                    break;
                case NE:
                    stringList.add(createWhereColumn(prefix,mySearch) + " != ? ");
                    break;
                case SORT:
                    break;
                default:
                    break;
            }
        }

        List<MySearchList> ands = mySearchList.getAnds();
        if(!WsListUtils.isEmpty(ands)){
            for(MySearchList searchList:ands){
                List<String> andStrings = searchListWhereSqlProcessor(searchList,prefix,map);
                if(andStrings.size() != 0) {
                    if (andStrings.size() == 1) {
                        stringList.add(WsStringUtils.jointListString(andStrings, " and "));
                    } else {
                        stringList.add("(" + WsStringUtils.jointListString(andStrings, " and ") + ")");
                    }
                }

            }
        }
        List<MySearchList> ors = mySearchList.getOrs();
        if(!WsListUtils.isEmpty(ors)) {
            for (MySearchList searchList : ors) {
                List<String> orStrings = searchListWhereSqlProcessor(searchList,prefix,map);
                if(orStrings.size() != 0) {
                    if (orStrings.size() == 1) {
                        stringList.add(WsStringUtils.jointListString(orStrings, " or "));
                    } else {
                        stringList.add("(" + WsStringUtils.jointListString(orStrings, " or ") + ")");
                    }
                }

            }
        }
        return stringList;
    }




    public static String createOneSelectColumn(String nickName,String columnName,String fieldName){
        return '`' +
                nickName +
                '`' +
                '.' +
                '`' +
                columnName +
                '`' +
                ' ' +
                '`' +
                nickName +
                '.' +
                fieldName +
                '`';
    }

    public static String createJoinSql(String tableNickName,String tableColumn,String joinTableName,String joinTableNickName,String joinColumn){
        return " inner join `" +
                joinTableName +
                '`' +
                ' ' +
                '`' +
                joinTableNickName +
                "` on `" +
                tableNickName +
                '`' +
                '.' +
                '`' +
                tableColumn +
                "` = `" +
                joinTableNickName +
                '`' +
                '.' +
                '`' +
                joinColumn +
                '`';

    }


    public String modelToSqlSelect(Class<?> clazz){

        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(clazz);
        assert fieldColumnRelationMapper != null;
        String tableName = fieldColumnRelationMapper.getTableName();
        String tableNickName = fieldColumnRelationMapper.getNickName();
        map.put(tableNickName,fieldColumnRelationMapper);
        log.info("获取当前表名为："+tableName);
        List<String> list = new ArrayList<>();
        List<String> joinString = new ArrayList<>();
        selectJoin(tableNickName,list,joinString,fieldColumnRelationMapper);

        return "select "+WsStringUtils.jointListString(list,",") + " from "+tableName+" "+fieldColumnRelationMapper.getNickName() + " "+WsStringUtils.jointListString(joinString," ");

    }


    /**
     * 拼接查询
     */
    private void selectJoin(String tableNickName,List<String> selectString,List<String> joinString,FieldColumnRelationMapper fieldColumnRelationMapper){

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
                map.put(lastTableNickName,mapper);
                //joinString.add("inner join " + mapper.getTableName() + " `" + lastTableNickName + "` on `" + tableNickName + "`.`"+fieldJoinClass.getAnotherJoinColumn() + "` = `"+lastTableNickName+"`.`"+fieldJoinClass.getJoinColumn()+"`");
                joinString.add(createJoinSql(tableNickName,fieldJoinClass.getAnotherJoinColumn(),mapper.getTableName(),lastTableNickName,fieldJoinClass.getJoinColumn()));
                selectJoin(lastTableNickName,selectString,joinString,mapper);
            }
        }
    }







    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz){
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


    public static FieldColumnRelationMapper hibernateAnalysisClassRelation(Class<?> clazz){
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        Table table = clazz.getAnnotation(Table.class);
        fieldColumnRelationMapper.setTableName(table.name());
        fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
        log.info("表名为："+table.name());
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;
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
                        fieldJoinClass.setAnotherJoinColumn(name);
                        joinColumn.referencedColumnName();
                        fieldJoinClass.setJoinColumn(joinColumn.referencedColumnName());
                    }
                    fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
                }
            }
        }
        mapperMap.put(clazz,fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }


    public static FieldColumnRelationMapper myatisPlusAnalysisClassRelation(Class<?> clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        TableName table = clazz.getAnnotation(TableName.class);
        fieldColumnRelationMapper.setTableName(table.value());
        fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
        log.info("表名为：" + table.value());
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;
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



