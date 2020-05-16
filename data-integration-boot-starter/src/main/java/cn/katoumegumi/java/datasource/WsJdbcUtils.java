package cn.katoumegumi.java.datasource;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
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
 * @author 王松
 */
public class WsJdbcUtils {

    public static final Logger log = LoggerFactory.getLogger(WsJdbcUtils.class);


    private JdbcTemplate jdbcTemplate;

    public <T> T insert(T t) {
        return insert(t, true);
    }

    public <T> List<T> insert(List<T> tList) {
        return insert(tList, true);
    }

    public <T> T insert(T t, boolean isAuto) {
        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.setMainClass(t.getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelUtils.insertSql(t, isAuto);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        Map<String, Object> keyMap = keyHolder.getKeys();
        if (keyMap.size() > 0) {
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
                        fieldColumnRelation.getField().set(t, WsBeanUtis.objectToT(keyMap.get(unUsedSet.remove(0)), fieldColumnRelation.getField().getType()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }

            }
        }


        return t;
    }

    public <T> List<T> insert(List<T> tList, boolean isAuto) {
        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.setMainClass(tList.get(0).getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        InsertSqlEntity insertSqlEntity = sqlModelUtils.insertSqlBatch(tList, isAuto);
        log.debug(insertSqlEntity.getInsertSql());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(createPreparedStatement(insertSqlEntity), keyHolder);
        List<Map<String, Object>> keyMapList = keyHolder.getKeyList();

        if (keyMapList.size() > 0) {
            Map<String, FieldColumnRelation> stringFieldColumnRelationMap = new HashMap<>();
            for (FieldColumnRelation fieldColumnRelation : insertSqlEntity.getIdList()) {
                stringFieldColumnRelationMap.put(fieldColumnRelation.getColumnName(), fieldColumnRelation);
            }

            for (int i = 0; i < keyMapList.size(); i++) {
                Map<String, Object> objectMap = keyMapList.get(i);
                List<String> unUsedSet = new ArrayList<>();
                T t = tList.get(i);
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
                            fieldColumnRelation.getField().set(tList.get(i), WsBeanUtis.objectToT(objectMap.get(unUsedSet.remove(0)), fieldColumnRelation.getField().getType()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
        }
        //log.info(JSON.toJSONString(keyHolder));
        return tList;
    }


    private PreparedStatementCreator createPreparedStatement(InsertSqlEntity insertSqlEntity) {
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(insertSqlEntity.getInsertSql(), Statement.RETURN_GENERATED_KEYS);
                Object o;
                List valueList = insertSqlEntity.getValueList();
                List<FieldColumnRelation> validList = insertSqlEntity.getUsedField();
                for (int i = 0; i < valueList.size(); i++) {
                    o = valueList.get(i);
                    if (o == null) {
                        statement.setNull(i + 1, Types.NULL);
                        continue;
                    }
                    if (o instanceof String) {
                        statement.setString(i + 1, WsStringUtils.anyToString(o));
                    } else if (o instanceof Integer || WsFieldUtils.classCompare(int.class, o.getClass())) {
                        statement.setInt(i + 1, WsBeanUtis.objectToT(o, int.class));
                    } else if (o instanceof Long || WsFieldUtils.classCompare(o.getClass(), long.class)) {
                        statement.setLong(i + 1, WsBeanUtis.objectToT(o, long.class));
                    } else if (o instanceof Short || WsFieldUtils.classCompare(o.getClass(), short.class)) {
                        statement.setShort(i + 1, WsBeanUtis.objectToT(o, short.class));
                    } else if (o instanceof Float || WsFieldUtils.classCompare(o.getClass(), Float.class)) {
                        statement.setFloat(i + 1, WsBeanUtis.objectToT(o, float.class));
                    } else if (o instanceof Double || WsFieldUtils.classCompare(o.getClass(), double.class)) {
                        statement.setDouble(i + 1, WsBeanUtis.objectToT(o, double.class));
                    } else if (o instanceof BigDecimal) {
                        statement.setBigDecimal(i + 1, WsBeanUtis.objectToT(o, BigDecimal.class));
                    } else if (o instanceof Date) {
                        statement.setString(i + 1, WsBeanUtis.objectToT(o, String.class));
                    } else if (o instanceof LocalDate || o instanceof LocalDateTime) {
                        statement.setString(i + 1, WsBeanUtis.objectToT(o, String.class));
                    } else {
                        throw new RuntimeException("不支持的数据类型:" + o.getClass());
                    }
                }
                return statement;
            }
        };
        return preparedStatementCreator;
    }


    public <T> void update(T t) {
        MySearchList mySearchList = MySearchList.newMySearchList().setMainClass(t.getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(t);
        log.debug(updateSqlEntity.getUpdateSql());
        jdbcTemplate.update(updateSqlEntity.getUpdateSql(), updateSqlEntity.getValueList().toArray());
    }

    public void update(MySearchList mySearchList) {
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(mySearchList);
        log.debug(updateSqlEntity.getUpdateSql());
        jdbcTemplate.update(updateSqlEntity.getUpdateSql(), updateSqlEntity.getValueList().toArray());
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
        String sql = sqlModelUtils.searchListBaseSQLProcessor();
        log.debug(sql);
        Map<Integer, Object> map = sqlModelUtils.getValueMap();
        List<String> nameList = new ArrayList<>();
        List list = new ArrayList();
        List finalList = list;
        map.forEach((integer, o) -> {
            finalList.add(o);
        });
        list = jdbcTemplate.query(sql, finalList.toArray(), new RowMapper<Map>() {
            @Override
            public Map mapRow(java.sql.ResultSet resultSet, int i) throws SQLException {
                if (i < 1) {
                    int length = resultSet.getMetaData().getColumnCount();
                    for (int j = 0; j < length; j++) {
                        nameList.add(resultSet.getMetaData().getColumnLabel(j + 1));
                    }
                }
                Map map = new HashMap();
                for (int j = 0; j < nameList.size(); j++) {
                    map.put(nameList.get(j), resultSet.getObject(j + 1));
                }
                return map;
            }
        });
        return sqlModelUtils.loadingObject(sqlModelUtils.mergeMapList(sqlModelUtils.handleMap(list)));
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

    /**
     * 分页查询
     *
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> IPage<T> getTPage(MySearchList mySearchList) {
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        String sql = sqlModelUtils.searchListBaseSQLProcessor();
        log.debug(sql);
        String countSql = sqlModelUtils.searchListBaseCountSQLProcessor();
        Map<Integer, Object> map = sqlModelUtils.getValueMap();
        List<String> nameList = new ArrayList<>();
        List list = new ArrayList();
        List finalList = list;
        map.forEach((integer, o) -> {
            finalList.add(o);
        });
        list = jdbcTemplate.query(sql, finalList.toArray(), new RowMapper<Map>() {
            @Override
            public Map mapRow(java.sql.ResultSet resultSet, int i) throws SQLException {
                if (i < 1) {
                    int length = resultSet.getMetaData().getColumnCount();
                    for (int j = 0; j < length; j++) {
                        nameList.add(resultSet.getMetaData().getColumnLabel(j + 1));
                    }
                }
                Map map = new HashMap();
                for (int j = 0; j < nameList.size(); j++) {
                    map.put(nameList.get(j), resultSet.getObject(j + 1));
                }
                return map;
            }
        });

        List<T> tList = sqlModelUtils.loadingObject(sqlModelUtils.mergeMapList(sqlModelUtils.handleMap(list)));
        Long count = jdbcTemplate.queryForObject(countSql, finalList.toArray(), Long.class);
        if (count == null) {
            count = 0L;
        }
        Page iPage = mySearchList.getPageVO();
        iPage.setRecords(tList);
        iPage.setTotal(count);
        return iPage;
    }


    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}