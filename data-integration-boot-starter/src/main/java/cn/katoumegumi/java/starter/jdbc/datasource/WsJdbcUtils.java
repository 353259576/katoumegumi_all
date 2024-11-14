package cn.katoumegumi.java.starter.jdbc.datasource;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.common.model.KeyValue;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.handler.SqlEntityFactory;
import cn.katoumegumi.java.sql.handler.model.DeleteSqlEntity;
import cn.katoumegumi.java.sql.handler.model.InsertSqlEntity;
import cn.katoumegumi.java.sql.handler.model.SelectSqlEntity;
import cn.katoumegumi.java.sql.handler.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;
import cn.katoumegumi.java.sql.resultSet.strategys.JdkResultSet;
import cn.katoumegumi.java.sql.model.component.SqlLimit;
import cn.katoumegumi.java.sql.handler.model.SqlParameter;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ws
 */
public class WsJdbcUtils {

    public static final Logger log = LoggerFactory.getLogger(WsJdbcUtils.class);


    private JdbcTemplate jdbcTemplate;

    private static final int[] EMPTY_INT_ARRAY = new int[0];


    public <T> int insert(T t) {
        if(t == null){
            throw new IllegalArgumentException("insert Object is null");
        }
        if (WsReflectUtils.isArrayType(t.getClass())) {
            throw new RuntimeException("insert Object is array");
        }
        MySearchList mySearchList = MySearchList.create(t.getClass());
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelFactory.createInsertSqlEntity(t);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int row = jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        Map<String, Object> keyMap = keyHolder.getKeys();
        if (WsCollectionUtils.isNotEmpty(keyMap)) {
            List<PropertyBaseColumnRelation> idList = insertSqlEntity.getIdList();
            setGeneratedKey(t,keyMap,idList);
        }
        return row;
    }

    public <T> int insert(List<T> tList) {
        if (WsCollectionUtils.isEmpty(tList)) {
            return 0;
        }
        MySearchList mySearchList = MySearchList.create(tList.get(0).getClass());
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelFactory.createInsertSqlEntity(tList);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int row = jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        List<Map<String, Object>> keyMapList = keyHolder.getKeyList();
        if(WsCollectionUtils.isNotEmpty(keyMapList)){
            List<PropertyBaseColumnRelation> idList = insertSqlEntity.getIdList();
            for (int i = 0; i < keyMapList.size(); i++){
                setGeneratedKey(tList.get(i),keyMapList.get(i),idList);
            }
        }
        return row;
    }


    private PreparedStatementCreator createPreparedStatement(InsertSqlEntity insertSqlEntity) {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(insertSqlEntity.getInsertSql(), Statement.RETURN_GENERATED_KEYS);
            Object o;
            List<?> valueList = WsCollectionUtils.listToList(insertSqlEntity.getValueList(), SqlParameter::getValue);
            for (int i = 0; i < valueList.size(); i++) {
                o = valueList.get(i);
                if (o == null) {
                    statement.setNull(i + 1, Types.NULL);
                    continue;
                }
                if (o instanceof String) {
                    statement.setString(i + 1, WsStringUtils.anyToString(o));
                } else if (o instanceof Integer || WsReflectUtils.classCompare(int.class, o.getClass())) {
                    statement.setInt(i + 1, WsBeanUtils.baseTypeConvert(o, int.class));
                } else if (o instanceof Long || WsReflectUtils.classCompare(o.getClass(), long.class)) {
                    statement.setLong(i + 1, WsBeanUtils.baseTypeConvert(o, long.class));
                } else if (o instanceof Short || WsReflectUtils.classCompare(o.getClass(), short.class)) {
                    statement.setShort(i + 1, WsBeanUtils.baseTypeConvert(o, short.class));
                } else if (o instanceof Float || WsReflectUtils.classCompare(o.getClass(), Float.class)) {
                    statement.setFloat(i + 1, WsBeanUtils.baseTypeConvert(o, float.class));
                } else if (o instanceof Double || WsReflectUtils.classCompare(o.getClass(), double.class)) {
                    statement.setDouble(i + 1, WsBeanUtils.baseTypeConvert(o, double.class));
                } else if (o instanceof BigDecimal) {
                    statement.setBigDecimal(i + 1, WsBeanUtils.baseTypeConvert(o, BigDecimal.class));
                } else if (o instanceof Date) {
                    statement.setString(i + 1, WsBeanUtils.baseTypeConvert(o, String.class));
                } else if (o instanceof LocalDate || o instanceof LocalDateTime) {
                    statement.setString(i + 1, WsBeanUtils.baseTypeConvert(o, String.class));
                }else {
                    statement.setObject(i+1,o);
                    //throw new RuntimeException("不支持的数据类型:" + o.getClass());
                }
            }
            return statement;
        };
    }

    public <T> int update(T t) {
        return update(t, false);
    }

    public <T> int update(T t, boolean isAll) {
        if (t == null) {
            return 0;
        }
        MySearchList mySearchList = MySearchList.create(t.getClass());
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        UpdateSqlEntity updateSqlEntity = SqlEntityFactory.createUpdateSqlEntity(sqlModelFactory.createUpdateModel(t,isAll));
        log.debug(updateSqlEntity.getUpdateSql());
        return jdbcTemplate.update(updateSqlEntity.getUpdateSql(), WsCollectionUtils.listToArray(updateSqlEntity.getValueList(), SqlParameter::getValue));
    }

    public int update(MySearchList mySearchList) {
        if (mySearchList == null) {
            return 0;
        }
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        UpdateSqlEntity updateSqlEntity = SqlEntityFactory.createUpdateSqlEntity(sqlModelFactory.createUpdateModel());
        log.debug(updateSqlEntity.getUpdateSql());
        return jdbcTemplate.update(updateSqlEntity.getUpdateSql(), WsCollectionUtils.listToArray(updateSqlEntity.getValueList(), SqlParameter::getValue));
    }

    public int[] updateBatch(List<MySearchList> mySearchLists) {
        if (WsCollectionUtils.isEmpty(mySearchLists)) {
            return EMPTY_INT_ARRAY;
        }
        int[] ans = new int[mySearchLists.size()];
        Map<String, KeyValue<List<Object[]>,List<Integer>>> map = new HashMap<>();
        int index = 0;
        for (MySearchList mySearchList : mySearchLists) {
            SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
            UpdateSqlEntity updateSqlEntity = SqlEntityFactory.createUpdateSqlEntity(sqlModelFactory.createUpdateModel());
            KeyValue<List<Object[]>,List<Integer>> keyValue = map.computeIfAbsent(updateSqlEntity.getUpdateSql(), sql -> new KeyValue<>(new ArrayList<>(),new ArrayList<>()));
            keyValue.getKey().add(WsCollectionUtils.listToArray(updateSqlEntity.getValueList(), SqlParameter::getValue));
            keyValue.getValue().add(index++);
        }
        for (Map.Entry<String, KeyValue<List<Object[]>, List<Integer>>> stringKeyValueEntry : map.entrySet()) {
            String sql = stringKeyValueEntry.getKey();
            KeyValue<List<Object[]>,List<Integer>> keyValue = stringKeyValueEntry.getValue();
            log.debug(sql);
            int[] returnAns = jdbcTemplate.batchUpdate(sql, keyValue.getKey());
            for (int i = 0; i < returnAns.length; i++){
                ans[keyValue.getValue().get(i)] = returnAns[i];
            }
        }
        return ans;
    }

    public <T> int[] updateBatchByT(List<T> tList) {
        return updateBatchByT(tList, false);
    }

    public <T> int[] updateBatchByT(List<T> tList, boolean isAll) {
        if (WsCollectionUtils.isEmpty(tList)) {
            return EMPTY_INT_ARRAY;
        }
        int[] ans = new int[tList.size()];
        int index = 0;
        Map<String, KeyValue<List<Object[]>,List<Integer>>> map = new HashMap<>();
        MySearchList mySearchList = MySearchList.create(tList.get(0).getClass());
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        for (T t : tList) {
            UpdateSqlEntity updateSqlEntity = SqlEntityFactory.createUpdateSqlEntity(sqlModelFactory.createUpdateModel(t,isAll));
            KeyValue<List<Object[]>,List<Integer>> keyValue = map.computeIfAbsent(updateSqlEntity.getUpdateSql(), sql -> new KeyValue<>(new ArrayList<>(),new ArrayList<>()));
            keyValue.getKey().add(WsCollectionUtils.listToArray(updateSqlEntity.getValueList(), SqlParameter::getValue));
            keyValue.getValue().add(index++);
        }
        for (Map.Entry<String, KeyValue<List<Object[]>, List<Integer>>> stringKeyValueEntry : map.entrySet()) {
            String sql = stringKeyValueEntry.getKey();
            KeyValue<List<Object[]>,List<Integer>> keyValue = stringKeyValueEntry.getValue();
            log.debug(sql);
            int[] returnAns = jdbcTemplate.batchUpdate(sql, keyValue.getKey());
            for (int i = 0; i < returnAns.length; i++){
                ans[keyValue.getValue().get(i)] = returnAns[i];
            }
        }
        return ans;
    }

    public int delete(MySearchList mySearchList) {
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        DeleteSqlEntity deleteSqlEntity = SqlEntityFactory.createDeleteSqlEntity(sqlModelFactory.createDeleteModel());
        log.debug(deleteSqlEntity.getDeleteSql());
        return jdbcTemplate.update(deleteSqlEntity.getDeleteSql(), WsCollectionUtils.listToArray(deleteSqlEntity.getValueList(), SqlParameter::getValue));
    }


    /**
     * 查询列表
     *
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> List<T> getListT(MySearchList mySearchList) {
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        SelectSqlEntity selectSqlEntity = SqlEntityFactory.createSelectSqlEntity(sqlModelFactory.createSelectModel());
        String sql = selectSqlEntity.getSelectSql();
        log.debug(sql);
        List<Object> parameterList = selectSqlEntity.getValueList().stream().map(SqlParameter::getValue).collect(Collectors.toList());
        return queryList(sql, parameterList, sqlModelFactory);
    }

    private <T> List<T> queryList(String sql, List<Object> parameterList, SQLModelFactory sqlModelFactory) {
        return jdbcTemplate.query(sql, new ArgumentPreparedStatementSetter(parameterList.toArray()), rs -> {
            return sqlModelFactory.convertResult(new JdkResultSet(rs));
        });
    }

    private <T> T querySingleColumnObject(String sql,List<Object> parameterList,Class<T> returnType){
        return jdbcTemplate.query(sql, new ArgumentPreparedStatementSetter(parameterList.toArray()), rs -> {
            if (rs.next()) {
                return WsBeanUtils.baseTypeConvert(rs.getObject(1), returnType);
            }
            return null;
        });
    }


    public <T> List<T> getListT(T t) {
        MySearchList mySearchList = SQLModelFactory.objectToMySearchList(t);
        return getListT(mySearchList);
    }

    /**
     * 查询一条
     *
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> T getTOne(MySearchList mySearchList) {
        List<T> tList = getListT(mySearchList);
        if (WsCollectionUtils.isNotEmpty(tList)) {
            if (tList.size() > 1) {
                log.warn("本次查询数据大于一条但是仅显示一条。");
            }
            return tList.get(0);
        }
        return null;
    }

    public <T> T getTOne(T t) {
        MySearchList mySearchList = SQLModelFactory.objectToMySearchList(t);
        return getTOne(mySearchList);
    }

    public <T> T getOne(Class<T> tClass, Object... objects) {
        PropertyColumnRelationMapper mapper = FieldColumnRelationMapperFactory.analysisClassRelation(tClass);
        List<PropertyBaseColumnRelation> ids = mapper.getIds();
        if (ids.size() != objects.length) {
            throw new RuntimeException("主键信息需要填写完整");
        }
        MySearchList mySearchList = MySearchList.create(tClass);
        for (int i = 0; i < ids.size(); i++) {
            PropertyBaseColumnRelation relation = ids.get(i);
            Object value = objects[i];
            mySearchList.eq(relation.getBeanProperty().getPropertyName(), value);
        }
        return getTOne(mySearchList);
    }

    /**
     * 分页查询
     *
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> IPage<T> getTPage(MySearchList mySearchList) {
        SQLModelFactory sqlModelFactory = new SQLModelFactory(mySearchList);
        SelectSqlEntity selectSqlEntity = SqlEntityFactory.createSelectSqlEntity(sqlModelFactory.createSelectModel());
        String sql = selectSqlEntity.getSelectSql();
        log.debug(sql);
        String countSql = selectSqlEntity.getCountSql();
        log.info(countSql);
        List<Object> parameterList = selectSqlEntity.getValueList().stream().map(SqlParameter::getValue).collect(Collectors.toList());
        Long count = querySingleColumnObject(countSql,parameterList,Long.class);
        if(count == null){
            count = 0L;
        }
        SqlLimit sqlLimit = mySearchList.getSqlLimit();
        List<T> tList;
        if(count > sqlLimit.getOffset()) {
            tList = queryList(sql, parameterList, sqlModelFactory);
        }else {
            tList = new ArrayList<>(0);
        }
        IPage<T> iPage = new Page<>();
        iPage.setCurrent(sqlLimit.getCurrent());
        iPage.setSize(sqlLimit.getSize());
        iPage.setRecords(tList);
        iPage.setTotal(count);
        return iPage;
    }

    public <T> IPage<T> getTPage(T t, IPage<?> page) {
        MySearchList mySearchList = SQLModelFactory.objectToMySearchList(t);
        mySearchList.setSqlLimit(sqlLimit -> sqlLimit.setCurrent(page.getCurrent()).setSize(page.getSize()));
        return getTPage(mySearchList);
    }

    /**
     * 添加或保存（只支持单主键）
     * @param t
     * @param <T>
     * @return
     */
    public <T> Integer saveOrUpdate(T t){
        PropertyColumnRelationMapper propertyColumnRelationMapper = SQLModelFactory.getFieldColumnRelationMapper(t.getClass());
        List<PropertyBaseColumnRelation> propertyBaseColumnRelations = propertyColumnRelationMapper.getIds();
        PropertyBaseColumnRelation relation = propertyBaseColumnRelations.get(0);
        BeanPropertyModel beanPropertyModel = relation.getBeanProperty();
        Object o = beanPropertyModel.getValue(t);
        if(o == null){
            return insert(t);
        }else {
            MySearchList mySearchList = MySearchList.create(t.getClass());
            mySearchList.singleColumnName(beanPropertyModel.getPropertyName());
            mySearchList.eq(beanPropertyModel.getPropertyName(),o);
            Object oldIndex = getTOne(mySearchList);
            if(oldIndex == null){
                return insert(t);
            }else {
                return update(t);
            }
        }
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private <T> void setGeneratedKey(T t, Map<String,Object> keyMap, List<PropertyBaseColumnRelation> idList){
        boolean[] isUseArray = new boolean[idList.size()];
        Map<String, Integer> fieldNameAndIndexMap = new HashMap<>(idList.size());
        for (int i = 0; i < idList.size(); i++){
            fieldNameAndIndexMap.put(idList.get(i).getColumnName(),i);
        }
        List<String> unUseColumnNameList = new ArrayList<>(keyMap.size());
        for (Map.Entry<String, Object> stringObjectEntry : keyMap.entrySet()) {
            String columnName = stringObjectEntry.getKey();
            Integer index = fieldNameAndIndexMap.get(columnName);
            if(index == null){
                unUseColumnNameList.add(columnName);
                continue;
            }
            isUseArray[index] = true;
            PropertyBaseColumnRelation propertyBaseColumnRelation = idList.get(index);
            propertyBaseColumnRelation.getBeanProperty()
                    .setValue(t,WsBeanUtils.baseTypeConvert(stringObjectEntry.getValue(), propertyBaseColumnRelation.getBeanProperty().getPropertyClass()));
        }
        if (CollectionUtils.isEmpty(unUseColumnNameList)) {
            return;
        }

        for (int idIndex = 0,unUseIndex = 0; idIndex < isUseArray.length && unUseIndex < unUseColumnNameList.size();unUseIndex++){
            Object value = keyMap.get(unUseColumnNameList.get(unUseIndex));
            while (idIndex < isUseArray.length){
                if (isUseArray[idIndex]){
                    idIndex++;
                    continue;
                }
                isUseArray[idIndex] = false;
                PropertyBaseColumnRelation propertyBaseColumnRelation = idList.get(idIndex);
                propertyBaseColumnRelation.getBeanProperty().setValue(t,WsBeanUtils.baseTypeConvert(value, propertyBaseColumnRelation.getBeanProperty().getPropertyClass()));
                idIndex++;
                break;
            }
        }
    }
}
