package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsCollectionUtils;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.common.model.KeyValue;
import cn.katoumegumi.java.common.model.TripleEntity;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;
import cn.katoumegumi.java.sql.entity.ExistEntityInfo;
import cn.katoumegumi.java.sql.entity.FieldColumnRelationMapperName;
import cn.katoumegumi.java.sql.entity.MapperDictTree;
import cn.katoumegumi.java.sql.entity.ReturnEntityId;
import cn.katoumegumi.java.sql.handler.model.InsertSqlEntity;
import cn.katoumegumi.java.sql.handler.model.SqlParameter;
import cn.katoumegumi.java.sql.handler.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.mapper.model.FieldJoinClass;
import cn.katoumegumi.java.sql.model.component.*;
import cn.katoumegumi.java.sql.model.condition.*;
import cn.katoumegumi.java.sql.model.result.DeleteModel;
import cn.katoumegumi.java.sql.model.result.SelectModel;
import cn.katoumegumi.java.sql.model.result.TableModel;
import cn.katoumegumi.java.sql.model.result.UpdateModel;
import cn.katoumegumi.java.sql.resultSet.WsResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * @author ws
 */
public class SQLModelFactory {

    private static final Logger log = LoggerFactory.getLogger(SQLModelFactory.class);

    /**
     * 插入参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> INSERT_SQL_INTERCEPTOR_MAP = new HashMap<>();
    /**
     * 修改参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> UPDATE_SQL_INTERCEPTOR_MAP = new HashMap<>();
    /**
     * 查询参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> SELECT_SQL_INTERCEPTOR_MAP = new HashMap<>();


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
    private SelectModel cacheSelectModel;

    public SQLModelFactory(MySearchList mySearchList) {
        this(mySearchList, new TranslateNameUtils(), false);
    }

    public SQLModelFactory(MySearchList mySearchList, TranslateNameUtils translateNameUtils) {
        this(mySearchList, translateNameUtils, true);
    }

    public SQLModelFactory(MySearchList mySearchList, TranslateNameUtils parentTranslateNameUtils, boolean isNested) {
        this.mySearchList = mySearchList;
        this.translateNameUtils = isNested ? new TranslateNameUtils(parentTranslateNameUtils) : parentTranslateNameUtils;
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
        if (WsCollectionUtils.isNotEmpty(ids)) {
            for (FieldColumnRelation relation : ids) {
                if (WsBeanUtils.isBaseType(relation.getBeanProperty().getPropertyClass())) {
                    Object value = relation.getBeanProperty().getValue(o);
                    if (value != null){
                        mySearchList.eq(relation.getBeanProperty().getPropertyName(), value);
                    }
                }
            }
        }
        if (WsCollectionUtils.isNotEmpty(columns)) {
            for (FieldColumnRelation relation : columns) {
                if (WsBeanUtils.isBaseType(relation.getBeanProperty().getPropertyClass())) {
                    Object value = relation.getBeanProperty().getValue(o);
                    if (value != null){
                        mySearchList.eq(relation.getBeanProperty().getPropertyName(), value);
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
            INSERT_SQL_INTERCEPTOR_MAP.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isUpdate()) {
            UPDATE_SQL_INTERCEPTOR_MAP.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isSelect()) {
            SELECT_SQL_INTERCEPTOR_MAP.put(sqlInterceptor.fieldName(), sqlInterceptor);
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
    public <T> InsertSqlEntity createInsertSqlEntity(T t) {
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mainClass);
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<FieldColumnRelation> validList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();

        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();

        for (FieldColumnRelation fieldColumnRelation : idList) {
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            Object o = beanPropertyModel.getValue(t);
            if (o != null) {
                columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                validList.add(fieldColumnRelation);
                valueList.add(new SqlParameter(o));
            }
        }


        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            Object o;
            AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
            if (sqlInterceptor != null && sqlInterceptor.useCondition(analysisClassRelation(mainClass))) {
                o = sqlInterceptor.insertFill();
            } else {
                o = beanPropertyModel.getValue(t);
            }
            if (o != null) {
                columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                validList.add(fieldColumnRelation);
                valueList.add(new SqlParameter(o));
            }
        }

        String insertSql = SqlCommonConstants.INSERT_INTO + guardKeyword(fieldColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(placeholderList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS;
        InsertSqlEntity entity = new InsertSqlEntity(insertSql);
        //entity.setInsertSql(insertSql);
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
    public <T> InsertSqlEntity createInsertSqlEntity(List<T> tList) {
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
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            Object o = beanPropertyModel.getValue(tList.get(0));
            validField.add(fieldColumnRelation);
            columnNameList.add(guardKeyword(fieldColumnRelation.getColumnName()));
            placeholderList.add(SqlCommonConstants.PLACEHOLDER);
            valueList.add(new SqlParameter(o));
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
            Object o;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.insertFill();
            } else {
                o = beanPropertyModel.getValue(tList.get(0));
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
                BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
                AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
                Object o;
                if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                    o = sqlInterceptor.insertFill();
                } else {
                    o = beanPropertyModel.getValue(tList.get(i));
                }
                valueList.add(new SqlParameter(o));
            }
            placeholderList.add(placeholderSql);
        }

        String insertSql = SqlCommonConstants.INSERT_INTO + guardKeyword(fieldColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + WsStringUtils.jointListString(placeholderList, ",");
        InsertSqlEntity insertSqlEntity = new InsertSqlEntity(insertSql);
        //insertSqlEntity.setInsertSql(insertSql);
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
    public <T> UpdateSqlEntity createUpdateSqlEntity(T t, boolean isAll) {
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(t.getClass());
        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();
        List<FieldColumnRelation> columnList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<String> columnStrList = new ArrayList<>();
        List<String> idStrList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();

        List<FieldColumnRelation> validColumnList = new ArrayList<>();
        List<FieldColumnRelation> validIdList = new ArrayList<>();

        for (FieldColumnRelation fieldColumnRelation : columnList) {
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            AbstractSqlInterceptor sqlInterceptor = UPDATE_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
            Object o;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.updateFill();
            } else {
                o = beanPropertyModel.getValue(t);
            }
            if (isAll || o != null) {
                String str = guardKeyword(fieldColumnRelation.getColumnName()) + SqlCommonConstants.EQ + SqlCommonConstants.PLACEHOLDER;
                columnStrList.add(str);
                valueList.add(new SqlParameter(o));
                validColumnList.add(fieldColumnRelation);
            }
        }
        for (FieldColumnRelation fieldColumnRelation : idList) {
            BeanPropertyModel beanPropertyModel = fieldColumnRelation.getBeanProperty();
            Object o = beanPropertyModel.getValue(t);
            if (o != null) {
                String str = guardKeyword(fieldColumnRelation.getColumnName()) + SqlCommonConstants.EQ + SqlCommonConstants.PLACEHOLDER;
                idStrList.add(str);
                valueList.add(new SqlParameter(o));
                validIdList.add(fieldColumnRelation);
            }
        }
        if (idStrList.isEmpty()) {
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
        if (WsCollectionUtils.isEmpty(mySearchList.getColumnNameList())) {
            return oneLoopMargeMap2(resultSet);
        } else {
            List<Object> tList = new ArrayList<>();
            TableColumn tableColumn = this.cacheSelectModel.getSelect().get(0);
            try {
                while (resultSet.next()) {
                    Object o = WsBeanUtils.objectToT(resultSet.getObject(1), tableColumn.getBeanProperty().getPropertyClass());
                    if (o != null) {
                        tList.add(o);
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            return (List<T>) tList;
        }
    }


    public <T> List<T> oneLoopMargeMap2(WsResultSet resultSet) {
        try {
            final int length = resultSet.getColumnCount();
            if (length == 0) {
                return new ArrayList<>(0);
            }


            FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
            String rootPath = mainMapper.getNickName();


            List<FieldColumnRelation> columnRelationList = new ArrayList<>(length);
            List<int[][]> localtionList = new ArrayList<>();
            Map<String, FieldColumnRelationMapperName> abbreviationAndMapperNameMap = new HashMap<>();
            String columnNameTemp;
            List<String> nameListTemp;
            String mapperName;
            FieldColumnRelationMapper mapperTemp;
            FieldColumnRelation fieldColumnRelationTemp;
            FieldColumnRelationMapperName fieldColumnRelationMapperNameTemp;
            //0是id的坐标 1是非id坐标
            int[][] locationListTemp;
            int mapperNameSign = 0;
            Map<String, Integer> mapperNameAndSignMap = new HashMap<>();

            int mapperNameIndex = 0;

            for (int i = 0; i < length; i++) {
                columnNameTemp = resultSet.getColumnLabel(i + 1);
                nameListTemp = WsStringUtils.split(columnNameTemp, '.');
                //获取映射名称
                fieldColumnRelationMapperNameTemp = abbreviationAndMapperNameMap.get(nameListTemp.get(0));
                if (fieldColumnRelationMapperNameTemp == null) {
                    mapperName = translateNameUtils.getParticular(nameListTemp.get(0));
                    mapperTemp = translateNameUtils.getLocalMapper(mapperName);
                    fieldColumnRelationMapperNameTemp = new FieldColumnRelationMapperName(mapperNameIndex++, nameListTemp.get(0), mapperName,mapperTemp);
                    mapperNameSign = fieldColumnRelationMapperNameTemp.setCompleteNameSplitSignNameList(mapperNameAndSignMap, mapperNameSign);
                    abbreviationAndMapperNameMap.put(fieldColumnRelationMapperNameTemp.getAbbreviation(), fieldColumnRelationMapperNameTemp);
                }else {
                    mapperTemp = fieldColumnRelationMapperNameTemp.getMapper();
                }

                //获取位置数组
                if (fieldColumnRelationMapperNameTemp.getIndex() >= localtionList.size()) {
                    locationListTemp = new int[2][];
                    locationListTemp[0] = new int[mapperTemp.getIds().size()];
                    locationListTemp[1] = new int[mapperTemp.getFieldColumnRelations().size()];
                    localtionList.add(locationListTemp);
                } else {
                    locationListTemp = localtionList.get(fieldColumnRelationMapperNameTemp.getIndex());
                }

                //填写位置数组
                fieldColumnRelationTemp = mapperTemp.getFieldColumnRelationByFieldName(nameListTemp.get(1));
                if (fieldColumnRelationTemp.isId()) {
                    locationListTemp[0][mapperTemp.getLocation(fieldColumnRelationTemp)] = i;
                } else {
                    locationListTemp[1][mapperTemp.getLocation(fieldColumnRelationTemp)] = i;
                }
                columnRelationList.add(fieldColumnRelationTemp);
            }

            MapperDictTree mapperDictTree = new MapperDictTree();
            for (FieldColumnRelationMapperName name : abbreviationAndMapperNameMap.values()) {
                mapperDictTree.add(name);
            }

            //判断是否需要进行数据合并
            boolean hasArray = mapperDictTree.checkNeedMergeAndBuild();


            //获取root类
            mapperDictTree = mapperDictTree.getChildMap().get(mapperNameAndSignMap.get(rootPath));

            List<Object> valueList = new ArrayList<>();

            ExistEntityInfo existEntityInfo = hasArray ? new ExistEntityInfo() : null;

            Object value;
            while (resultSet.next()) {
                value = createNeedMergeValue(resultSet, existEntityInfo, mapperDictTree, localtionList, columnRelationList,hasArray);
                if (value != null) {
                    valueList.add(value);
                }
            }
            return (List<T>) valueList;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    private static Object createNeedMergeValue(WsResultSet resultSet, ExistEntityInfo parentExistEntityInfo, MapperDictTree mapperDictTree, List<int[][]> locationList, List<FieldColumnRelation> columnRelationList,boolean isArray) throws SQLException {
        if (parentExistEntityInfo == null) {
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
        if (!isArray) {
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
        FieldColumnRelationMapper mapper = mapperDictTree.getCurrentMapperName().getMapper();
        if (mapper.getIds().isEmpty()) {
            //没有id数据不能合并
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
        FieldColumnRelationMapperName mapperName = mapperDictTree.getCurrentMapperName();
        int[][] location = locationList.get(mapperName.getIndex());
        Object[] ids = new Object[mapper.getIds().size()];
        boolean isNotNullObject = false;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = resultSet.getObject(location[0][i] + 1);
            if (ids[i] != null) {
                isNotNullObject = true;
            }
        }
        if (!isNotNullObject) {
            //id为空,后续数据不合并
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
        ReturnEntityId returnEntityId = new ReturnEntityId(ids);
        Map<ReturnEntityId, TripleEntity<Object,Object[],ExistEntityInfo[]>> valueMap = parentExistEntityInfo.getExistMap();
        TripleEntity<Object,Object[],ExistEntityInfo[]> tripleEntity = valueMap.get(returnEntityId);
        Object value;
        boolean isNotExist = tripleEntity == null;
        if (isNotExist) {
            value = WsBeanUtils.createObject(mapper.getClazz());
            fillObjectValue(value, ids, location[0], columnRelationList);
            fillObjectValue(value, resultSet, location[1], columnRelationList);
            if(mapperDictTree.getChildMap().isEmpty()){
                tripleEntity = new TripleEntity<>(value,null,null);
            }else {
                tripleEntity = new TripleEntity<>(value, new Object[mapperDictTree.getChildMap().size()],new ExistEntityInfo[mapperDictTree.getChildMap().size()]);

            }
            valueMap.put(returnEntityId, tripleEntity);
        } else {
            value = tripleEntity.getKey();
        }

        Object subValue;
        ExistEntityInfo subExistEntityInfo;
        Object[] cacheSubValue = tripleEntity.getLeft();
        ExistEntityInfo[] subExistEntityInfoArray = tripleEntity.getRight();
        for (int i = 0; i < mapperDictTree.getChildMap().size(); i++) {
            MapperDictTree subTree = mapperDictTree.getMapperDictTrees()[i];
            FieldJoinClass fieldJoinClass = mapperDictTree.getFieldJoinClasses()[i];

            if(subExistEntityInfoArray[i] == null){
                subExistEntityInfo = new ExistEntityInfo();
                subExistEntityInfoArray[i] = subExistEntityInfo;
            }else {
                subExistEntityInfo = subExistEntityInfoArray[i];
            }

            if (cacheSubValue[i] == SqlCommonConstants.NULL_VALUE) {
                continue;
            }
            subValue = createNeedMergeValue(resultSet, subExistEntityInfo, subTree, locationList, columnRelationList,mapperDictTree.isHasArray());
            if (cacheSubValue[i] == null) {
                //第一次获取值
                if (fieldJoinClass.isArray()) {
                    if (subValue != null) {
                        List<Object> list = new ArrayList<>();
                        list.add(subValue);
                        cacheSubValue[i] = list;
                        fieldJoinClass.getBeanProperty().setValue(value,list);
                        //WsReflectUtils.setValue(value, list, fieldJoinClass.getField());
                    }
                } else {
                    if (subValue == null) {
                        cacheSubValue[i] = SqlCommonConstants.NULL_VALUE;
                    } else {
                        cacheSubValue[i] = subValue;
                        fieldJoinClass.getBeanProperty().setValue(value,subValue);
                        //WsReflectUtils.setValue(value, subValue, fieldJoinClass.getField());
                    }
                }
            } else {
                if (fieldJoinClass.isArray() && subValue != null) {
                    List<Object> list = (List<Object>) cacheSubValue[i];
                    list.add(subValue);
                }
            }
        }

        if (isNotExist) {
            return value;
        }
        return null;
    }


    /**
     * 创建对象
     *
     * @param resultSet
     * @param mapperDictTree
     * @param locationList
     * @param columnRelationList
     * @return
     * @throws SQLException
     */
    private static Object createValue(WsResultSet resultSet, MapperDictTree mapperDictTree, List<int[][]> locationList, List<FieldColumnRelation> columnRelationList) throws SQLException {
        FieldColumnRelationMapper mapper = mapperDictTree.getCurrentMapperName().getMapper();
        FieldColumnRelationMapperName mapperName = mapperDictTree.getCurrentMapperName();
        int[][] location = locationList.get(mapperName.getIndex());
        Object value = WsBeanUtils.createObject(mapper.getClazz());
        boolean isNotNull = fillObjectValue(value, resultSet, location[0], columnRelationList) | fillObjectValue(value, resultSet, location[1], columnRelationList);
        for (int i = 0; i < mapperDictTree.getChildMap().size(); i++) {

            FieldJoinClass fieldJoinClass = mapperDictTree.getFieldJoinClasses()[i];
            Object joinValue = createValue(resultSet, mapperDictTree.getMapperDictTrees()[i], locationList, columnRelationList);
            if (joinValue != null) {
                isNotNull = true;
                if (fieldJoinClass.isArray()) {
                    List<Object> list = new ArrayList<>(1);
                    list.add(joinValue);
                    fieldJoinClass.getBeanProperty().setValue(value,list);
                } else {
                    fieldJoinClass.getBeanProperty().setValue(value,joinValue);
                }
            }


        }
        if (isNotNull) {
            return value;
        }
        return null;
    }

    private static boolean fillObjectValue(Object o, Object[] values, int[] location, List<FieldColumnRelation> columnRelationList) {
        boolean isAdd = false;
        for (int i = 0; i < location.length; i++) {
            FieldColumnRelation fieldColumnRelationTemp = columnRelationList.get(location[i]);
            Object value = values[i];
            if (value == null) {
                continue;
            }
            isAdd = true;
            setValue(o,value,fieldColumnRelationTemp.getBeanProperty());
        }
        return isAdd;
    }

    private static boolean fillObjectValue(Object o, WsResultSet resultSet, int[] location, List<FieldColumnRelation> columnRelationList) throws SQLException {
        boolean isAdd = false;
        for (int j : location) {
            FieldColumnRelation fieldColumnRelationTemp = columnRelationList.get(j);
            Object value = resultSet.getObject(j + 1);
            if (value == null) {
                continue;
            }
            isAdd = true;
            setValue(o,value,fieldColumnRelationTemp.getBeanProperty());
        }
        return isAdd;
    }

    private static void setValue(Object target, Object source, BeanPropertyModel beanPropertyModel){
        if (source instanceof byte[]) {
            source = new String((byte[]) source);
        }
        source = WsBeanUtils.objectToT(source,beanPropertyModel.getPropertyClass());
        beanPropertyModel.setValue(target,source);
    }

    public TranslateNameUtils getTranslateNameUtils() {
        return translateNameUtils;
    }


    /**
     * MySearchList转换为SelectModel
     *
     * @return
     */
    public SelectModel createSelectModel() {
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath, mainMapper);
        final boolean appointQueryColumn = WsCollectionUtils.isNotEmpty(this.mySearchList.getColumnNameList());
        List<TableColumn> queryColumnList = new ArrayList<>();

        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper, queryColumnList, appointQueryColumn, false);

        if (appointQueryColumn) {
            if (this.mySearchList.isSingleColumn()) {
                //是单列查询自动加入distinct关键字去重
                BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity(this.mySearchList.getColumnNameList().get(0), rootPath, 2);
                DynamicTableColumn dynamicTableColumn = new DynamicTableColumn(
                        new SqlFunctionCondition(false, "distinct ", baseTableColumn),
                        baseTableColumn
                );
                queryColumnList.add(dynamicTableColumn);
            } else {
                for (String columnName : this.mySearchList.getColumnNameList()) {
                    queryColumnList.add(translateNameUtils.getColumnBaseEntity(columnName, rootPath, 2));
                }
            }

        }

        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);


        List<OrderByCondition> orderByConditionList = null;
        if (WsCollectionUtils.isNotEmpty(this.mySearchList.getOrderSearches())) {
            orderByConditionList = new ArrayList<>(this.mySearchList.getOrderSearches().size());
            for (MySearch mySearch : this.mySearchList.getOrderSearches()) {
                if (mySearch.getFieldName().charAt(mySearch.getFieldName().length() - 1) == SqlCommonConstants.RIGHT_BRACKETS) {
                    orderByConditionList.add(new OrderByCondition(new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, mySearch.getFieldName()), null), WsBeanUtils.objectToT(mySearch.getValue(), String.class)));
                } else {
                    orderByConditionList.add(new OrderByCondition(translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), rootPath, 2), WsBeanUtils.objectToT(mySearch.getValue(), String.class)));
                }
            }
        }
        SelectModel selectModel = new SelectModel(queryColumnList, from, joinTableModelList, where, orderByConditionList, this.mySearchList.getSqlLimit());
        this.cacheSelectModel = selectModel;
        return selectModel;
    }

    /**
     * MySearchList转换为DeleteModel
     *
     * @return
     */
    public DeleteModel createDeleteModel() {
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath, mainMapper);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper, null, true, true);
        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);
        return new DeleteModel(from, joinTableModelList, where);
    }

    /**
     * MySearchList转换为UpdateModel
     *
     * @return
     */
    public UpdateModel createUpdateModel() {
        final FieldColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        List<MySearch> updateSearchList = this.mySearchList.filterUpdateSearch();
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAbbreviation(mainMapper.getNickName()));
        final String rootPath = mainMapper.getNickName();
        translateNameUtils.addLocalMapper(rootPath, mainMapper);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper, null, true, true);
        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);
        List<Condition> conditionList = searchToExpressionCondition(rootPath, updateSearchList, null);
        if (WsCollectionUtils.isEmpty(conditionList)) {
            throw new NullPointerException("update condition is null");
        }
        List<Condition> updateInterceptorConditionList = getUpdateConditionSqlInterceptorConditionList(rootPath, mainMapper);
        if (WsCollectionUtils.isNotEmpty(updateInterceptorConditionList)) {
            conditionList.addAll(updateInterceptorConditionList);
        }
        return new UpdateModel(from, joinTableModelList, conditionList, where);
    }

    /**
     * 表关联处理
     *
     * @param rootPath               根路径
     * @param mainMapper             主映射
     * @param queryColumnList        存储查询列的队列
     * @param appointQueryColumn     是否指定了需要查询的列
     * @param ignoreDefaultJoinTable 是否忽略默认关联的表
     * @return
     */
    private List<JoinTableModel> handleJoinTableModel(final String rootPath, final FieldColumnRelationMapper mainMapper, final List<TableColumn> queryColumnList, final boolean appointQueryColumn, final boolean ignoreDefaultJoinTable) {
        final List<TableRelation> tableRelationList = this.mySearchList.getJoins();
        final List<JoinTableModel> joinTableModelList = new ArrayList<>();
        final Map<TableRelation, JoinTableModel> usedRelationMap = new HashMap<>();
        if (!ignoreDefaultJoinTable) {
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

            while (!queue.isEmpty()) {
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
                    if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
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
                    conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(mapper.getFieldColumnRelationByColumn(fieldJoinClass.getJoinColumn()).getBeanProperty().getPropertyName(), path, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(joinMapper.getFieldColumnRelationByColumn(fieldJoinClass.getAnotherJoinColumn()).getBeanProperty().getPropertyName(), joinPath, 2)));
                    if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
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

        for (TableRelation tableRelation : tableRelationList) {
            JoinTableModel joinTableModel = usedRelationMap.get(tableRelation);
            if (joinTableModel == null) {
                String path = tableRelation.getTableNickName() == null ? rootPath : rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableRelation.getTableNickName();
                String joinPath = tableRelation.getJoinTableNickName() == null ? rootPath : rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableRelation.getJoinTableNickName();
                FieldColumnRelationMapper joinMapper = analysisClassRelation(tableRelation.getJoinTableClass());
                translateNameUtils.addLocalMapper(joinPath, joinMapper);
                FieldColumnRelationMapper mapper = tableRelation.getTableNickName() == null ? mainMapper : translateNameUtils.getLocalMapper(path);
                List<Condition> conditionModelList = new ArrayList<>();
                conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(tableRelation.getTableColumn(), path, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(tableRelation.getJoinTableColumn(), joinPath, 2)));
                List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath, joinMapper);
                if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
                    conditionModelList.addAll(selectInterceptorConditionList);
                }
                if (tableRelation.getConditionSearchList() != null) {
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath, tableRelation.getConditionSearchList(), SqlOperator.AND);
                    if (relationCondition != null && WsCollectionUtils.isNotEmpty(relationCondition.getConditionList())) {
                        conditionModelList.addAll(relationCondition.getConditionList());
                    }

                }
                joinTableModelList.add(new JoinTableModel(new TableModel(mapper, translateNameUtils.getCurrentAbbreviation(path)), new TableModel(joinMapper, translateNameUtils.getCurrentAbbreviation(joinPath)), tableRelation.getJoinType(), new RelationCondition(conditionModelList, SqlOperator.AND)));
            } else {
                if (tableRelation.getConditionSearchList() != null) {
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath, tableRelation.getConditionSearchList(), SqlOperator.AND);
                    if (relationCondition != null && WsCollectionUtils.isNotEmpty(relationCondition.getConditionList())) {
                        joinTableModel.getOn().getConditionList().addAll(relationCondition.getConditionList());
                    }
                }
            }
        }
        return joinTableModelList;
    }

    /**
     * 处理where条件（必须在处理表关联关系之后进行）
     *
     * @param rootPath
     * @param mainMapper
     * @param mySearchList
     * @return
     */
    private RelationCondition handleWhere(final String rootPath, final FieldColumnRelationMapper mainMapper, final MySearchList mySearchList) {
        RelationCondition where = searchListToConditionRelation(rootPath, mySearchList, SqlOperator.AND);
        List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(rootPath, mainMapper);
        if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
            if (where == null) {
                where = new RelationCondition(selectInterceptorConditionList, SqlOperator.AND);
            } else {
                where.getConditionList().addAll(selectInterceptorConditionList);
            }
        }
        return where;
    }

    /**
     * 解析需要查询的列名
     *
     * @param mapper
     * @param path
     * @param queryColumnList
     */
    private void addQueryColumnList(final FieldColumnRelationMapper mapper, final String path, final List<TableColumn> queryColumnList) {
        if (WsCollectionUtils.isNotEmpty(mapper.getIds())) {
            for (FieldColumnRelation fieldColumnRelation : mapper.getIds()) {
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(fieldColumnRelation, mapper, path));
            }
        }
        if (WsCollectionUtils.isNotEmpty(mapper.getFieldColumnRelations())) {
            for (FieldColumnRelation fieldColumnRelation : mapper.getFieldColumnRelations()) {
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(fieldColumnRelation, mapper, path));
            }
        }
    }


    private RelationCondition searchListToConditionRelation(final String rootPath, final MySearchList searchList, final SqlOperator relation) {
        if (WsCollectionUtils.isEmpty(searchList.getAll()) && WsCollectionUtils.isEmpty(searchList.getAnds()) && WsCollectionUtils.isEmpty(searchList.getOrs())) {
            return null;
        }
        RelationCondition where = new RelationCondition(searchToExpressionCondition(rootPath, searchList.getAll(), null), relation);

        if (WsCollectionUtils.isNotEmpty(searchList.getAnds())) {
            RelationCondition andWhere = new RelationCondition(new ArrayList<>(), SqlOperator.AND);
            for (MySearchList and : searchList.getAnds()) {
                RelationCondition childCondition = searchListToConditionRelation(rootPath, and, SqlOperator.AND);
                if (childCondition != null) {
                    andWhere.getConditionList().add(childCondition);
                }
            }
            if (!WsCollectionUtils.isEmpty(andWhere.getConditionList())) {
                where.getConditionList().add(andWhere);
            }
        }
        if (WsCollectionUtils.isNotEmpty(searchList.getOrs())) {
            RelationCondition orWhere = new RelationCondition(new ArrayList<>(), SqlOperator.OR);
            for (MySearchList and : searchList.getOrs()) {
                RelationCondition childCondition = searchListToConditionRelation(rootPath, and, SqlOperator.AND);
                if (childCondition != null) {
                    orWhere.getConditionList().add(childCondition);
                }
            }
            if (!WsCollectionUtils.isEmpty(orWhere.getConditionList())) {
                where.getConditionList().add(orWhere);
            }
        }
        return where;
    }

    /**
     * mysearch转condition
     *
     * @param rootPath
     * @param searches
     * @param expressionConditionList
     * @return
     */
    private List<Condition> searchToExpressionCondition(String rootPath, List<MySearch> searches, List<Condition> expressionConditionList) {
        if (expressionConditionList == null) {
            expressionConditionList = new ArrayList<>(searches.size());
        }
        for (MySearch search : searches) {
            Object left;
            SqlOperator operator = search.getOperator();

            Object right = null;
            switch (operator) {
                case EQP:
                case NEP:
                case GTP:
                case LTP:
                case GTEP:
                case LTEP:
                    left = translateNameUtils.getColumnBaseEntity(search.getFieldName(), rootPath, 2);
                    right = translateNameUtils.getColumnBaseEntity((String) search.getValue(), rootPath, 2);
                    break;
                case SQL:
                    left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, search.getFieldName()), search.getValue());
                    break;
                case EXISTS:
                case NOT_EXISTS:
                    if (search.getValue() instanceof MySearchList) {
                        //right = search.getValue();
                        left = search.getValue();
                    } else {
                        left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, search.getFieldName()), search.getValue());
                    }
                    break;
                case EQUATION:
                    SqlEquation sqlEquation = (SqlEquation) search.getValue();
                    expressionConditionList.add(transferSqlEquationToCondition(rootPath, sqlEquation));
                    continue;
                case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                    BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity(search.getFieldName(), rootPath, 2);
                    MultiExpressionCondition multiExpressionCondition = new MultiExpressionCondition(5);
                    multiExpressionCondition.add(baseTableColumn)
                            .add(SqlEquation.Symbol.EQUAL);
                    if (WsReflectUtils.classCompare(baseTableColumn.getBeanProperty().getPropertyClass(), Number.class)) {
                        multiExpressionCondition.add(new SqlFunctionCondition("IFNULL", baseTableColumn, 0));
                    } else {
                        multiExpressionCondition.add(baseTableColumn);
                    }
                    multiExpressionCondition.add(sqlOperatorToSymbol(operator))
                            .add(search.getValue() == null ? SqlCommonConstants.NULL_VALUE : search.getValue());
                    expressionConditionList.add(multiExpressionCondition);
                    continue;
                case SET:
                    operator = SqlOperator.EQ;
                default:
                    left = translateNameUtils.getColumnBaseEntity(search.getFieldName(), rootPath, 2);
                    right = search.getValue();
                    if (right == null) {
                        right = SqlCommonConstants.NULL_VALUE;
                    }
                    break;
            }
            if (right instanceof MySearchList) {
                SQLModelFactory sqlModelFactory = new SQLModelFactory((MySearchList) right, translateNameUtils);
                right = sqlModelFactory.createSelectModel();
            }
            if (left instanceof MySearchList) {
                SQLModelFactory sqlModelFactory = new SQLModelFactory((MySearchList) left, translateNameUtils);
                left = sqlModelFactory.createSelectModel();
            }
            SqlEquation.Symbol symbol = sqlOperatorToSymbol(operator);
            expressionConditionList.add(new SingleExpressionCondition(left, symbol, right));
        }

        return expressionConditionList;
    }

    private SqlEquation.Symbol sqlOperatorToSymbol(SqlOperator sqlOperator) {
        switch (sqlOperator) {
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
            case LIKE:
                return SqlEquation.Symbol.LIKE;
            case IN:
                return SqlEquation.Symbol.IN;
            case NIN:
                return SqlEquation.Symbol.NOT_IN;
            case EXISTS:
                return SqlEquation.Symbol.EXISTS;
            case NOT_EXISTS:
                return SqlEquation.Symbol.NOT_EXISTS;
            case BETWEEN:
                return SqlEquation.Symbol.BETWEEN;
            case NOT_BETWEEN:
                return SqlEquation.Symbol.NOT;
            case SQL:
                return SqlEquation.Symbol.SQL;
            case ADD:
                return SqlEquation.Symbol.ADD;
            case SUBTRACT:
                return SqlEquation.Symbol.SUBTRACT;
            case MULTIPLY:
                return SqlEquation.Symbol.MULTIPLY;
            case DIVIDE:
                return SqlEquation.Symbol.DIVIDE;
            case SET:
                return SqlEquation.Symbol.SET;
            case NULL:
                return SqlEquation.Symbol.NULL;
            case NOTNULL:
                return SqlEquation.Symbol.NOT_NULL;
            default:
                throw new IllegalArgumentException("Unsupported symbol:" + sqlOperator.name());
        }
    }

    private MultiExpressionCondition transferSqlEquationToCondition(String rootPath, SqlEquation sqlEquation) {
        MultiExpressionCondition multiExpressionCondition = new MultiExpressionCondition(sqlEquation.getValueList().size());
        for (int i = 0; i < sqlEquation.getValueList().size(); ++i) {
            Integer type = sqlEquation.getTypeList().get(i);
            Object value = sqlEquation.getValueList().get(i);
            switch (type) {
                case ValueTypeConstants.COLUMN_NAME_TYPE:
                    multiExpressionCondition.add(translateNameUtils.getColumnBaseEntity((String) value, rootPath, 2));
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
                    SQLModelFactory sqlModelFactory = new SQLModelFactory((MySearchList) value, translateNameUtils);
                    multiExpressionCondition.add(sqlModelFactory.createSelectModel());
                    break;
                case ValueTypeConstants.SQL_EQUATION_MODEL:
                    multiExpressionCondition.add(transferSqlEquationToCondition(rootPath, (SqlEquation) value));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported sqlEquation:" + type);
            }
        }
        return multiExpressionCondition;
    }

    /**
     * 获取where拦截器添加的条件
     *
     * @param path   当前mapper路径
     * @param mapper
     * @return 拦截器添加的条件
     */
    public List<Condition> getWhereConditionSqlInterceptorConditionList(String path, FieldColumnRelationMapper mapper) {
        List<Condition> conditionList = new ArrayList<>();
        fillWhereConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getIds());
        fillWhereConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getFieldColumnRelations());
        return conditionList;
    }

    public List<Condition> getUpdateConditionSqlInterceptorConditionList(String path, FieldColumnRelationMapper mapper) {
        List<Condition> conditionList = new ArrayList<>();
        fillUpdateConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getIds());
        fillUpdateConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getFieldColumnRelations());
        return conditionList;
    }

    public void fillWhereConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                              String path, FieldColumnRelationMapper mapper,
                                                              List<FieldColumnRelation> fieldColumnRelationList) {
        if (WsCollectionUtils.isEmpty(fieldColumnRelationList)) {
            return;
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            AbstractSqlInterceptor interceptor = SELECT_SQL_INTERCEPTOR_MAP.get(fieldColumnRelation.getBeanProperty().getPropertyName());
            if (interceptor != null && interceptor.useCondition(mapper)) {
                Object fillValue = interceptor.selectFill();
                if (fillValue == null) {
                    fillValue = SqlCommonConstants.NULL_VALUE;
                }
                conditionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(fieldColumnRelation.getBeanProperty().getPropertyName(), path, 2), SqlEquation.Symbol.EQUAL, fillValue)
                );
            }
        }
    }

    public void fillUpdateConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                               String path, FieldColumnRelationMapper mapper,
                                                               List<FieldColumnRelation> fieldColumnRelationList) {
        if (WsCollectionUtils.isEmpty(fieldColumnRelationList)) {
            return;
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            AbstractSqlInterceptor interceptor = UPDATE_SQL_INTERCEPTOR_MAP.get(fieldColumnRelation.getBeanProperty().getPropertyName());
            if (interceptor != null && interceptor.useCondition(mapper)) {
                Object fillValue = interceptor.updateFill();
                if (fillValue == null) {
                    fillValue = SqlCommonConstants.NULL_VALUE;
                }
                conditionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(fieldColumnRelation.getBeanProperty().getPropertyName(), path, 2), SqlEquation.Symbol.EQUAL, fillValue)
                );
            }
        }
    }


}