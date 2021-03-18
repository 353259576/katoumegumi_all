package cn.katoumegumi.java.datasource;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.*;

/**
 * @author ws
 */
public class WsJdbcUtils {

    public static final Logger log = LoggerFactory.getLogger(WsJdbcUtils.class);


    private JdbcTemplate jdbcTemplate;


    public <T> int insert(T t) {
        if(t == null){
            return 0;
        }
        MySearchList mySearchList = MySearchList.create(t.getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelUtils.insertSql(t);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int row = jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        Map<String, Object> keyMap = keyHolder.getKeys();
        if (WsListUtils.isNotEmpty(keyMap) && keyMap.size() > 0) {
            Map<String, FieldColumnRelation> stringFieldColumnRelationMap = new HashMap<>();
            for (FieldColumnRelation fieldColumnRelation : insertSqlEntity.getIdList()) {
                stringFieldColumnRelationMap.put(fieldColumnRelation.getColumnName(), fieldColumnRelation);
            }
            List<String> unUsedSet = new ArrayList<>();
            keyMap.forEach((s, o) -> {
                FieldColumnRelation fieldColumnRelation = stringFieldColumnRelationMap.get(s);
                if (fieldColumnRelation != null) {
                    Field field = fieldColumnRelation.getField();
                    field.setAccessible(true);
                    try {
                        field.set(t, o);
                        stringFieldColumnRelationMap.remove(s);
                        keyMap.remove(s);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    unUsedSet.add(s);
                }
            });
            if (stringFieldColumnRelationMap.size() > 0 && unUsedSet.size() > 0) {
                Set<Map.Entry<String, FieldColumnRelation>> fSet = stringFieldColumnRelationMap.entrySet();
                for (Map.Entry<String, FieldColumnRelation> entry : fSet) {
                    FieldColumnRelation fieldColumnRelation = entry.getValue();
                    try {
                        fieldColumnRelation.getField().set(t, WsBeanUtils.objectToT(keyMap.get(unUsedSet.remove(0)), fieldColumnRelation.getField().getType()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }

            }
        }


        return row;
    }

    public <T> int insert(List<T> tList) {
        if(WsListUtils.isEmpty(tList)){
            return 0;
        }
        MySearchList mySearchList = MySearchList.create(tList.get(0).getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelUtils.insertSqlBatch(tList);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int row = jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        List<Map<String, Object>> keyMapList = keyHolder.getKeyList();

        if (WsListUtils.isNotEmpty(keyMapList) && keyMapList.size() > 0) {
            Map<String, FieldColumnRelation> stringFieldColumnRelationMap = new HashMap<>();
            for (FieldColumnRelation fieldColumnRelation : insertSqlEntity.getIdList()) {
                stringFieldColumnRelationMap.put(fieldColumnRelation.getColumnName(), fieldColumnRelation);
            }

            for (int i = 0; i < keyMapList.size(); i++) {
                Map<String, Object> objectMap = keyMapList.get(i);
                List<String> unUsedSet = new ArrayList<>();
                int finalI = i;
                objectMap.forEach((s, o) -> {
                    FieldColumnRelation fieldColumnRelation = stringFieldColumnRelationMap.get(s);
                    if (fieldColumnRelation != null) {
                        Field field = fieldColumnRelation.getField();
                        field.setAccessible(true);
                        try {
                            field.set(tList.get(finalI), o);
                            stringFieldColumnRelationMap.remove(s);
                            objectMap.remove(s);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        unUsedSet.add(s);
                    }
                });
                if (stringFieldColumnRelationMap.size() > 0 && unUsedSet.size() > 0) {
                    Set<Map.Entry<String, FieldColumnRelation>> fSet = stringFieldColumnRelationMap.entrySet();
                    for (Map.Entry<String, FieldColumnRelation> entry : fSet) {
                        FieldColumnRelation fieldColumnRelation = entry.getValue();
                        try {
                            fieldColumnRelation.getField().set(tList.get(i), WsBeanUtils.objectToT(objectMap.get(unUsedSet.remove(0)), fieldColumnRelation.getField().getType()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }
        //log.info(JSON.toJSONString(keyHolder));
        return row;
    }


    private PreparedStatementCreator createPreparedStatement(InsertSqlEntity insertSqlEntity) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(insertSqlEntity.getInsertSql(), Statement.RETURN_GENERATED_KEYS);
                Object o;
                List valueList = insertSqlEntity.getValueList();
                for (int i = 0; i < valueList.size(); i++) {
                    o = valueList.get(i);
                    if (o == null) {
                        statement.setNull(i + 1, Types.NULL);
                        continue;
                    }
                    if (o instanceof String) {
                        statement.setString(i + 1, WsStringUtils.anyToString(o));
                    } else if (o instanceof Integer || WsFieldUtils.classCompare(int.class, o.getClass())) {
                        statement.setInt(i + 1, WsBeanUtils.objectToT(o, int.class));
                    } else if (o instanceof Long || WsFieldUtils.classCompare(o.getClass(), long.class)) {
                        statement.setLong(i + 1, WsBeanUtils.objectToT(o, long.class));
                    } else if (o instanceof Short || WsFieldUtils.classCompare(o.getClass(), short.class)) {
                        statement.setShort(i + 1, WsBeanUtils.objectToT(o, short.class));
                    } else if (o instanceof Float || WsFieldUtils.classCompare(o.getClass(), Float.class)) {
                        statement.setFloat(i + 1, WsBeanUtils.objectToT(o, float.class));
                    } else if (o instanceof Double || WsFieldUtils.classCompare(o.getClass(), double.class)) {
                        statement.setDouble(i + 1, WsBeanUtils.objectToT(o, double.class));
                    } else if (o instanceof BigDecimal) {
                        statement.setBigDecimal(i + 1, WsBeanUtils.objectToT(o, BigDecimal.class));
                    } else if (o instanceof Date) {
                        statement.setString(i + 1, WsBeanUtils.objectToT(o, String.class));
                    } else if (o instanceof LocalDate || o instanceof LocalDateTime) {
                        statement.setString(i + 1, WsBeanUtils.objectToT(o, String.class));
                    } else {
                        throw new RuntimeException("不支持的数据类型:" + o.getClass());
                    }
                }
                return statement;
            }
        };
        return preparedStatementCreator;
    }

    public <T> int update(T t){
        return update(t,false);
    }
    public <T> int update(T t,boolean isAll) {
        if(t == null){
            return 0;
        }
        MySearchList mySearchList = MySearchList.create(t.getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(t,isAll);
        log.debug(updateSqlEntity.getUpdateSql());
        return jdbcTemplate.update(updateSqlEntity.getUpdateSql(), updateSqlEntity.getValueList().toArray());
    }

    public int update(MySearchList mySearchList) {
        if(mySearchList == null){
            return 0;
        }
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(mySearchList);
        log.debug(updateSqlEntity.getUpdateSql());
        return jdbcTemplate.update(updateSqlEntity.getUpdateSql(), updateSqlEntity.getValueList().toArray());
    }

    public void updateBatch(List<MySearchList> mySearchLists) {
        if(WsListUtils.isEmpty(mySearchLists)){
            return;
        }
        Map<String,List<Object[]>> map = new HashMap<>();
        for(MySearchList mySearchList:mySearchLists){
            SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
            UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(mySearchList);
            List<Object[]> objectList = map.computeIfAbsent(updateSqlEntity.getUpdateSql(),sql->{
                return new ArrayList<>();
            });
            objectList.add(updateSqlEntity.getValueList().toArray());
        }
        map.forEach((sql,listValue)->{
            log.debug(sql);
            jdbcTemplate.batchUpdate(sql,listValue);
        });
    }

    public <T> void updateBatchByT(List<T> tList){
        updateBatchByT(tList,false);
    }

    public <T> void updateBatchByT(List<T> tList,boolean isAll) {
        if(WsListUtils.isEmpty(tList)){
            return;
        }
        Map<String,List<Object[]>> map = new HashMap<>();
        //List<String> sqlList = new ArrayList<>(tList.size());
        //List<Object[]> list = new ArrayList<>();
        for(T t:tList){
            MySearchList mySearchList = MySearchList.create(t.getClass());
            SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
            UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(t,isAll);
            List<Object[]> objectList = map.computeIfAbsent(updateSqlEntity.getUpdateSql(),sql->{
                return new ArrayList<>();
            });
            objectList.add(updateSqlEntity.getValueList().toArray());
        }
        map.forEach((sql,listValue)->{
            log.debug(sql);
            jdbcTemplate.batchUpdate(sql,listValue);
        });
    }

    public int delete(MySearchList mySearchList){
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        DeleteSqlEntity deleteSqlEntity = sqlModelUtils.delete();
        log.debug(deleteSqlEntity.getDeleteSql());
        return jdbcTemplate.update(deleteSqlEntity.getDeleteSql(),deleteSqlEntity.getValueList().toArray());
    }


    /**
     * 查询列表
     *
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> List<T> getListT(MySearchList mySearchList) {
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity selectSqlEntity = sqlModelUtils.select();
        String sql = selectSqlEntity.getSelectSql();
        log.debug(sql);

        List finalList = selectSqlEntity.getValueList();

        return handleJdbcReturnValue(sql,finalList,sqlModelUtils);

        //List list = handleJdbcReturnValue(sql, finalList);

        //return sqlModelUtils.oneLoopMargeMap(list);
        //return sqlModelUtils.loadingObject(sqlModelUtils.mergeMapList(sqlModelUtils.handleMap(list)));
    }

    private <T> List<T> handleJdbcReturnValue(String sql,List finalList,SQLModelUtils sqlModelUtils){
        return jdbcTemplate.query(sql, finalList.toArray(), new ResultSetExtractor<List<T>>() {
            @Override
            public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<T> list = sqlModelUtils.margeMap(rs);
                return list;
            }
        });
    }


    private List handleJdbcReturnValue(String sql, List finalList) {

        List<String> nameList = new ArrayList<>();

        List list = jdbcTemplate.query(sql, finalList.toArray(), (resultSet, i) -> {
            if (i < 1) {
                int length = resultSet.getMetaData().getColumnCount();
                for (int j = 0; j < length; j++) {
                    nameList.add(resultSet.getMetaData().getColumnLabel(j + 1));
                }
            }
            int length = nameList.size();
            Map map = new HashMap((int) (length/0.75));
            //List<ReturnColumnEntity> valueList = new ArrayList<>(nameListLength);
            for (int j = 0; j < length; ++j) {
                //valueList.add(new ReturnColumnEntity(nameList.get(j),resultSet.getObject(j+1)));
                map.put(nameList.get(j), resultSet.getObject(j + 1));

            }
            return map;
            //return valueList;
        });
        return list;
    }

    public <T> List<T> getListT(T t) {
        MySearchList mySearchList = SQLModelUtils.objectToMySearchList(t);
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
        if (WsListUtils.isNotEmpty(tList)) {
            if (tList.size() > 1) {
                log.warn("本次查询数据大于一条但是仅显示一条。");
            }
            return tList.get(0);
        }
        return null;
    }

    public <T> T getTOne(T t) {
        MySearchList mySearchList = SQLModelUtils.objectToMySearchList(t);
        return getTOne(mySearchList);
    }

    public <T> T getOne(Class<T> tClass, Object... objects) {
        FieldColumnRelationMapper mapper = FieldColumnRelationMapperFactory.analysisClassRelation(tClass);
        List<FieldColumnRelation> ids = mapper.getIds();
        if (ids.size() != objects.length) {
            throw new RuntimeException("主键信息需要填写完整");
        }
        MySearchList mySearchList = MySearchList.create(tClass);
        for (int i = 0; i < ids.size(); i++) {
            FieldColumnRelation relation = ids.get(i);
            Object value = objects[i];
            mySearchList.eq(relation.getFieldName(), value);
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
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity selectSqlEntity = sqlModelUtils.select();
        String sql = selectSqlEntity.getSelectSql();
        log.debug(sql);
        String countSql = selectSqlEntity.getCountSql();
        log.info(countSql);
        List finalList = selectSqlEntity.getValueList();
        //List list = handleJdbcReturnValue(sql, finalList);

        List<T> tList = handleJdbcReturnValue(sql,finalList,sqlModelUtils);//sqlModelUtils.loadingObject(sqlModelUtils.mergeMapList(sqlModelUtils.handleMap(list)));
        Long count = jdbcTemplate.queryForObject(countSql, finalList.toArray(), Long.class);
        if (count == null) {
            count = 0L;
        }
        Page<T> iPage = new Page<>();
        SqlLimit sqlLimit = mySearchList.getSqlLimit();
        iPage.setCurrent(sqlLimit.getCurrent());
        iPage.setSize(sqlLimit.getSize());
        iPage.setRecords(tList);
        iPage.setTotal(count);
        return iPage;
    }

    public <T> IPage<T> getTPage(T t, Page page) {
        MySearchList mySearchList = SQLModelUtils.objectToMySearchList(t);
        mySearchList.setSqlLimit(sqlLimit -> sqlLimit.setCurrent(page.getCurrent()).setSize(page.getSize()));
        return getTPage(mySearchList);
    }


    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
