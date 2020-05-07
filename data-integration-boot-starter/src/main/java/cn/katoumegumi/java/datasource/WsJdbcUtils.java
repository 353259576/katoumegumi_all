package cn.katoumegumi.java.datasource;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SQLModelUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author 王松
 */
public class WsJdbcUtils {

    public static final Logger log = LoggerFactory.getLogger(WsJdbcUtils.class);


    private JdbcTemplate jdbcTemplate;


    public <T> T insert(T t){
        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.setMainClass(t.getClass());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        String insertSql = sqlModelUtils.insertSql(1);
        log.debug(insertSql);
        List valueList = sqlModelUtils.insertValue(t);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(createPreparedStatement(valueList,insertSql),keyHolder);
        Map<String,Object> keyMap = keyHolder.getKeys();
        log.info(JSON.toJSONString(keyMap));
        return t;
    }


    private PreparedStatementCreator createPreparedStatement(List valueList,String insertSql){
        PreparedStatementCreator preparedStatementCreator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
                Object o;
                for(int i = 0; i < valueList.size(); i++){
                    o = valueList.get(i);
                    if(o instanceof String){
                        statement.setString(i+1, WsStringUtils.anyToString(o));
                    }else if(o instanceof Integer || WsFieldUtils.classCompare(int.class,o.getClass())) {
                        statement.setInt(i + 1, WsBeanUtis.objectToT(o, int.class));
                    }else if(o instanceof Long || WsFieldUtils.classCompare(o.getClass(),long.class)){
                        statement.setLong(i+1,WsBeanUtis.objectToT(o,long.class));
                    }else if(o instanceof Short || WsFieldUtils.classCompare(o.getClass(),short.class)){
                        statement.setShort(i+1,WsBeanUtis.objectToT(o,short.class));
                    }else if(o instanceof Float || WsFieldUtils.classCompare(o.getClass(),Float.class)){
                        statement.setFloat(i+1,WsBeanUtis.objectToT(o,float.class));
                    }else if(o instanceof Double || WsFieldUtils.classCompare(o.getClass(),double.class)){
                        statement.setDouble(i+1,WsBeanUtis.objectToT(o,double.class));
                    }else if (o instanceof BigDecimal){
                        statement.setBigDecimal(i + 1,WsBeanUtis.objectToT(o,BigDecimal.class));
                    }else if(o instanceof Date){
                        statement.setString(i + 1,WsBeanUtis.objectToT(o, String.class));
                    }else if (o instanceof LocalDate || o instanceof LocalDateTime){
                        statement.setString(i + 1,WsBeanUtis.objectToT(o, String.class));
                    }else {
                        throw new RuntimeException("不支持的数据类型:"+o.getClass());
                    }
                }
                return statement;
            }
        };
        return preparedStatementCreator;
    }


    /**
     * 查询列表
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> List<T> getListT(MySearchList mySearchList){
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        String sql = sqlModelUtils.searchListBaseSQLProcessor();
        log.debug(sql);
        Map<Integer,Object> map = sqlModelUtils.getValueMap();
        List<String> nameList = new ArrayList<>();
        List list = new ArrayList();
        List finalList = list;
        map.forEach((integer, o) -> {
            finalList.add(o);
        });
        list = jdbcTemplate.query(sql,finalList.toArray(),new RowMapper<Map>() {
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
     * @param mySearchList
     * @param <T>
     * @return
     */
    public <T> T getTOne(MySearchList mySearchList){
        List<T> tList = getListT(mySearchList);
        if(WsListUtils.isNotEmpty(tList)){
            if(tList.size() > 1){
                log.warn("本次查询数据大于一条但是仅显示一条。");
            }
            return tList.get(0);
        }
        return null;
    }

    /**
     * 分页查询
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
