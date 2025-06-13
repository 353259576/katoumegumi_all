package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsCollectionUtils;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.common.model.KeyValue;
import cn.katoumegumi.java.common.model.TripleEntity;
import cn.katoumegumi.java.sql.common.OrderByTypeEnums;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;
import cn.katoumegumi.java.sql.handler.model.InsertSqlEntity;
import cn.katoumegumi.java.sql.handler.model.SqlParameter;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;
import cn.katoumegumi.java.sql.mapper.model.PropertyObjectColumnJoinRelation;
import cn.katoumegumi.java.sql.model.base.ExistEntityInfo;
import cn.katoumegumi.java.sql.model.base.FieldColumnRelationMapperName;
import cn.katoumegumi.java.sql.model.base.MapperDictTree;
import cn.katoumegumi.java.sql.model.base.ReturnEntityId;
import cn.katoumegumi.java.sql.model.component.*;
import cn.katoumegumi.java.sql.model.condition.*;
import cn.katoumegumi.java.sql.model.query.QueryColumn;
import cn.katoumegumi.java.sql.model.query.QueryElement;
import cn.katoumegumi.java.sql.model.query.QuerySqlString;
import cn.katoumegumi.java.sql.model.result.*;
import cn.katoumegumi.java.sql.resultSet.WsResultSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * @author ws
 */
public class SQLModelFactory {

    private static final Log log = LogFactory.getLog(SQLModelFactory.class);

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
     * 表名和列名转换
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
        PropertyColumnRelationMapper mapper = analysisClassRelation(mainClass);
        String rootPath = mapper.getEntityName();
        //设置root path
        if (WsStringUtils.isBlank(mySearchList.getAlias())) {
            translateNameUtils.setAlias(rootPath);
        } else {
            translateNameUtils.setAlias(rootPath, mySearchList.getAlias());
        }
        //检查是否是相对路径，如果不是就改为相对路径
        translateNameUtils.addRootPathPrefix(rootPath);
        String mainEntityPath;
        String joinEntityPath;
        for (TableRelation relation : mySearchList.getJoins()) {
            mainEntityPath = relation.getMainEntityPath();
            if (WsStringUtils.isNotBlank(mainEntityPath)) {
                mainEntityPath = translateNameUtils.translateToEntityName(mainEntityPath);
                if (mainEntityPath.startsWith(rootPath)) {
                    if (mainEntityPath.length() == rootPath.length()) {
                        mainEntityPath = null;
                    } else {
                        mainEntityPath = mainEntityPath.substring(rootPath.length() + 1);
                    }
                }
                relation.setMainEntityPath(mainEntityPath);
            } else {
                relation.setMainEntityPath(null);
            }
            if (WsStringUtils.isNotBlank(relation.getJoinEntityPath())) {
                joinEntityPath = translateNameUtils.translateToEntityName(relation.getJoinEntityPath());
                String relativePath = translateNameUtils.getRelativePath(joinEntityPath);
                relation.setJoinEntityPath(relativePath);
                if (relativePath.length() == joinEntityPath.length()) {
                    joinEntityPath = rootPath + '.' + relativePath;
                }
                if (WsStringUtils.isBlank(relation.getAlias())) {
                    relation.setAlias(translateNameUtils.createAlias(joinEntityPath));
                }
                translateNameUtils.setAlias(joinEntityPath, relation.getAlias());
            }
        }
    }


    public static PropertyColumnRelationMapper analysisClassRelation(Class<?> mainClass) {
        return FieldColumnRelationMapperFactory.analysisClassRelation(mainClass);
    }


    /**
     * 忽略数据库关键词
     *
     * @param keyword
     * @return
     */
    public static String ignoreKeyword(String keyword) {
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
        PropertyColumnRelationMapper mapper = analysisClassRelation(o.getClass());
        List<PropertyBaseColumnRelation> ids = mapper.getIds();
        List<PropertyBaseColumnRelation> columns = mapper.getFieldColumnRelations();
        MySearchList mySearchList = MySearchList.create(o.getClass());
        if (WsCollectionUtils.isNotEmpty(ids)) {
            for (PropertyBaseColumnRelation relation : ids) {
                if (WsBeanUtils.isBaseType(relation.getBeanProperty().getPropertyClass())) {
                    Object value = relation.getBeanProperty().getValue(o);
                    if (value != null) {
                        mySearchList.eq(relation.getBeanProperty().getPropertyName(), value);
                    }
                }
            }
        }
        if (WsCollectionUtils.isNotEmpty(columns)) {
            for (PropertyBaseColumnRelation relation : columns) {
                if (WsBeanUtils.isBaseType(relation.getBeanProperty().getPropertyClass())) {
                    Object value = relation.getBeanProperty().getValue(o);
                    if (value != null) {
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
    public static PropertyColumnRelationMapper getFieldColumnRelationMapper(Class<?> clazz) {
        return FieldColumnRelationMapperFactory.analysisClassRelation(clazz);
    }

    private static Object createNeedMergeValue(WsResultSet resultSet, ExistEntityInfo parentExistEntityInfo, MapperDictTree mapperDictTree, List<int[][]> locationList, List<PropertyBaseColumnRelation> columnRelationList, boolean isArray) throws SQLException {
        if (parentExistEntityInfo == null || !isArray) {
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
        FieldColumnRelationMapperName mapperName = mapperDictTree.getCurrentMapperName();
        PropertyColumnRelationMapper mapper = mapperName.getMapper();
        if (mapper.getIds().isEmpty()) {
            //没有id数据不能合并
            return createValue(resultSet, mapperDictTree, locationList, columnRelationList);
        }
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
        Map<ReturnEntityId, TripleEntity<Object, Object[], ExistEntityInfo[]>> valueMap = parentExistEntityInfo.getExistMap();
        TripleEntity<Object, Object[], ExistEntityInfo[]> tripleEntity = valueMap.get(returnEntityId);
        Object value;
        boolean isNotExist = tripleEntity == null;
        if (isNotExist) {
            value = WsBeanUtils.createObject(mapper.getClazz());
            fillObjectValue(value, ids, location[0], columnRelationList);
            fillObjectValue(value, resultSet, location[1], columnRelationList);
            if (mapperDictTree.getChildMap().isEmpty()) {
                tripleEntity = new TripleEntity<>(value, null, null);
            } else {
                tripleEntity = new TripleEntity<>(value, new Object[mapperDictTree.getChildMap().size()], new ExistEntityInfo[mapperDictTree.getChildMap().size()]);

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
            PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation = mapperDictTree.getFieldJoinClasses()[i];

            if (subExistEntityInfoArray[i] == null) {
                subExistEntityInfo = new ExistEntityInfo();
                subExistEntityInfoArray[i] = subExistEntityInfo;
            } else {
                subExistEntityInfo = subExistEntityInfoArray[i];
            }

            if (cacheSubValue[i] == SqlCommonConstants.NULL_VALUE) {
                continue;
            }
            subValue = createNeedMergeValue(resultSet, subExistEntityInfo, subTree, locationList, columnRelationList, mapperDictTree.isHasArray());
            if (cacheSubValue[i] == null) {
                //第一次获取值
                if (propertyObjectColumnJoinRelation.isArray()) {
                    if (subValue != null) {
                        List<Object> list = new ArrayList<>();
                        list.add(subValue);
                        cacheSubValue[i] = list;
                        propertyObjectColumnJoinRelation.getBeanProperty().setValue(value, list);
                        //WsReflectUtils.setValue(value, list, propertyObjectColumnJoinRelation.getField());
                    }
                } else {
                    if (subValue == null) {
                        cacheSubValue[i] = SqlCommonConstants.NULL_VALUE;
                    } else {
                        cacheSubValue[i] = subValue;
                        propertyObjectColumnJoinRelation.getBeanProperty().setValue(value, subValue);
                        //WsReflectUtils.setValue(value, subValue, propertyObjectColumnJoinRelation.getField());
                    }
                }
            } else {
                if (propertyObjectColumnJoinRelation.isArray() && subValue != null) {
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
    private static Object createValue(WsResultSet resultSet, MapperDictTree mapperDictTree, List<int[][]> locationList, List<PropertyBaseColumnRelation> columnRelationList) throws SQLException {
        PropertyColumnRelationMapper mapper = mapperDictTree.getCurrentMapperName().getMapper();
        FieldColumnRelationMapperName mapperName = mapperDictTree.getCurrentMapperName();
        int[][] location = locationList.get(mapperName.getIndex());
        Object value = WsBeanUtils.createObject(mapper.getClazz());
        boolean isNotNull = fillObjectValue(value, resultSet, location[0], columnRelationList) | fillObjectValue(value, resultSet, location[1], columnRelationList);
        for (int i = 0; i < mapperDictTree.getChildMap().size(); i++) {

            PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation = mapperDictTree.getFieldJoinClasses()[i];
            Object joinValue = createValue(resultSet, mapperDictTree.getMapperDictTrees()[i], locationList, columnRelationList);
            if (joinValue != null) {
                isNotNull = true;
                if (propertyObjectColumnJoinRelation.isArray()) {
                    List<Object> list = new ArrayList<>(1);
                    list.add(joinValue);
                    propertyObjectColumnJoinRelation.getBeanProperty().setValue(value, list);
                } else {
                    propertyObjectColumnJoinRelation.getBeanProperty().setValue(value, joinValue);
                }
            }


        }
        if (isNotNull) {
            return value;
        }
        return null;
    }

    private static boolean fillObjectValue(Object o, Object[] values, int[] location, List<PropertyBaseColumnRelation> columnRelationList) {
        boolean isAdd = false;
        for (int i = 0; i < location.length; i++) {
            PropertyBaseColumnRelation propertyBaseColumnRelationTemp = columnRelationList.get(location[i]);
            Object value = values[i];
            if (value == null) {
                continue;
            }
            isAdd = true;
            setValue(o, value, propertyBaseColumnRelationTemp.getBeanProperty());
        }
        return isAdd;
    }

    private static boolean fillObjectValue(Object o, WsResultSet resultSet, int[] location, List<PropertyBaseColumnRelation> columnRelationList) throws SQLException {
        boolean isAdd = false;
        for (int j : location) {
            PropertyBaseColumnRelation propertyBaseColumnRelationTemp = columnRelationList.get(j);
            Object value = resultSet.getObject(j + 1);
            if (value == null) {
                continue;
            }
            isAdd = true;
            setValue(o, value, propertyBaseColumnRelationTemp.getBeanProperty());
        }
        return isAdd;
    }

    private static void setValue(Object target, Object source, BeanPropertyModel beanPropertyModel) {
        if (source instanceof byte[]) {
            source = new String((byte[]) source);
        }
        source = WsBeanUtils.baseTypeConvert(source, beanPropertyModel.getPropertyClass());
        beanPropertyModel.setValue(target, source);
    }

    /**
     * 生成单个insert sql语句
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> InsertSqlEntity createInsertSqlEntity(T t) {
        PropertyColumnRelationMapper propertyColumnRelationMapper = analysisClassRelation(mainClass);
        List<PropertyBaseColumnRelation> propertyBaseColumnRelationList = propertyColumnRelationMapper.getFieldColumnRelations();
        List<PropertyBaseColumnRelation> validList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();

        List<PropertyBaseColumnRelation> idList = propertyColumnRelationMapper.getIds();

        for (PropertyBaseColumnRelation propertyBaseColumnRelation : idList) {
            BeanPropertyModel beanPropertyModel = propertyBaseColumnRelation.getBeanProperty();
            Object o = beanPropertyModel.getValue(t);
            if (o != null) {
                columnNameList.add(ignoreKeyword(propertyBaseColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                validList.add(propertyBaseColumnRelation);
                valueList.add(new SqlParameter(o));
            }
        }


        for (PropertyBaseColumnRelation propertyBaseColumnRelation : propertyBaseColumnRelationList) {
            BeanPropertyModel beanPropertyModel = propertyBaseColumnRelation.getBeanProperty();
            Object o;
            AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
            if (sqlInterceptor != null && sqlInterceptor.useCondition(analysisClassRelation(mainClass))) {
                o = sqlInterceptor.insertFill();
            } else {
                o = beanPropertyModel.getValue(t);
            }
            if (o != null) {
                columnNameList.add(ignoreKeyword(propertyBaseColumnRelation.getColumnName()));
                placeholderList.add(SqlCommonConstants.PLACEHOLDER);
                validList.add(propertyBaseColumnRelation);
                valueList.add(new SqlParameter(o));
            }
        }

        String insertSql = SqlCommonConstants.INSERT_INTO + ignoreKeyword(propertyColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(placeholderList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS;
        InsertSqlEntity entity = new InsertSqlEntity(insertSql);
        //entity.setInsertSql(insertSql);
        entity.setUsedField(validList);
        entity.setIdList(propertyColumnRelationMapper.getIds());
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
        PropertyColumnRelationMapper propertyColumnRelationMapper = analysisClassRelation(tList.get(0).getClass());
        List<PropertyBaseColumnRelation> propertyBaseColumnRelationList = propertyColumnRelationMapper.getFieldColumnRelations();
        List<PropertyBaseColumnRelation> validField = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        List<SqlParameter> valueList = new ArrayList<>();


        List<PropertyBaseColumnRelation> idList = propertyColumnRelationMapper.getIds();
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : idList) {
            BeanPropertyModel beanPropertyModel = propertyBaseColumnRelation.getBeanProperty();
            Object o = beanPropertyModel.getValue(tList.get(0));
            validField.add(propertyBaseColumnRelation);
            columnNameList.add(ignoreKeyword(propertyBaseColumnRelation.getColumnName()));
            placeholderList.add(SqlCommonConstants.PLACEHOLDER);
            valueList.add(new SqlParameter(o));
        }
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : propertyBaseColumnRelationList) {
            BeanPropertyModel beanPropertyModel = propertyBaseColumnRelation.getBeanProperty();
            AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
            Object o;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(propertyColumnRelationMapper)) {
                o = sqlInterceptor.insertFill();
            } else {
                o = beanPropertyModel.getValue(tList.get(0));
            }
            validField.add(propertyBaseColumnRelation);
            columnNameList.add(ignoreKeyword(propertyBaseColumnRelation.getColumnName()));
            placeholderList.add(SqlCommonConstants.PLACEHOLDER);
            valueList.add(new SqlParameter(o));
        }
        String placeholderSql = SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(placeholderList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS;
        placeholderList = new ArrayList<>();
        placeholderList.add(placeholderSql);
        int size = tList.size();
        for (int i = 1; i < size; i++) {
            for (PropertyBaseColumnRelation propertyBaseColumnRelation : validField) {
                BeanPropertyModel beanPropertyModel = propertyBaseColumnRelation.getBeanProperty();
                AbstractSqlInterceptor sqlInterceptor = INSERT_SQL_INTERCEPTOR_MAP.get(beanPropertyModel.getPropertyName());
                Object o;
                if (sqlInterceptor != null && sqlInterceptor.useCondition(propertyColumnRelationMapper)) {
                    o = sqlInterceptor.insertFill();
                } else {
                    o = beanPropertyModel.getValue(tList.get(i));
                }
                valueList.add(new SqlParameter(o));
            }
            placeholderList.add(placeholderSql);
        }

        String insertSql = SqlCommonConstants.INSERT_INTO + ignoreKeyword(propertyColumnRelationMapper.getTableName()) + SqlCommonConstants.LEFT_BRACKETS + WsStringUtils.jointListString(columnNameList, SqlCommonConstants.COMMA) + SqlCommonConstants.RIGHT_BRACKETS + SqlCommonConstants.VALUE + WsStringUtils.jointListString(placeholderList, ",");
        InsertSqlEntity insertSqlEntity = new InsertSqlEntity(insertSql);
        //insertSqlEntity.setInsertSql(insertSql);
        insertSqlEntity.setUsedField(validField);
        insertSqlEntity.setIdList(propertyColumnRelationMapper.getIds());
        insertSqlEntity.setValueList(valueList);
        return insertSqlEntity;
    }

    /**
     * 合并返回数据
     *
     * @param resultSet
     * @param <T>
     * @return
     */
    public <T> List<T> convertResult(SelectModel selectModel,WsResultSet resultSet) {
        if (WsCollectionUtils.isEmpty(mySearchList.getColumnNameList())) {
            return convertMultiResult(selectModel,resultSet);
        } else {
            List<Object> tList = new ArrayList<>();
            TableColumn tableColumn = selectModel.getSelect().get(0);
            try {
                while (resultSet.next()) {
                    Object o = WsBeanUtils.baseTypeConvert(resultSet.getObject(1), tableColumn.getBeanProperty().getPropertyClass());
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

    private <T> List<T> convertMultiResult(SelectModel selectModel,WsResultSet resultSet) {
        try {
            final int length = resultSet.getColumnCount();
            if (length == 0) {
                return new ArrayList<>();
            }
            PropertyColumnRelationMapper mainMapper = selectModel.getFrom().getTable();
            String rootPath = mainMapper.getEntityName();
            List<PropertyBaseColumnRelation> columnRelationList = new ArrayList<>(length);
            List<int[][]> localtionList = new ArrayList<>();
            Map<String, FieldColumnRelationMapperName> aliasAndMapperNameMap = new HashMap<>();
            String columnNameTemp;
            TableColumn tableColumnTemp;
            List<String> nameListTemp;
            PropertyColumnRelationMapper mapperTemp;
            PropertyBaseColumnRelation propertyBaseColumnRelationTemp;
            FieldColumnRelationMapperName fieldColumnRelationMapperNameTemp;
            //0是id的坐标 1是非id坐标-
            int[][] locationListTemp;
            int mapperNameSign = 0;
            Map<String, Integer> mapperNameAndSignMap = new HashMap<>();

            int mapperNameIndex = 0;

            for (int i = 0; i < length; i++) {
                columnNameTemp = resultSet.getColumnLabel(i + 1);
                tableColumnTemp = selectModel.getSelect().get(i);
                nameListTemp = WsStringUtils.split(columnNameTemp, SqlCommonConstants.SQL_COMMON_DELIMITER);
                //获取映射名称
                fieldColumnRelationMapperNameTemp = aliasAndMapperNameMap.get(nameListTemp.get(0));
                if (fieldColumnRelationMapperNameTemp == null) {
                    mapperTemp = translateNameUtils.getLocalMapper(tableColumnTemp.getTableAlias());
                    fieldColumnRelationMapperNameTemp = new FieldColumnRelationMapperName(mapperNameIndex++, tableColumnTemp.getTableAlias(), tableColumnTemp.getTablePath(), mapperTemp);
                    mapperNameSign = fieldColumnRelationMapperNameTemp.setCompleteNameSplitSignNameList(mapperNameAndSignMap, mapperNameSign);
                    aliasAndMapperNameMap.put(fieldColumnRelationMapperNameTemp.getAlias(), fieldColumnRelationMapperNameTemp);
                } else {
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
                propertyBaseColumnRelationTemp = tableColumnTemp.getFieldColumnRelation();
                if (propertyBaseColumnRelationTemp.isId()) {
                    locationListTemp[0][mapperTemp.getLocation(propertyBaseColumnRelationTemp)] = i;
                } else {
                    locationListTemp[1][mapperTemp.getLocation(propertyBaseColumnRelationTemp)] = i;
                }
                columnRelationList.add(propertyBaseColumnRelationTemp);
            }

            MapperDictTree mapperDictTree = new MapperDictTree();
            for (FieldColumnRelationMapperName name : aliasAndMapperNameMap.values()) {
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
                value = createNeedMergeValue(resultSet, existEntityInfo, mapperDictTree, localtionList, columnRelationList, hasArray);
                if (value != null) {
                    valueList.add(value);
                }
            }
            return (List<T>) valueList;


        } catch (SQLException e) {
            log.error(e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    public TranslateNameUtils getTranslateNameUtils() {
        return translateNameUtils;
    }


    public SelectModel createSelectCountModel() {
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        final String rootPath = mainMapper.getEntityName();
        translateNameUtils.addLocalMapper(from.getAlias(), mainMapper);
        List<TableColumn> queryColumnList = new ArrayList<>(1);
        if (WsCollectionUtils.isNotEmpty(mainMapper.getIds())) {
            queryColumnList.add(translateNameUtils.getColumnBaseEntity(QueryColumn.of(mainMapper.getIds().get(0).getBeanProperty().getPropertyName()), rootPath, 2));
        }else {
            queryColumnList.add(translateNameUtils.getColumnBaseEntity(QueryColumn.of(mainMapper.getFieldColumnRelations().get(0).getBeanProperty().getPropertyName()), rootPath, 2));
        }
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper);
        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);
        return new SelectModel(queryColumnList, from, joinTableModelList, where, null, this.mySearchList.getSqlLimit());
    }


    /**
     * MySearchList转换为SelectModel
     *
     * @return
     */
    public SelectModel createSelectModel() {
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        final String rootPath = mainMapper.getEntityName();
        translateNameUtils.addLocalMapper(from.getAlias(), mainMapper);
        final boolean appointQueryColumn = WsCollectionUtils.isNotEmpty(this.mySearchList.getColumnNameList());
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper);
        List<TableColumn> queryColumnList;
        if (appointQueryColumn) {
            queryColumnList = new ArrayList<>();
            if (this.mySearchList.isSingleColumn()) {
                //是单列查询自动加入distinct关键字去重
                BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity((QueryColumn) this.mySearchList.getColumnNameList().get(0), rootPath, 2);
                DynamicTableColumn dynamicTableColumn = new DynamicTableColumn(
                        new SqlFunctionCondition(false, "distinct ", baseTableColumn),
                        baseTableColumn
                );
                queryColumnList.add(dynamicTableColumn);
            } else {
                for (QueryElement columnName : this.mySearchList.getColumnNameList()) {
                    queryColumnList.add(translateNameUtils.getColumnBaseEntity((QueryColumn) columnName, rootPath, 2));
                }
            }

        }else {
            queryColumnList = handleQueryColumn(rootPath,mainMapper,joinTableModelList);
        }

        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);


        List<OrderByCondition> orderByConditionList = null;
        if (WsCollectionUtils.isNotEmpty(this.mySearchList.getOrderSearches())) {
            orderByConditionList = new ArrayList<>(this.mySearchList.getOrderSearches().size());
            for (MySearch mySearch : this.mySearchList.getOrderSearches()) {
                if (mySearch.getColumn() instanceof QueryColumn) {
                    orderByConditionList.add(new OrderByCondition(translateNameUtils.getColumnBaseEntity((QueryColumn) mySearch.getColumn(), rootPath, 2), (OrderByTypeEnums) mySearch.getValue()));
                } else {
                    orderByConditionList.add(new OrderByCondition(new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, ((QuerySqlString) mySearch.getColumn()).getSql()), null), (OrderByTypeEnums) mySearch.getValue()));
                }
            }
        }
        //this.cacheSelectModel = selectModel;
        return new SelectModel(queryColumnList, from, joinTableModelList, where, orderByConditionList, this.mySearchList.getSqlLimit());
    }

    /**
     * MySearchList转换为DeleteModel
     *
     * @return
     */
    public DeleteModel createDeleteModel() {
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        final String rootPath = mainMapper.getEntityName();
        translateNameUtils.addLocalMapper(from.getAlias(), mainMapper);
        //List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper, null, true);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper);
        RelationCondition where = handleWhere(rootPath, mainMapper, this.mySearchList);
        return new DeleteModel(from, joinTableModelList, where);
    }

    /**
     * 生成InsertModel
     *
     * @return
     */
    public InsertModel createInsertModel() {
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        final String rootPath = mainMapper.getEntityName();
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        List<TableColumn> idList = new ArrayList<>(mainMapper.getIds().size());
        List<TableColumn> tableColumnList = new ArrayList<>(mainMapper.getFieldColumnRelations().size());
        for (PropertyBaseColumnRelation id : mainMapper.getIds()) {
            idList.add(translateNameUtils.createColumnBaseEntity(id, mainMapper, rootPath));
        }
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : mainMapper.getFieldColumnRelations()) {
            tableColumnList.add(translateNameUtils.createColumnBaseEntity(propertyBaseColumnRelation, mainMapper, rootPath));
        }
        return new InsertModel(from, idList, tableColumnList);
    }

    /**
     * MySearchList转换为UpdateModel
     *
     * @return
     */
    public UpdateModel createUpdateModel() {
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(this.mySearchList.getMainClass());
        List<MySearch> updateSearchList = this.mySearchList.filterUpdateSearch();
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        final String rootPath = mainMapper.getEntityName();
        translateNameUtils.addLocalMapper(from.getAlias(), mainMapper);
        List<JoinTableModel> joinTableModelList = handleJoinTableModel(rootPath, mainMapper);
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

    public <T> UpdateModel createUpdateModel(T t, boolean allUpdate) {
        if (t == null) {
            throw new NullPointerException("object is null");
        }
        final PropertyColumnRelationMapper mainMapper = analysisClassRelation(t.getClass());
        if (mainMapper.getIds().isEmpty()) {
            throw new IllegalArgumentException("no primary key");
        }
        TableModel from = new TableModel(mainMapper, translateNameUtils.getCurrentAlias(mainMapper.getEntityName()));
        final String rootPath = mainMapper.getEntityName();
        translateNameUtils.addLocalMapper(from.getAlias(), mainMapper);
        List<Condition> whereCondtionList = new ArrayList<>(mainMapper.getIds().size());

        for (PropertyBaseColumnRelation id : mainMapper.getIds()) {
            Condition condition = createConditionBySqlInterceptor(rootPath, mainMapper, id, SqlEquation.Symbol.EQUAL, SELECT_SQL_INTERCEPTOR_MAP);
            if (condition == null) {
                Object o = id.getBeanProperty().getValue(t);
                if (o == null) {
                    if (!allUpdate) {
                        continue;
                    }
                    o = SqlCommonConstants.NULL_VALUE;
                }
                whereCondtionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(id.getBeanProperty().getPropertyName(), rootPath, 2),
                                SqlEquation.Symbol.EQUAL,
                                o)
                );
            } else {
                whereCondtionList.add(condition);
            }
        }
        List<Condition> updateCondtionList = new ArrayList<>(mainMapper.getFieldColumnRelations().size());
        for (PropertyBaseColumnRelation baseColumn : mainMapper.getFieldColumnRelations()) {
            Condition condition = createConditionBySqlInterceptor(rootPath, mainMapper, baseColumn, SqlEquation.Symbol.EQUAL, UPDATE_SQL_INTERCEPTOR_MAP);
            if (condition == null) {
                Object o = baseColumn.getBeanProperty().getValue(t);
                if (o == null) {
                    if (!allUpdate) {
                        continue;
                    }
                    o = SqlCommonConstants.NULL_VALUE;
                }
                updateCondtionList.add(
                        new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(baseColumn.getBeanProperty().getPropertyName(), rootPath, 2),
                                SqlEquation.Symbol.EQUAL,
                                o)
                );
            } else {
                updateCondtionList.add(condition);
            }
        }
        return new UpdateModel(from, Collections.emptyList(), updateCondtionList, new RelationCondition(whereCondtionList, SqlOperator.AND));
    }

    /**
     * 表关联处理
     *
     * @param rootPath               根路径
     * @param mainMapper             主映射
     * @return
     */
    private List<JoinTableModel> handleJoinTableModel(final String rootPath, final PropertyColumnRelationMapper mainMapper) {
        final List<TableRelation> tableRelationList = this.mySearchList.getJoins();
        final List<JoinTableModel> joinTableModelList = new ArrayList<>();
        final Map<TableRelation, JoinTableModel> usedRelationMap = new HashMap<>();
        final Map<String, TableRelation> tableRelationMap = new HashMap<>();
        for (TableRelation tableRelation : tableRelationList) {
            if (tableRelation.getMainEntityPath() == null) {
                tableRelationMap.put(translateNameUtils.getCurrentAlias(rootPath) + SqlCommonConstants.KEY_COMMON_DELIMITER + translateNameUtils.getAlias(translateNameUtils.getCompleteEntityPath(rootPath, tableRelation.getJoinEntityPath())), tableRelation);
            } else {
                tableRelationMap.put(translateNameUtils.getCurrentAlias(translateNameUtils.getCompleteEntityPath(rootPath, tableRelation.getMainEntityPath())) + SqlCommonConstants.KEY_COMMON_DELIMITER + translateNameUtils.getAlias(translateNameUtils.getCompleteEntityPath(rootPath, tableRelation.getJoinEntityPath())), tableRelation);
            }
        }
        final Queue<KeyValue<String, PropertyObjectColumnJoinRelation>> queue = new ArrayDeque<>();
        for (PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation : mainMapper.getFieldJoinClasses()) {
            queue.add(new KeyValue<>(rootPath, propertyObjectColumnJoinRelation));
        }
        while (!queue.isEmpty()) {
            KeyValue<String, PropertyObjectColumnJoinRelation> keyValue = queue.poll();
            PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation = keyValue.getValue();
            String mainPath = keyValue.getKey();
            String joinPath = mainPath + SqlCommonConstants.PATH_COMMON_DELIMITER + propertyObjectColumnJoinRelation.getJoinEntityPropertyName();
            String tableAlias = translateNameUtils.getCurrentAlias(mainPath);
            String joinTableAlias = translateNameUtils.getCurrentAlias(joinPath);
            String key = tableAlias + SqlCommonConstants.KEY_COMMON_DELIMITER + joinTableAlias;
            TableRelation tableRelation = tableRelationMap.get(key);
            boolean checkChild = false;
            PropertyColumnRelationMapper joinMapper = null;
            if (tableRelation != null) {
                checkChild = true;
                joinMapper = analysisClassRelation(propertyObjectColumnJoinRelation.getJoinEntityClass());
                translateNameUtils.addLocalMapper(joinTableAlias, joinMapper);
                List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath, joinMapper);
                List<Condition> conditionModelList = new ArrayList<>((tableRelation.getConditionSearchList() == null ? 1 : 1 + tableRelation.getConditionSearchList().getAll().size()) + selectInterceptorConditionList.size());
                conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(tableRelation.getTableColumn(), mainPath, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(tableRelation.getJoinTableColumn(), joinPath, 2)));
                if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
                    conditionModelList.addAll(selectInterceptorConditionList);
                }
                if (tableRelation.getConditionSearchList() != null) {
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath, tableRelation.getConditionSearchList(), SqlOperator.AND);
                    if (relationCondition != null && WsCollectionUtils.isNotEmpty(relationCondition.getConditionList())) {
                        conditionModelList.addAll(relationCondition.getConditionList());
                    }
                }
                JoinTableModel joinTableModel = new JoinTableModel(
                        new TableModel(translateNameUtils.getLocalMapper(tableAlias), tableAlias),
                        new TableModel(joinMapper, joinTableAlias),
                        tableRelation.getJoinType(),
                        new RelationCondition(conditionModelList, SqlOperator.AND),
                        true);
                joinTableModelList.add(joinTableModel);
                usedRelationMap.put(tableRelation, joinTableModel);
            } else if (WsStringUtils.isNotBlank(propertyObjectColumnJoinRelation.getJoinTableColumnName())) {
                checkChild = true;
                joinMapper = analysisClassRelation(propertyObjectColumnJoinRelation.getJoinEntityClass());
                PropertyColumnRelationMapper mapper = translateNameUtils.getLocalMapper(tableAlias);
                //PropertyColumnRelationMapper mapper = translateNameUtils.getLocalMapper(mainPath);
                translateNameUtils.addLocalMapper(joinTableAlias, joinMapper);
                List<Condition> selectInterceptorConditionList = getWhereConditionSqlInterceptorConditionList(joinPath, joinMapper);
                List<Condition> conditionModelList = new ArrayList<>(1 + selectInterceptorConditionList.size());
                conditionModelList.add(new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(mapper.getFieldColumnRelationByColumn(propertyObjectColumnJoinRelation.getMainTableColumnName()).getBeanProperty().getPropertyName(), mainPath, 2), SqlEquation.Symbol.EQUAL, translateNameUtils.createColumnBaseEntity(joinMapper.getFieldColumnRelationByColumn(propertyObjectColumnJoinRelation.getJoinTableColumnName()).getBeanProperty().getPropertyName(), joinPath, 2)));
                if (WsCollectionUtils.isNotEmpty(selectInterceptorConditionList)) {
                    conditionModelList.addAll(selectInterceptorConditionList);
                }
                joinTableModelList.add(new JoinTableModel(
                        new TableModel(mapper, tableAlias),
                        new TableModel(joinMapper, joinTableAlias),
                        propertyObjectColumnJoinRelation.getJoinType(),
                        new RelationCondition(conditionModelList, SqlOperator.AND),
                        true
                ));
            }
            if (checkChild) {
                //addQueryColumnList(joinMapper, joinPath, queryColumnList);
                for (PropertyObjectColumnJoinRelation join : joinMapper.getFieldJoinClasses()) {
                    queue.add(new KeyValue<>(joinPath, join));
                }
            }
        }

        for (TableRelation tableRelation : tableRelationList) {
            JoinTableModel joinTableModel = usedRelationMap.get(tableRelation);
            if (joinTableModel == null) {
                String path = tableRelation.getMainEntityPath() == null ? rootPath : rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableRelation.getMainEntityPath();
                String joinPath = tableRelation.getJoinEntityPath() == null ? rootPath : rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableRelation.getJoinEntityPath();
                String alias = translateNameUtils.getAlias(path);
                String joinAlias = translateNameUtils.getAlias(joinPath);
                PropertyColumnRelationMapper joinMapper = analysisClassRelation(tableRelation.getJoinEntityClass());
                translateNameUtils.addLocalMapper(joinAlias, joinMapper);
                PropertyColumnRelationMapper mapper = tableRelation.getMainEntityPath() == null ? mainMapper : translateNameUtils.getLocalMapper(alias);
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
                joinTableModelList.add(new JoinTableModel(
                        new TableModel(mapper, alias),
                        new TableModel(joinMapper, joinAlias),
                        tableRelation.getJoinType(),
                        new RelationCondition(conditionModelList, SqlOperator.AND),
                        false
                ));
            }/* else {
                if (tableRelation.getConditionSearchList() != null) {
                    RelationCondition relationCondition = searchListToConditionRelation(rootPath, tableRelation.getConditionSearchList(), SqlOperator.AND);
                    if (relationCondition != null && WsCollectionUtils.isNotEmpty(relationCondition.getConditionList())) {
                        joinTableModel.getOn().getConditionList().addAll(relationCondition.getConditionList());
                    }
                }
            }*/
        }
        return joinTableModelList;
    }

    /**
     * 处理查询的列
     * @param rootPath
     * @param mainMapper
     * @param joinTableModelList
     * @return
     */
    private List<TableColumn> handleQueryColumn(String rootPath,PropertyColumnRelationMapper mainMapper,List<JoinTableModel> joinTableModelList) {
        List<TableColumn> tableColumnList = new ArrayList<>();
        addQueryColumnList(mainMapper,rootPath,tableColumnList);
        if (WsCollectionUtils.isNotEmpty(joinTableModelList)) {
            for (JoinTableModel joinTableModel : joinTableModelList) {
                if (!joinTableModel.isQueryColumn()){
                    continue;
                }
                addQueryColumnList(joinTableModel.getJoinTable().getTable(), translateNameUtils.getOriginalName(joinTableModel.getJoinTable().getAlias()), tableColumnList);
            }
        }
        return tableColumnList;
    }

    /**
     * 处理where条件（必须在处理表关联关系之后进行）
     *
     * @param rootPath
     * @param mainMapper
     * @param mySearchList
     * @return
     */
    private RelationCondition handleWhere(final String rootPath, final PropertyColumnRelationMapper mainMapper, final MySearchList mySearchList) {
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
    private void addQueryColumnList(final PropertyColumnRelationMapper mapper, final String path, final List<TableColumn> queryColumnList) {
        if (queryColumnList == null) {
            return;
        }
        if (WsCollectionUtils.isNotEmpty(mapper.getIds())) {
            for (PropertyBaseColumnRelation propertyBaseColumnRelation : mapper.getIds()) {
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(propertyBaseColumnRelation, mapper, path));
            }
        }
        if (WsCollectionUtils.isNotEmpty(mapper.getFieldColumnRelations())) {
            for (PropertyBaseColumnRelation propertyBaseColumnRelation : mapper.getFieldColumnRelations()) {
                queryColumnList.add(translateNameUtils.createColumnBaseEntity(propertyBaseColumnRelation, mapper, path));
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
     * mySearch转condition
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
                    left = translateNameUtils.getColumnBaseEntity((QueryColumn) search.getColumn(), rootPath, 2);
                    right = translateNameUtils.getColumnBaseEntity((QueryColumn) search.getValue(), rootPath, 2);
                    break;
                case SQL:
                    left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, ((QuerySqlString) search.getColumn()).getSql()), search.getValue());
                    break;
                case EXISTS:
                case NOT_EXISTS:
                    if (search.getValue() instanceof MySearchList) {
                        //right = search.getValue();
                        left = search.getValue();
                    } else {
                        left = new SqlStringModel(translateNameUtils.translateTableNickName(rootPath, ((QuerySqlString) search.getColumn()).getSql()), search.getValue());
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
                    BaseTableColumn baseTableColumn = translateNameUtils.getColumnBaseEntity((QueryColumn) search.getColumn(), rootPath, 2);
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
                    left = translateNameUtils.getColumnBaseEntity((QueryColumn) search.getColumn(), rootPath, 2);
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
                    multiExpressionCondition.add(translateNameUtils.getColumnBaseEntity((QueryColumn) value, rootPath, 2));
                    break;
                case ValueTypeConstants.SYMBOL_TYPE:
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
    public List<Condition> getWhereConditionSqlInterceptorConditionList(String path, PropertyColumnRelationMapper mapper) {
        List<Condition> conditionList = new ArrayList<>();
        fillWhereConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getIds());
        fillWhereConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getFieldColumnRelations());
        return conditionList;
    }

    public List<Condition> getUpdateConditionSqlInterceptorConditionList(String path, PropertyColumnRelationMapper mapper) {
        List<Condition> conditionList = new ArrayList<>();
        fillUpdateConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getIds());
        fillUpdateConditionSqlInterceptorConditionList(conditionList, path, mapper, mapper.getFieldColumnRelations());
        return conditionList;
    }

    public void fillWhereConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                              String path, PropertyColumnRelationMapper mapper,
                                                              List<PropertyBaseColumnRelation> propertyBaseColumnRelationList) {
        if (WsCollectionUtils.isEmpty(propertyBaseColumnRelationList)) {
            return;
        }
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : propertyBaseColumnRelationList) {
            AbstractSqlInterceptor interceptor = SELECT_SQL_INTERCEPTOR_MAP.get(propertyBaseColumnRelation.getBeanProperty().getPropertyName());
            if (interceptor != null && interceptor.useCondition(mapper)) {
                Condition condition = createConditionBySqlInterceptor(path, mapper, propertyBaseColumnRelation, SqlEquation.Symbol.EQUAL, SELECT_SQL_INTERCEPTOR_MAP);
                if (condition != null) {
                    conditionList.add(condition);
                }
            }
        }
    }

    public void fillUpdateConditionSqlInterceptorConditionList(List<Condition> conditionList,
                                                               String path, PropertyColumnRelationMapper mapper,
                                                               List<PropertyBaseColumnRelation> propertyBaseColumnRelationList) {
        if (WsCollectionUtils.isEmpty(propertyBaseColumnRelationList)) {
            return;
        }
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : propertyBaseColumnRelationList) {
            Condition condition = createConditionBySqlInterceptor(path, mapper, propertyBaseColumnRelation, SqlEquation.Symbol.EQUAL, UPDATE_SQL_INTERCEPTOR_MAP);
            if (condition != null) {
                conditionList.add(condition);
            }
        }
    }

    /**
     * 通过sql拦截器创建条件
     *
     * @param path
     * @param mapper
     * @param propertyBaseColumnRelation
     * @param symbol
     * @param sqlInterceptorMap
     * @return
     */
    public Condition createConditionBySqlInterceptor(String path,
                                                     PropertyColumnRelationMapper mapper,
                                                     PropertyBaseColumnRelation propertyBaseColumnRelation,
                                                     SqlEquation.Symbol symbol,
                                                     Map<String, AbstractSqlInterceptor> sqlInterceptorMap) {
        if (propertyBaseColumnRelation == null) {
            return null;
        }
        AbstractSqlInterceptor interceptor = sqlInterceptorMap.get(propertyBaseColumnRelation.getBeanProperty().getPropertyName());
        if (interceptor != null && interceptor.useCondition(mapper)) {
            Object fillValue = interceptor.updateFill();
            if (fillValue == null) {
                fillValue = SqlCommonConstants.NULL_VALUE;
            }
            return new SingleExpressionCondition(translateNameUtils.createColumnBaseEntity(propertyBaseColumnRelation.getBeanProperty().getPropertyName(), path, 2), symbol, fillValue);
        }
        return null;
    }


}