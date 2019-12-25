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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ws
 */
@Slf4j
public class SQLModelUtils {

    public static Map<Class<?>,FieldColumnRelationMapper> mapperMap = new HashMap<>();

    private Map<String,FieldColumnRelationMapper> map = new HashMap<>();

    private Map<Integer, Object> valueMap = new TreeMap<>();

    private AtomicInteger atomicInteger = new AtomicInteger(1);

    public static void main(String[] args) {


        System.out.println(createJoinSql("nickName","id","jointable","joinnickname","id"));
    }




    public String searchListBaseSQLProcessor(MySearchList mySearchList){
        StringBuilder selectSql = new StringBuilder(modelToSqlSelect(mySearchList.getMainClass()));
        List<TableRelation> list = mySearchList.getJoins();
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(mySearchList.getMainClass());
        String baseTableName = fieldColumnRelationMapper.getNickName();
        for(TableRelation tableRelation:list) {
            selectSql.append(createJoinSql(baseTableName + "." + tableRelation.getTableNickName(), tableRelation.getTableColumn(), baseTableName + "." + tableRelation.getJoinTableName(), tableRelation.getJoinTableNickName(), tableRelation.getJoinTableColumn()));
        }
        if(!(mySearchList.getAll().isEmpty()&&mySearchList.getAnds().isEmpty()&&mySearchList.getOrs().isEmpty())){
            selectSql.append(" where ");
            List<String> whereStrings = searchListWhereSqlProcessor(mySearchList,baseTableName);
            selectSql.append(WsStringUtils.jointListString(whereStrings, " and "));
        }
        List<MySearch> orderSearches = mySearchList.getOrderSearches();
        List<String> list1 = new ArrayList<>();
        for(MySearch mySearch:orderSearches){
            list1.add(createWhereColumn(baseTableName,mySearch));
        }
        if(list1.size() > 0){
            selectSql.append(" order by ")
                    .append(WsStringUtils.jointListString(list1,","));
        }
        return selectSql.toString();

    }

    public String createWhereColumn(String prefix,MySearch mySearch){
        StringBuilder tableColumn;
        String prefixString;
        String fieldName;
        FieldColumnRelationMapper mapper;
        FieldColumnRelation fieldColumnRelation;
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
            fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
            stringBuffer.append(fieldPrefix);
            stringBuffer.append('`');
            stringBuffer.append('.')
                    .append('`')
                    .append(fieldColumnRelation.getColumnName())
                    .append('`');
            tableColumn = stringBuffer;
        }else {
            prefixString = prefix;
            fieldName = mySearch.getFieldName();
            StringBuilder stringBuffer = new StringBuilder();
            mapper = map.get(prefixString);
            fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
            stringBuffer.append('`')
                    .append(prefix)
                    .append('`')
                    .append('.')
                    .append('`')
                    .append(fieldColumnRelation.getColumnName())
                    .append('`');
            tableColumn = stringBuffer;
        }





        switch (mySearch.getOperator()) {
            case EQ:
                tableColumn.append(" = ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LIKE:
                tableColumn.append(" like ?");
                valueMap.put(atomicInteger.getAndAdd(1),"%"+WsBeanUtis.objectToT(mySearch.getValue(),String.class)+"%");
                break;
            case GT:
                tableColumn.append(" > ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(),fieldColumnRelation.getFieldClass()));
                break;
            case LT:
                tableColumn.append(" < ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(),fieldColumnRelation.getFieldClass()));
                break;
            case GTE:
                tableColumn.append(" >= ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(),fieldColumnRelation.getFieldClass()));
                break;
            case LTE:
                tableColumn.append(" <= ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(),fieldColumnRelation.getFieldClass()));
                break;
            case IN:
                if(WsFieldUtils.classCompare(mySearch.getValue().getClass(),Collection.class)){
                    Collection collection = (Collection)mySearch.getValue();
                    Iterator iterator = collection.iterator();
                    List<String> symbols = new ArrayList<>();
                    while (iterator.hasNext()){
                        Object o = iterator.next();
                        symbols.add("?");
                        valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(o,fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols,","));
                    tableColumn.append(')');

                }else if(mySearch.getValue().getClass().isArray()){
                    Object[] os = (Object[])mySearch.getValue();
                    List<String> symbols = new ArrayList<>();
                    for (Object o:os) {
                        symbols.add("?");
                        valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(o,fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols,","));
                    tableColumn.append(')');
                }else {
                    throw new RuntimeException("非数组类型");
                }

                break;
            case NIN:
                if(WsFieldUtils.classCompare(mySearch.getValue().getClass(),Collection.class)){
                    Collection collection = (Collection)mySearch.getValue();
                    Iterator iterator = collection.iterator();
                    List<String> symbols = new ArrayList<>();
                    while (iterator.hasNext()){
                        Object o = iterator.next();
                        symbols.add("?");
                        valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(o,fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" not in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols,","));
                    tableColumn.append(')');

                }else if(mySearch.getValue().getClass().isArray()){
                    Object[] os = (Object[])mySearch.getValue();
                    List<String> symbols = new ArrayList<>();
                    for (Object o:os) {
                        symbols.add("?");
                        valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(o,fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" no in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols,","));
                    tableColumn.append(')');
                }else {
                    throw new RuntimeException("非数组类型");
                }
                break;
            case NULL:
                tableColumn.append(" is null");
                break;
            case NOTNULL:
                tableColumn.append(" is not null");
                break;
            case NE:
                tableColumn.append(" != ?");
                valueMap.put(atomicInteger.getAndAdd(1),WsBeanUtis.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case SORT:
                tableColumn.append(' ');
                tableColumn.append(mySearch.getValue());
                break;
            default:
                throw new RuntimeException("未知的方式");
        }
        return tableColumn.toString();
    }


    public List<String> searchListWhereSqlProcessor(MySearchList mySearchList,String prefix){
        Iterator<MySearch> iterator = mySearchList.iterator();
        List<String> stringList = new ArrayList<>();
        while (iterator.hasNext()) {
            MySearch mySearch = iterator.next();
            stringList.add(createWhereColumn(prefix,mySearch));
        }

        List<MySearchList> ands = mySearchList.getAnds();
        if(!WsListUtils.isEmpty(ands)){
            for(MySearchList searchList:ands){
                List<String> andStrings = searchListWhereSqlProcessor(searchList,prefix);
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
                List<String> orStrings = searchListWhereSqlProcessor(searchList,prefix);
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

    public Map<Integer, Object> getValueMap() {
        return valueMap;
    }
}



