package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.KeyValue;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;
import cn.katoumegumi.java.sql.entity.*;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ws
 */
public class SQLModelUtils {

    private static final Logger log = LoggerFactory.getLogger(SQLModelUtils.class);

    /**
     * 插入参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> insertSqlInterceptorMap = new HashMap<>();
    /**
     * 修改参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> updateSqlInterceptorMap = new HashMap<>();
    /**
     * 查询参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> selectSqlInterceptorMap = new HashMap<>();


    /**
     * 已经使用过的表关联关系
     */
    private final Set<TableRelation> usedTableRelation = new HashSet<>();

    /**
     * 记录where所需要的值
     */
    private final List<SqlParameter> baseWhereValueList = new ArrayList<>();

    /**
     * 表面和列名转换
     */
    private final TranslateNameUtils translateNameUtils;


    /**
     * 表查询条件
     */
    private final MySearchList mySearchList;

    /**
     * 主表的class类型
     */
    private final Class<?> mainClass;

    /**
     * 缓存的sqlEntity
     */
    private SqlEntity cacheSqlEntity;
    private SelectModel cacheSelectModel;

    public SQLModelUtils(MySearchList mySearchList) {
        this(mySearchList, new TranslateNameUtils(),false);
    }

    public SQLModelUtils(MySearchList mySearchList, TranslateNameUtils translateNameUtils){
        this(mySearchList,translateNameUtils,true);
    }

    public SQLModelUtils(MySearchList mySearchList, TranslateNameUtils parentTranslateNameUtils,boolean isNested) {
        this.mySearchList = mySearchList;
        this.translateNameUtils = isNested?new TranslateNameUtils(parentTranslateNameUtils):parentTranslateNameUtils;
        mainClass = mySearchList.getMainClass();
        FieldColumnRelationMapper mapper = analysisClassRelation(mainClass);
        if (WsStringUtils.isBlank(mySearchList.getAlias())) {
            translateNameUtils.setAbbreviation(mapper.getNickName());
        } else {
            translateNameUtils.setAbbreviation(mapper.getNickName(), mySearchList.getAlias());
        }
        String prefix = mapper.getNickName();
        translateNameUtils.addMainClassName(prefix);
        String tableName;
        String joinTableName;
        for (TableRelation relation : mySearchList.getJoins()) {
            tableName = relation.getTableNickName();
            if (WsStringUtils.isNotBlank(tableName)) {
                tableName = translateNameUtils.translateToTableName(tableName);
                if (tableName.startsWith(prefix)) {
                    if (tableName.length() == prefix.length()) {
                        tableName = null;
                    } else {
                        tableName = tableName.substring(prefix.length() + 1);
                    }
                }
            } else {
                tableName = null;
            }
            relation.setTableNickName(tableName);
            if (WsStringUtils.isNotBlank(relation.getJoinTableNickName())) {
                joinTableName = translateNameUtils.translateToTableName(relation.getJoinTableNickName());
                joinTableName = translateNameUtils.getNoPrefixTableName(joinTableName);
                relation.setJoinTableNickName(joinTableName);
                joinTableName = prefix + '.' + joinTableName;
                if (WsStringUtils.isBlank(relation.getAlias())) {
                    relation.setAlias(translateNameUtils.createAbbreviation(joinTableName));
                }
                translateNameUtils.setAbbreviation(joinTableName, relation.getAlias());
            }
            tableName = null;
            joinTableName = null;

        }
    }


    public static FieldColumnRelationMapper analysisClassRelation(Class<?> mainClass) {
        return FieldColumnRelationMapperFactory.analysisClassRelation(mainClass);
    }


    /**
     * 数据库关键词
     *
     * @param keyword
     * @return
     */
    public static String guardKeyword(String keyword) {
        if (keyword.startsWith("`")) {
            return keyword;
        }
        return '`' + keyword + '`';
    }

    /**
     * 对象转换成表查询条件
     *
     * @param o
     * @return
     */
    public static MySearchList objectToMySearchList(Object o) {
        FieldColumnRelationMapper mapper = analysisClassRelation(o.getClass());
        List<FieldColumnRelation> ids = mapper.getIds();
        List<FieldColumnRelation> columns = mapper.getFieldColumnRelations();
        MySearchList mySearchList = MySearchList.create(o.getClass());
        if (WsListUtils.isNotEmpty(ids)) {
            for (FieldColumnRelation relation : ids) {
                if (WsBeanUtils.isBaseType(relation.getFieldClass())) {
                    try {
                        Object value = relation.getField().get(o);
                        if (value != null) {
                            mySearchList.eq(relation.getFieldName(), value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (WsListUtils.isNotEmpty(columns)) {
            for (FieldColumnRelation relation : columns) {
                if (WsBeanUtils.isBaseType(relation.getFieldClass())) {
                    try {
                        Object value = relation.getField().get(o);
                        if (value != null) {
                            mySearchList.eq(relation.getFieldName(), value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mySearchList;
    }

    /**
     * sql拦截器
     *
     * @param sqlInterceptor
     */
    public static void addSqlInterceptor(AbstractSqlInterceptor sqlInterceptor) {
        if (sqlInterceptor.isInsert()) {
            insertSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isUpdate()) {
            updateSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isSelect()) {
            selectSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
    }


    /**
     * 获取mapper
     *
     * @param clazz
     * @return
     */
    public static FieldColumnRelationMapper getFieldColumnRelationMapper(Class<?> clazz) {
        return FieldColumnRelationMapperFactory.analysisClassRelation(clazz);
    }

    /**
     * 生成单个insert sql语句
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> InsertSqlEntity insertSql(T t) {
        InsertSqlEntity entity = new InsertSqlEntity();
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mainClass);
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<FieldColumnRelation> validList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();

        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();

        for (FieldColumnRelation fieldColumnRelation : idList) {
            Field field = fieldColumnRelation.getField();
            try {
                Object o = field.get(t);
                if (o != null) {
                    columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
                    placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                    validList.add(fieldColumnRelation);
                    valueList.add(new SqlParameter(o));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            Field field = fieldColumnRelation.getField();
            AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(analysisClassRelation(mainClass))) {
                o = sqlInterceptor.insertFill();
            } else {
                o = WsFieldUtils.getValue(t, field);
            }
            if (o != null) {
                columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                validList.add(fieldColumnRelation);
                valueList.add(new SqlParameter(o));
            }
        }

        String insertSql = SqlCommonConstants.INSERT_INTO + guardKeyword(fieldColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(placeholderList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS;
        entity.setInsertSql(insertSql);
        entity.setUsedField(validList);
        entity.setIdList(fieldColumnRelationMapper.getIds());
        entity.setValueList(valueList);
        return entity;
    }

    /**
     * 生成insert sql语句
     *
     * @param tList
     * @param <T>
     * @return
     */
    public <T> InsertSqlEntity insertSqlBatch(List<T> tList) {
        if (tList == null) {
            throw new NullPointerException("The list cannot be empty");
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(tList.get(0).getClass());
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<FieldColumnRelation> validField = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();


        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();
        for (FieldColumnRelation fieldColumnRelation : idList) {
            Field field = fieldColumnRelation.getField();
            try {
                Object o = field.get(tList.get(0));
                validField.add(fieldColumnRelation);
                columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                valueList.add(new SqlParameter(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            Field field = fieldColumnRelation.getField();

            AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.insertFill();
            } else {
                o = WsFieldUtils.getValue(tList.get(0), field);
            }
            validField.add(fieldColumnRelation);
            columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
            placeholderList.add(SqlCommonConstants.PLACEHOLDER);
            valueList.add(new SqlParameter(o));
        }
        String placeholderSql = SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(placeholderList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS;
        placeholderList = new ArrayList<>();
        placeholderList.add(placeholderSql);
        int size = tList.size();
        for (int i = 1; i < size; i++) {
            for (FieldColumnRelation fieldColumnRelation : validField) {
                Field field = fieldColumnRelation.getField();
                AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                Object o = null;
                if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                    o = sqlInterceptor.insertFill();
                } else {
                    o = WsFieldUtils.getValue(tList.get(i), field);
                }
                valueList.add(new SqlParameter(o));
            }
            placeholderList.add(placeholderSql);
        }
        InsertSqlEntity insertSqlEntity = new InsertSqlEntity();
        String insertSql = SqlCommonConstants.INSERT_INTO + guardKeyword(fieldColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + WsStringUtils.jointListString(placeholderList, ",");
        insertSqlEntity.setInsertSql(insertSql);
        insertSqlEntity.setUsedField(validField);
        insertSqlEntity.setIdList(fieldColumnRelationMapper.getIds());
        insertSqlEntity.setValueList(valueList);
        return insertSqlEntity;
    }

    /**
     * 生成update sql语句
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> UpdateSqlEntity update(T t, boolean isAll) {
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(t.getClass());
        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();
        List<FieldColumnRelation> columnList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<String> columnStrList = new ArrayList<>();
        List<String> idStrList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();

        List<FieldColumnRelation> validColumnList = new ArrayList<>();
        List<FieldColumnRelation> validIdList = new ArrayList<>();

        for (FieldColumnRelation fieldColumnRelation : columnList) {

            AbstractSqlInterceptor sqlInterceptor = updateSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.updateFill();
            } else {
                o = WsFieldUtils.getValue(t, fieldColumnRelation.getField());
            }
            if (isAll || o != null) {
                String str = guardKeyword(fieldColumnRelation.getColumnName()) + SqlCommonConstants.EQ + SqlCommonConstants.PLACEHOLDER;
                columnStrList.add(str);
                valueList.add(new SqlParameter(o));
                validColumnList.add(fieldColumnRelation);
            }
        }
        for (FieldColumnRelation fieldColumnRelation : idList) {
            try {
                Object o = fieldColumnRelation.getField().get(t);
                if (o != null) {
                    String str = guardKeyword(fieldColumnRelation.getColumnName()) + SqlCommonConstants.EQ + SqlCommonConstants.PLACEHOLDER;
                    idStrList.add(str);
                    valueList.add(new SqlParameter(o));
                    validIdList.add(fieldColumnRelation);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (idStrList.size() == 0) {
            throw new NullPointerException("primary key is null");
        }
        String updateSql = SqlCommonConstants.UPDATE + guardKeyword(fieldColumnRelationMapper.getTableName()) + SqlCommonConstants.SET + WsStringUtils.jointListString(columnStrList, SqlCommonConstants.COMMA) + SqlCommonConstants.WHERE + WsStringUtils.jointListString(idStrList, SqlCommonConstants.SQL_AND);
        UpdateSqlEntity updateSqlEntity = new UpdateSqlEntity();
        updateSqlEntity.setUpdateSql(updateSql);
        updateSqlEntity.setIdList(validIdList);
        updateSqlEntity.setUsedField(validColumnList);
        updateSqlEntity.setValueList(valueList);
        return updateSqlEntity;
    }

    /**
     * 合并返回数据
     *
     * @param resultSet
     * @param <T>
     * @return
     */
    public <T> List<T> margeMap(WsResultSet resultSet) {
        if (WsListUtils.isEmpty(mySearchList.getColumnNameList())) {
            return oneLoopMargeMap(resultSet);
        } else {
            List<Object> tList = new ArrayList<>();
            TableColumn tableColumn = this.cacheSelectModel == null?cacheSqlEntity.getColumnList().get(0):this.cacheSelectModel.getSelect().get(0);
            try {
                while (resultSet.next()) {
                    Object o = WsBeanUtils.objectToT(resultSet.getObject(1), tableColumn.getField().getType());
                    if (o != null) {
                        tList.add(o);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return (List<T>) tList;
        }
    }

    /**
     * 合并返回数据
     *
     * @param resultSet
     * @param <T>
     * @return
     */
    public <T> List<T> oneLoopMargeMap(WsResultSet resultSet) {
        try {
            int classNum = translateNameUtils.locationMapperSize();
            //ResultSetMetaData resultSetMetaData = null;

            //resultSetMetaData = resultSet.getMetaData();
            //length = resultSetMetaData.getColumnCount();
            final int length = resultSet.getColumnCount();

            if (length == 0) {
                return new ArrayList<>(0);
            }
            FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
            String rootPath = mainMapper.getNickName();


            List<List<String>> columnNameListList = new ArrayList<>(length);
            List<FieldColumnRelationMapper> mapperList = new ArrayList<>(length);
            List<FieldColumnRelation> columnRelationList = new ArrayList<>(length);


            String columnName = null;
            for (int i = 0; i < length; i++) {
                columnName = resultSet.getColumnLabel(i + 1);
                List<String> nameList = WsStringUtils.split(columnName, '.');
                nameList.set(0, translateNameUtils.getParticular(nameList.get(0)));
                FieldColumnRelationMapper mapper = translateNameUtils.getLocalMapper(nameList.get(0));
                FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByFieldName(nameList.get(1));
                columnNameListList.add(nameList);
                mapperList.add(mapper);
                columnRelationList.add(fieldColumnRelation);
            }

            List<ReturnEntity> returnEntityList = new ArrayList<>();
            Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap = new HashMap<>();

            Map<String, ReturnEntity> returnEntityMap;
            while (resultSet.next()) {
                returnEntityMap = new HashMap<>(classNum);
                Object value;
                for (int i = 0; i < length; ++i) {
                    value = resultSet.getObject(i + 1);
                    List<String> nameList = columnNameListList.get(i);
                    FieldColumnRelationMapper mapper = mapperList.get(i);
                    FieldColumnRelation fieldColumnRelation = columnRelationList.get(i);
                    ReturnEntity returnEntity = returnEntityMap.computeIfAbsent(nameList.get(0), columnTypeName -> {
                        return new ReturnEntity(mapper);
                    });

                    if (fieldColumnRelation.isId()) {
                        returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    } else {
                        returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    }
                }
                ReturnEntity returnEntity = returnEntityMap.get(rootPath);
                if (returnEntity != null) {
                    ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity, rootPath);
                    if (returnEntity.equals(mainEntity)) {
                        returnEntityList.add(mainEntity);
                    }
                }
            }
            if (returnEntityList.size() == 0) {
                return new ArrayList<>(0);
            }
            List<Object> list = new ArrayList<>(returnEntityList.size());
            for (ReturnEntity returnEntity : returnEntityList) {
                list.add(returnEntity.getValue());
            }

            return (List<T>) list;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ArrayList<>(0);
        }
    }


    public <T> List<T> oneLoopMargeMap2(WsResultSet resultSet) {
        try {
            int classNum = translateNameUtils.locationMapperSize();
            final int length = resultSet.getColumnCount();

            if (length == 0) {
                return new ArrayList<>(0);
            }
            FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
            String rootPath = mainMapper.getNickName();


            List<String> columnNameListList = new ArrayList<>(length);
            List<FieldColumnRelation> columnRelationList = new ArrayList<>(length);
            List<FieldColumnRelationMapper> mapperList = new ArrayList<>(length);
            Map<FieldColumnRelationMapper,int[][]> mapperAndLocaltionMap = new HashMap<>();
            Map<FieldColumnRelationMapper,int[]> mapperAndSplitColumnNameListMap = new HashMap<>();
            String columnName;
            List<String> nameList;
            FieldColumnRelationMapper mapper;
            FieldColumnRelation fieldColumnRelation;
            int[][] locationList;
            int mapperNameSign = 0;
            Map<String,Integer> mapperNameAndSignMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                columnName = resultSet.getColumnLabel(i + 1);
                nameList = WsStringUtils.split(columnName, '.');
                nameList.set(0, translateNameUtils.getParticular(nameList.get(0)));
                mapper = translateNameUtils.getLocalMapper(nameList.get(0));
                int[] mapperNameSplitArray = mapperAndSplitColumnNameListMap.get(mapper);
                if(mapperNameSplitArray == null){
                    List<String> mapperNameSplitList = WsStringUtils.split(nameList.get(0),'.');
                    mapperNameSplitArray = new int[mapperNameSplitList.size()];
                    for (int index = 0,aLength = mapperNameSplitList.size(); index < aLength; index++){
                        Integer sign = mapperNameAndSignMap.get(mapperNameSplitList.get(index));
                        if(sign == null){
                            sign = mapperNameSign++;
                            mapperNameAndSignMap.put(mapperNameSplitList.get(index),sign);
                        }
                        mapperNameSplitArray[index] = sign;
                    }
                    mapperAndSplitColumnNameListMap.put(mapper,mapperNameSplitArray);
                }
                locationList = mapperAndLocaltionMap.computeIfAbsent(mapper,m->{
                    int[][] integers = new int[2][];
                    integers[0] = new int[m.getIds().size()];
                    integers[1] = new int[m.getFieldColumnRelations().size()];
                    return integers;
                });
                fieldColumnRelation = mapper.getFieldColumnRelationByFieldName(nameList.get(1));
                if(fieldColumnRelation.isId()){
                    locationList[0][mapper.getLocation(fieldColumnRelation)] = i;
                }else {
                    locationList[1][mapper.getLocation(fieldColumnRelation)] = i;
                }
                columnNameListList.add(nameList.get(nameList.size() - 1));
                mapperList.add(mapper);
                columnRelationList.add(fieldColumnRelation);
            }
            MapperDictTree mapperDictTree = new MapperDictTree();
            for (Map.Entry<FieldColumnRelationMapper, int[]> fieldColumnRelationMapperEntry : mapperAndSplitColumnNameListMap.entrySet()) {
                mapperDictTree.add(fieldColumnRelationMapperEntry.getValue(),fieldColumnRelationMapperEntry.getKey());
            }



            List<ReturnEntity> returnEntityList = new ArrayList<>();

            while (resultSet.next()){

            }

            if (returnEntityList.size() == 0) {
                return new ArrayList<>(0);
            }
            List<Object> list = new ArrayList<>(returnEntityList.size());
            for (ReturnEntity returnEntity : returnEntityList) {
                list.add(returnEntity.getValue());
            }

            return (List<T>) list;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ArrayList<>(0);
        }
    }



    /**
     * 合并返回数据
     * @param mapList
     * @param <T>
     * @return
     */
    public <T> List<T> oneLoopMargeMap(List<Map<Object, Object>> mapList) {
        if (WsListUtils.isEmpty(mapList)) {
            return new ArrayList<>(0);
        }

        FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
        String baseTableName = mainMapper.getNickName();

        List<ReturnEntity> returnEntityList = new ArrayList<>(mapList.size());
        List<List<String>> columnNameListList = new ArrayList<>();
        List<FieldColumnRelationMapper> mapperList = new ArrayList<>(mapList.size());
        List<FieldColumnRelation> columnRelationList = new ArrayList<>(mapList.size());
        Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap = new HashMap<>(mapList.size());

        Map<Object, Object> firstMap = mapList.get(0);
        Set<Map.Entry<Object, Object>> entrySet = firstMap.entrySet();

        for (Map.Entry<Object, Object> entry : entrySet) {
            List<String> nameList = WsStringUtils.split((String) entry.getKey(), '.');
            nameList.set(0, translateNameUtils.getParticular(nameList.get(0)));
            FieldColumnRelationMapper mapper = translateNameUtils.getLocalMapper(nameList.get(0));
            FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByFieldName(nameList.get(1));

            columnNameListList.add(nameList);
            mapperList.add(mapper);
            columnRelationList.add(fieldColumnRelation);
        }


        Map<String, ReturnEntity> returnEntityMap;
        for (Map<Object, Object> map : mapList) {
            returnEntityMap = new HashMap<>();
            entrySet = map.entrySet();
            int i = 0;
            for (Map.Entry<Object, Object> entry : entrySet) {
                if (entry.getValue() == null) {
                    ++i;
                    continue;
                }
                List<String> nameList = columnNameListList.get(i);
                FieldColumnRelationMapper mapper = mapperList.get(i);
                FieldColumnRelation fieldColumnRelation = columnRelationList.get(i);
                ReturnEntity returnEntity = returnEntityMap.computeIfAbsent(nameList.get(0), columnTypeName -> {
                    return new ReturnEntity(mapper);
                });

                if (fieldColumnRelation.isId()) {
                    returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                } else {
                    returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                }
                ++i;
            }
            ReturnEntity returnEntity = returnEntityMap.get(baseTableName);
            if (returnEntity != null) {
                ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity, baseTableName);
                //ReturnEntityUtils.packageReturnEntity(idReturnEntityMap, returnEntityMap, mainEntity, baseTableName);
                if (returnEntity.equals(mainEntity)) {
                    returnEntityList.add(mainEntity);
                }
            }
        }

        List<T> list = new ArrayList<>(returnEntityList.size());
        for (ReturnEntity returnEntity : returnEntityList) {
            list.add((T) returnEntity.getValue());
        }

        return list;
    }

    public TranslateNameUtils getTranslateNameUtils() {
        return translateNameUtils;
    }


    /**
     * MySearchList转换为SelectModel
     * @return
     */
    public SelectModel transferToSelectModel(){
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper,translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath,mainMapper);
        final boolean appointQueryColumn = WsListUtils.isNotEmpty(this.mySearchList.getColumnNameList());
        List<TableColumn> queryColumnList = new ArrayList<>();

        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath,mainMapper,queryColumnList,appointQueryColumn,false);

        if(appointQueryColumn){
            if(this.mySearchList.isSingleColumn()){
                //是单列查询自动加入distinct关键字去重
                BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity(this.mySearchList.getColumnNameList().get(0),rootPath,2);
                DynamicTableColumn dynamicTableColumn = new DynamicTableColumn(
                        new SqlFunctionCondition(false,"distinct ",baseTableColumn),
                        baseTableColumn
                );
                queryColumnList.add(dynamicTableColumn);
            }else {
                for (String columnName:this.mySearchList.getColumnNameList()){
                    queryColumnList.add(translateNameUtils.getColumnBaseEntity(columnName,rootPath,2));
                }
            }

        }

        RelationCondition where = handleWhere(rootPath,mainMapper,this.mySearchList);


        List<OrderByCondition> orderByConditionList = null;
        if(WsListUtils.isNotEmpty(this.mySearchList.getOrderSearches())){
            orderByConditionList = new ArrayList<>(this.mySearchList.getOrderSearches().size());
            for (MySearch mySearch:this.mySearchList.getOrderSearches()){
                if(mySearch.getFieldName().charAt(mySearch.getFieldName().length() - 1) == SqlCommonConstants.RIGHT_BRACKETS){
                    orderByConditionList.add(new OrderByCondition(new SqlStringModel(translateNameUtils.translateTableNickName(rootPath,mySearch.getFieldName()),null),WsBeanUtils.objectToT(mySearch.getValue(),String.class)));
                }else {
                    orderByConditionList.add(new OrderByCondition(translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(),rootPath,2),WsBeanUtils.objectToT(mySearch.getValue(),String.class)));
                }
            }
        }
        SelectModel selectModel = new SelectModel(queryColumnList,from,joinTableModelList,where, orderByConditionList,this.mySearchList.getSqlLimit());
        this.cacheSelectModel = selectModel;
        return selectModel;
    }

    /**
     * MySearchList转换为DeleteModel
     * @return
     */
    public DeleteModel transferToDeleteModel(){
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper,translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath,mainMapper);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath,mainMapper,null,true,true);
        RelationCondition where = handleWhere(rootPath,mainMapper,this.mySearchList);
        return new DeleteModel(from,joinTableModelList,where);
    }

    /**
     * MySearchList转换为UpdateModel
     * @return
     */
    public UpdateModel transferToUpdateModel(){
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        List<MySearch> updateSearchList = this.mySearchList.filterUpdateSearch();
        TableModel from = new TableModel(mainMapper,translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath,mainMapper);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath,mainMapper,null,true,true);
        RelationCondition where = handleWhere(rootPath,mainMapper,this.mySearchList);
        List<Condition> conditionList = searchToExpressionCondition(rootPath,updateSearchList,null);
        if(WsListUtils.isEmpty(conditionList)){
            throw new NullPointerException("update condition is null");
        }
        List<Condition> updateInterceptorConditionList = getUpdateConditionSqlInterceptorConditionList(rootPath,mainMapper);
        if(WsListUtils.isNotEmpty(updateInterceptorConditionList)){
            conditionList.addAll(updateInterceptorConditionList);
        }
        return new UpdateModel(from,joinTableModelList,conditionList,where);
    }

    /**
     * 表关联处理
     * @param rootPath 根路径
     * @param mainMapper 主映射
     * @param queryColumnList 存储查询列的队列
     * @param appointQueryColumn 是否指定了需要查询的列
     * @param ignoreDefaultJoinTable 是否忽略默认关联的表
     * @return
     */
    private List<JoinTableModel> handleJoinTableModel(final String rootPath, final FieldColumnRelationMapper mainMapper, final List<TableColumn> queryColumnList, final boolean appointQueryColumn, final boolean ignoreDefaultJoinTable){
        final List<TableRelation> tableRelationList = this.mySearchList.getJoins();
        final List<JoinTableModel> joinTableModelList = new ArrayList<>();
        final Map<TableRelation,JoinTableModel> usedRelationMap = new HashMap<>();
        if(!ignoreDefaultJoinTable) {
            final Map<String, TableRelation> tableRelationMap = new HashMap<>();
            for (TableRelation tableRelation : tableRelationList) {
                if (tableRelation.getTableNickName() == null) {
                    tableRelationMap.put(translateNameUtils.getCurrentAbbreviation(rootPath) + SqlCommonConstants.KEY_COMMON_DELIMITER + translateNameUtils.getAbbreviation(translateNameUtils.getCompleteTableNickName(rootPath, tableRelation.getJoinTableNickName())), tableRelation);
                } else {

                    tableRelationMap.put(translateNameUtils.getCurrentAbbreviation(translateNameUtils.getCompleteTableNickName(rootPath, tableRelation.getTableNickName())) + SqlCommonConstants.KEY_COMMON_DELIMITER + translateNameUtils.getAbbreviation(translateNameUtils.getCompleteTableNickName(rootPath, tableRelation.getJoinTableNickName())), tableRelation);
                }
            }
            final Queue<KeyValue<String, FieldJoinClass>> queue = new ArrayDeque<>();
            for (FieldJoinClass fieldJoinClass : mainMapper.getFieldJoinClasses()) {
                queue.add(new KeyValue<>(rootPath, fieldJoinClass));
            }

            if (!appointQueryColumn) {
                addQueryColumnList(mainMapper, rootPath, queryColumnList);
            }

            while (queue.size() > 0) {
                KeyValue<String, FieldJoinClass> keyValue = queue.poll();
                FieldJoinClass fieldJoinClass = keyValue.getValue();
                String path = keyValue.getKey();
                String joinPath = path + SqlCommonConstants.PATH_COMMON_DELIMITER + fieldJoinClass.getNickName();
                String tableAlias = translateNameUtils.getCurrentAbbreviation(path);
                String joinTableAlias = translateNameUtils.getCurrentAbbreviation(joinPath);
                String key = tableAlias + SqlCommonConstants.KEY_COMMON_DELIMITER + joinTableAlias;
                TableRelation tableRelation = tableRelationMap.get(key);
                boolean checkChild = false;
                FieldColumnRelationMapper joinMapper = null;
                if (tableRelation != null) {
                    checkChild = true;
                    joinMapper = analysisClassRelation(fieldJoinClass.getJoinClass());
                    translateNameUtils.addLocalMapper(joinPath, joinMapper);
                    List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath, joinMapper);
                    List<Condition> conditionModelList = new ArrayList<>((tableRelation.getConditionSearchList() == null ? 1 : 1 + tableRelation.getConditionSearchList().getAll().size()) + selectInterceptorConditionList.size());
                    conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(tableRelation.getTableColumn(), path, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(tableRelation.getJoinTableColumn(), joinPath, 2)));
                    if (WsListUtils.isNotEmpty(selectInterceptorConditionList)) {
                        conditionModelList.addAll(selectInterceptorConditionList);
                    }
                    JoinTableModel joinTableModel = new JoinTableModel(new TableModel(translateNameUtils.getLocalMapper(path), tableAlias), new TableModel(joinMapper, joinTableAlias), tableRelation.getJoinType(), new RelationCondition(conditionModelList, SqlOperator.AND));
                    joinTableModelList.add(joinTableModel);
                    usedRelationMap.put(tableRelation, joinTableModel);
                } else if (WsStringUtils.isNotBlank(fieldJoinClass.getAnotherJoinColumn())) {
                    checkChild = true;
                    joinMapper = analysisClassRelation(fieldJoinClass.getJoinClass());
                    FieldColumnRelationMapper mapper = translateNameUtils.getLocalMapper(path);
                    translateNameUtils.addLocalMapper(joinPath, joinMapper);
                    List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath, joinMapper);
                    List<Condition> conditionModelList = new ArrayList<>(1 + selectInterceptorConditionList.size());
                    conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(mapper.getFieldColumnRelationByColumn(fieldJoinClass.getJoinColumn()).getFieldName(), path, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(joinMapper.getFieldColumnRelationByColumn(fieldJoinClass.getAnotherJoinColumn()).getFieldName(), joinPath, 2)));
                    if (WsListUtils.isNotEmpty(selectInterceptorConditionList)) {
                        conditionModelList.addAll(selectInterceptorConditionList);
                    }
                    joinTableModelList.add(new JoinTableModel(new TableModel(translateNameUtils.getLocalMapper(path), tableAlias), new TableModel(joinMapper, joinTableAlias), fieldJoinClass.getJoinType(), new RelationCondition(conditionModelList, SqlOperator.AND)));
                }
                if (checkChild) {
                    if (!appointQueryColumn) {
                        addQueryColumnList(joinMapper, joinPath, queryColumnList);
                    }
                    for (FieldJoinClass join : joinMapper.getFieldJoinClasses()) {
                        queue.add(new KeyValue<>(joinPath, join));
                    }
                }
            }
        }

        for (TableRelation tableRelation:tableRelationList){
            JoinTableModel joinTableModel = usedRelationMap.get(tableRelation);
            if(joinTableModel == null){
                String path = tableRelation.getTableNickName() == null?rootPath:rootPath+ SqlCommonConstants.PATH_COMMON_DELIMITER+tableRelation.getTableNickName();
                String joinPath = tableRelation.getJoinTableNickName()==null?rootPath:rootPath+ SqlCommonConstants.PATH_COMMON_DELIMITER+tableRelation.getJoinTableNickName();
                FieldColumnRelationMapper joinMapper = analysisClassRelation(tableRelation.getJoinTableClass());
                translateNameUtils.addLocalMapper(joinPath,joinMapper);
                FieldColumnRelationMapper mapper = tableRelation.getTableNickName()==null?mainMapper:translateNameUtils.getLocalMapper(path);
                List<Condition> conditionModelList = new ArrayList<>();
                conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(tableRelation.getTableColumn(),path,2), SqlEquation.Symbol.EQUAL,translateNameUtils.createColumnBaseEntity(tableRelation.getJoinTableColumn(),joinPath,2)));
                List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath,joinMapper);
                if(WsListUtils.isNotEmpty(selectInterceptorConditionList)){
                    conditionModelList.addAll(selectInterceptorConditionList);
                }
                if(tableRelation.getConditionSearchList() != null){
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath,tableRelation.getConditionSearchList(),SqlOperator.AND);
                    if(relationCondition != null && WsListUtils.isNotEmpty(relationCondition.getConditionList())){
                        conditionModelList.addAll(relationCondition.getConditionList());
                    }

                }
                joinTableModelList.add(new JoinTableModel(new TableModel(mapper,translateNameUtils.getCurrentAbbreviation(path)),new TableModel(joinMapper,translateNameUtils.getCurrentAbbreviation(joinPath)),tableRelation.getJoinType(), new RelationCondition(conditionModelList,SqlOperator.AND)));
            }else {
                if(tableRelation.getConditionSearchList() != null){
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath,tableRelation.getConditionSearchList(),SqlOperator.AND);
                    if(relationCondition != null && WsListUtils.isNotEmpty(relationCondition.getConditionList())){
                        joinTableModel.getOn().getConditionList().addAll(relationCondition.getConditionList());
                    }
                }
            }
        }
        return joinTableModelList;
    }

    /**
     * 处理where条件（必须在处理表关联关系之后进行）
     * @param rootPath
     * @param mainMapper
     * @param mySearchList
     * @return
     */
    private RelationCondition handleWhere(final String rootPath, final FieldColumnRelationMapper mainMapper, final MySearchList mySearchList){
        RelationCondition where = searchListToConditionRelation(rootPath,mySearchList,SqlOperator.AND);
        List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(rootPath,mainMapper);
        if(WsListUtils.isNotEmpty(selectInterceptorConditionList)){
            if(where == null){
                where = new RelationCondition(selectInterceptorConditionList,SqlOperator.AND);
            }else {
                where.getConditionList().addAll(selectInterceptorConditionList);
            }
        }
        return where;
    }

    /**
     * 解析需要查询的列名
     * @param mapper
     * @param path
     * @param queryColumnList
     */
    private void addQueryColumnList(final FieldColumnRelationMapper mapper,final String path,final List<TableColumn> queryColumnList) {
        if(WsListUtils.isNotEmpty(mapper.getIds())){
            for (FieldColumnRelation fieldColumnRelation:mapper.getIds()){
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(fieldColumnRelation,mapper,path));
            }
        }
        if(WsListUtils.isNotEmpty(mapper.getFieldColumnRelations())){
            for (FieldColumnRelation fieldColumnRelation:mapper.getFieldColumnRelations()){
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(fieldColumnRelation,mapper,path));
            }
        }
    }


    private RelationCondition searchListToConditionRelation(final String rootPath, final MySearchList searchList, final SqlOperator relation){
        if(WsListUtils.isEmpty(searchList.getAll()) && WsListUtils.isEmpty(searchList.getAnds()) && WsListUtils.isEmpty(searchList.getOrs())){
            return null;
        }
        RelationCondition where = new RelationCondition(searchToExpressionCondition(rootPath,searchList.getAll(),null),relation);

        if(WsListUtils.isNotEmpty(searchList.getAnds())){
            RelationCondition andWhere = new RelationCondition(new ArrayList<>(),SqlOperator.AND);
            for (MySearchList and:searchList.getAnds()){
                RelationCondition childCondition = searchListToConditionRelation(rootPath,and,SqlOperator.AND);
                if(childCondition != null){
                    andWhere.getConditionList().add(childCondition);
                }
            }
            if(!WsListUtils.isEmpty(andWhere.getConditionList())){
                where.getConditionList().add(andWhere);
            }
        }
        if(WsListUtils.isNotEmpty(searchList.getOrs())){
            RelationCondition orWhere = new RelationCondition(new ArrayList<>(),SqlOperator.OR);
            for (MySearchList and:searchList.getOrs()){
                RelationCondition childCondition = searchListToConditionRelation(rootPath,and,SqlOperator.AND);
                if(childCondition != null){
                    orWhere.getConditionList().add(childCondition);
                }
            }
            if(!WsListUtils.isEmpty(orWhere.getConditionList())){
                where.getConditionList().add(orWhere);
            }
        }
        return where;
    }

    /**
     * mysearch转condition
     * @param rootPath
     * @param searches
     * @param expressionConditionList
     * @return
     */
    private List<Condition> searchToExpressionCondition(String rootPath,List<MySearch> searches,List<Condition> expressionConditionList){
        if(expressionConditionList == null){
            expressionConditionList = new ArrayList<>(searches.size());
        }
        for (MySearch search:searches){
            Object left = null;
            SqlOperator operator = search.getOperator();

            Object right = null;
            switch (operator){
                case EQP:
                case NEP:
                case GTP:
                case LTP:
                case GTEP:
                case LTEP:
                    left = translateNameUtils.getColumnBaseEntity(search.getFieldName(),rootPath,2);
                    right = translateNameUtils.getColumnBaseEntity((String) search.getValue(),rootPath,2);
                    break;
                case SQL:
                    left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath,search.getFieldName()),search.getValue());
                    break;
                case EXISTS:
                case NOT_EXISTS:
                    if(search.getValue() instanceof MySearchList){
                        //right = search.getValue();
                        left = search.getValue();
                    }else {
                        left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath,search.getFieldName()),search.getValue());
                    }
                    break;
                case EQUATION:
                    SqlEquation sqlEquation = (SqlEquation) search.getValue();
                    expressionConditionList.add(transferSqlEquationToCondition(rootPath,sqlEquation));
                    continue;
                case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                    BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity(search.getFieldName(),rootPath,2);
                    MultiExpressionCondition multiExpressionCondition = new MultiExpressionCondition(5);
                    multiExpressionCondition.add(baseTableColumn)
                            .add(SqlEquation.Symbol.EQUAL);
                    if(WsFieldUtils.classCompare(baseTableColumn.getField().getType(),Number.class)){
                        multiExpressionCondition.add(new SqlFunctionCondition("IFNULL", baseTableColumn,0));
                    }else {
                        multiExpressionCondition.add(baseTableColumn);
                    }
                    multiExpressionCondition.add(sqlOperatorToSymbol(operator))
                            .add(search.getValue() == null? SqlCommonConstants.NULL_VALUE:search.getValue());
                    expressionConditionList.add(multiExpressionCondition);
                    continue;
                case SET: operator = SqlOperator.EQ;
                default:
                    left = translateNameUtils.getColumnBaseEntity(search.getFieldName(),rootPath,2);
                    right = search.getValue();
                    if(right == null){
                        right = SqlCommonConstants.NULL_VALUE;
                    }
                    break;
            }
            if(right instanceof MySearchList){
                SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) right,translateNameUtils);
                right = sqlModelUtils.transferToSelectModel();
            }
            if(left instanceof MySearchList){
                SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) left,translateNameUtils);
                left = sqlModelUtils.transferToSelectModel();
            }
            SqlEquation.Symbol symbol = sqlOperatorToSymbol(operator);
            expressionConditionList.add(new SingleExpressionCondition(left,symbol,right));
        }

        return expressionConditionList;
    }

    private SqlEquation.Symbol sqlOperatorToSymbol(SqlOperator sqlOperator){
        switch (sqlOperator){
            case EQ:
            case EQP:
                return SqlEquation.Symbol.EQUAL;
            case NE:
            case NEP:
                return SqlEquation.Symbol.NOT_EQUAL;
            case GT:
            case GTP:
                return SqlEquation.Symbol.GT;
            case GTE:
            case GTEP:
                return SqlEquation.Symbol.GTE;
            case LT:
            case LTP:
                return SqlEquation.Symbol.LT;
            case LTE:
            case LTEP:
                return SqlEquation.Symbol.LTE;
            case LIKE:return SqlEquation.Symbol.LIKE;
            case IN:return SqlEquation.Symbol.IN;
            case NIN:return SqlEquation.Symbol.NOT_IN;
            case EXISTS:return SqlEquation.Symbol.EXISTS;
            case NOT_EXISTS:return SqlEquation.Symbol.NOT_EXISTS;
            case BETWEEN:return SqlEquation.Symbol.BETWEEN;
            case NOT_BETWEEN:return SqlEquation.Symbol.NOT;
            case SQL:return SqlEquation.Symbol.SQL;
            case ADD:return SqlEquation.Symbol.ADD;
            case SUBTRACT:return SqlEquation.Symbol.SUBTRACT;
            case MULTIPLY:return SqlEquation.Symbol.MULTIPLY;
            case DIVIDE:return SqlEquation.Symbol.DIVIDE;
            case SET:return SqlEquation.Symbol.SET;
            case NULL:return SqlEquation.Symbol.NULL;
            case NOTNULL:return SqlEquation.Symbol.NOT_NULL;
            default:throw new IllegalArgumentException("Unsupported symbol:" + sqlOperator.name());
        }
    }

    private MultiExpressionCondition transferSqlEquationToCondition(String rootPath, SqlEquation sqlEquation){
        MultiExpressionCondition multiExpressionCondition = new MultiExpressionCondition(sqlEquation.getValueList().size());
        for (int i = 0; i < sqlEquation.getValueList().size(); ++i){
            Integer type = sqlEquation.getTypeList().get(i);
            Object value = sqlEquation.getValueList().get(i);
            switch (type){
                case ValueTypeConstants.COLUMN_NAME_TYPE:
                    multiExpressionCondition.add(translateNameUtils.getColumnBaseEntity((String) value,rootPath,2));
                    break;
                case ValueTypeConstants.SYMBOL_TYPE:
                    multiExpressionCondition.add(value);
                    break;
                case ValueTypeConstants.BASE_VALUE_TYPE:
                case ValueTypeConstants.COLLECTION_TYPE:
                case ValueTypeConstants.ARRAY_TYPE:
                case ValueTypeConstants.NULL_VALUE_MODEL:
                    multiExpressionCondition.add(value);
                    break;
                case ValueTypeConstants.SEARCH_LIST_TYPE:
                    SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) value,translateNameUtils);
                    multiExpressionCondition.add(sqlModelUtils.transferToSelectModel());
                    break;
                case ValueTypeConstants.SQL_EQUATION_MODEL:
                    multiExpressionCondition.add(transferSqlEquationToCondition(rootPath,(SqlEquation) value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported sqlEquation:"+type);
            }
        }
        return multiExpressionCondition;
    }

    /**
     * 获取where拦截器添加的条件
     * @param path 当前mapper路径
     * @param mapper
     * @return 拦截器添加的条件
     */
    public List<Condition> getWhereConditionSqlInterceptorConditionList(String path, FieldColumnRelationMapper mapper){
        List<Condition> conditionList = new ArrayList<>();
        fillWhereConditionSqlInterceptorConditionList(conditionList,path,mapper,mapper.getIds());
        fillWhereConditionSqlInterceptorConditionList(conditionList,path,mapper,mapper.getFieldColumnRelations());
        return conditionList;
    }

    public List<Condition> getUpdateConditionSqlInterceptorConditionList(String path, FieldColumnRelationMapper mapper){
        List<Condition> conditionList = new ArrayList<>();
        fillUpdateConditionSqlInterceptorConditionList(conditionList,path,mapper,mapper.getIds());
        fillUpdateConditionSqlInterceptorConditionList(conditionList,path,mapper,mapper.getFieldColumnRelations());
        return conditionList;
    }

    public void fillWhereConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                              String path, FieldColumnRelationMapper mapper,
                                                              List<FieldColumnRelation> fieldColumnRelationList){
        if(WsListUtils.isEmpty(fieldColumnRelationList)){
            return;
        }
        for (FieldColumnRelation fieldColumnRelation:fieldColumnRelationList){
            AbstractSqlInterceptor interceptor = selectSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            if(interceptor != null && interceptor.useCondition(mapper)){
                Object fillValue = interceptor.selectFill();
                if(fillValue == null){
                    fillValue = SqlCommonConstants.NULL_VALUE;
                }
                conditionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(fieldColumnRelation.getFieldName(),path,2), SqlEquation.Symbol.EQUAL,fillValue)
                );
            }
        }
    }

    public void fillUpdateConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                              String path, FieldColumnRelationMapper mapper,
                                                              List<FieldColumnRelation> fieldColumnRelationList){
        if(WsListUtils.isEmpty(fieldColumnRelationList)){
            return;
        }
        for (FieldColumnRelation fieldColumnRelation:fieldColumnRelationList){
            AbstractSqlInterceptor interceptor = updateSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            if(interceptor != null && interceptor.useCondition(mapper)){
                Object fillValue = interceptor.updateFill();
                if(fillValue == null){
                    fillValue = SqlCommonConstants.NULL_VALUE;
                }
                conditionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(fieldColumnRelation.getFieldName(),path,2), SqlEquation.Symbol.EQUAL,fillValue)
                );
            }
        }
    }







}