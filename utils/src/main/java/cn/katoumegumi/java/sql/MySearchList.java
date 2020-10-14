package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 查询条件构造器
 * @author ws
 */
public class MySearchList {
    private final List<MySearch> orderSearches = new ArrayList<>();
    private final List<MySearchList> ands = new ArrayList<>();
    private final List<MySearchList> ors = new ArrayList<>();
    //private final Map<Class, String> tableAndNickNameMap = new HashMap<>();
    private final List<TableRelation> joins = new ArrayList<>();
    private List<MySearch> mySearches = new ArrayList<>();
    private Class<?> mainClass;
    private String alias;

    private JoinType defaultJoinType = JoinType.INNER;

    private SqlLimit sqlLimit;

    public MySearchList() {

    }

    public MySearchList(List<MySearch> mySearches) {
        this.mySearches = mySearches;
    }

    public static MySearchList newMySearchList() {
        return new MySearchList();
    }

    public static MySearchList newMySearchList(List<MySearch> mySearches) {
        return new MySearchList(mySearches);
    }

    public static MySearchList create() {
        return new MySearchList();
    }

    public static <T> MySearchList create(Class<T> tClass) {
        return new MySearchList().setMainClass(tClass);
    }


    public MySearchList add(MySearch mySearch) {
        mySearches.add(mySearch);
        return this;
    }
    
    public MySearchList add(String fieldName, SqlOperator operator, Object value) {
        if (value instanceof SupplierFunc) {
            value = WsFieldUtils.getFieldName((SupplierFunc<?>) value);
        }
        checkValue(fieldName,operator,value);
        if(operator.equals(SqlOperator.SORT)){
            orderSearches.add(new MySearch(fieldName,operator,value));
            return this;
        }
        mySearches.add(new MySearch(fieldName, operator, value));
        return this;
    }

    public <T> MySearchList add(SupplierFunc<T> supplierFunc, SqlOperator operator, Object value) {
        return add(null,supplierFunc,operator,value);
    }

    public <T> MySearchList add(String tableName,SupplierFunc<T> supplierFunc, SqlOperator operator, Object value) {
        return add(getColumnName(tableName,supplierFunc), operator, value);
    }

    public MySearch get(int i) {
        return mySearches.get(i);
    }

    public List<MySearch> getAll() {
        return mySearches;
    }

    public boolean isEmpty() {
        return mySearches.isEmpty() && ands.isEmpty() && ors.isEmpty();
    }

    public Iterator<MySearch> iterator() {
        return mySearches.iterator();
    }


    public MySearch remove(String name) {
        MySearch mySearch = null;
        for (int i = 0; i < mySearches.size(); i++) {
            mySearch = mySearches.get(i);
            if (mySearch.getFieldName().equals(name)) {
                mySearches.remove(i);
                return mySearch;
            }
        }
        return null;
    }

    public MySearch get(String value) {
        for (MySearch m : mySearches) {
            if (m.getFieldName().equals(value)) {
                return m;
            }
        }
        return null;
    }

    public MySearch get(String value, SqlOperator sqlOperator) {
        for (MySearch m : mySearches) {
            if (m.getFieldName().equals(value) && m.getOperator().equals(sqlOperator)) {
                return m;
            }
        }
        return null;
    }


    public MySearchList setPageVO(Page pageVO) {
        SqlLimit sqlLimit = new SqlLimit();
        sqlLimit.setCurrent(pageVO.getCurrent());
        if(pageVO.getCurrent() < 1L){
            pageVO.setCurrent(1L);
        }
        sqlLimit.setOffset((pageVO.getCurrent() - 1) * pageVO.getSize());
        sqlLimit.setSize(pageVO.getSize());
        this.sqlLimit = sqlLimit;
        return this;
    }

    public SqlLimit getSqlLimit() {
        return sqlLimit;
    }

    public MySearchList setSqlLimit(Consumer<SqlLimit> sqlLimitConsumer) {
        SqlLimit sqlLimit = new SqlLimit();
        sqlLimitConsumer.accept(sqlLimit);
        this.sqlLimit = sqlLimit;
        return this;
    }



    public Class<?> getMainClass() {
        return mainClass;
    }

    public MySearchList setMainClass(Class<?> mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    /**
     * 等于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList eq(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQ,value);
    }

    public MySearchList eq(String columnFieldName,Object value){
        return eq(null,columnFieldName,value);
    }

    public MySearchList eq(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQ,value);
    }

    public MySearchList eq(SupplierFunc<?> columnFieldName,Object value){
        return eq(null,columnFieldName,value);
    }


    /**
     * 不等于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList ne(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NE,value);
    }

    public MySearchList ne(String columnFieldName,Object value){
        return ne(null,columnFieldName,value);
    }

    public MySearchList ne(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NE,value);
    }

    public MySearchList ne(SupplierFunc<?> columnFieldName,Object value){
        return eq(null,columnFieldName,value);
    }


    /**
     * 大于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList gt(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GT,value);
    }

    public MySearchList gt(String columnFieldName,Object value){
        return gt(null,columnFieldName,value);
    }

    public MySearchList gt(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GT,value);
    }

    public MySearchList gt(SupplierFunc<?> columnFieldName,Object value){
        return gt(null,columnFieldName,value);
    }

    /**
     * 大于等于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList gte(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTE,value);
    }

    public MySearchList gte(String columnFieldName,Object value){
        return gte(null,columnFieldName,value);
    }

    public MySearchList gte(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTE,value);
    }

    public MySearchList gte(SupplierFunc<?> columnFieldName,Object value){
        return gte(null,columnFieldName,value);
    }

    /**
     * 小于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList lt(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LT,value);
    }

    public MySearchList lt(String columnFieldName,Object value){
        return lt(null,columnFieldName,value);
    }

    public MySearchList lt(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LT,value);
    }

    public MySearchList lt(SupplierFunc<?> columnFieldName,Object value){
        return lt(null,columnFieldName,value);
    }


    /**
     * 小于等于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList lte(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTE,value);
    }

    public MySearchList lte(String columnFieldName,Object value){
        return lte(null,columnFieldName,value);
    }

    public MySearchList lte(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQ,value);
    }

    public MySearchList lte(SupplierFunc<?> columnFieldName,Object value){
        return lte(null,columnFieldName,value);
    }

    /**
     * in
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList in(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.IN,value);
    }

    public MySearchList in(String columnFieldName,Object value){
        return in(null,columnFieldName,value);
    }

    public MySearchList in(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.IN,value);
    }

    public MySearchList in(SupplierFunc<?> columnFieldName,Object value){
        return in(null,columnFieldName,value);
    }

    /**
     * not in
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList nIn(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NIN,value);
    }

    public MySearchList nIn(String columnFieldName,Object value){
        return nIn(null,columnFieldName,value);
    }

    public MySearchList nIn(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NIN,value);
    }

    public MySearchList nIn(SupplierFunc<?> columnFieldName,Object value){
        return nIn(null,columnFieldName,value);
    }

    /**
     * 模糊查询
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList like(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LIKE,value);
    }

    public MySearchList like(String columnFieldName,Object value){
        return like(null,columnFieldName,value);
    }

    public MySearchList like(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LIKE,value);
    }

    public MySearchList like(SupplierFunc<?> columnFieldName,Object value){
        return like(null,columnFieldName,value);
    }


    /**
     * 为空
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList isNull(String tableName,String columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NULL,null);
    }

    public MySearchList isNull(String columnFieldName){
        return isNull(null,columnFieldName);
    }

    public MySearchList isNull(String tableName,SupplierFunc<?> columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NULL,null);
    }

    public MySearchList isNull(SupplierFunc<?> columnFieldName){
        return isNull(null,columnFieldName);
    }





    /**
     * 不为空
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList isNotNull(String tableName,String columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOTNULL,null);
    }

    public MySearchList isNotNull(String columnFieldName){
        return isNotNull(null,columnFieldName);
    }

    public MySearchList isNotNull(String tableName,SupplierFunc<?> columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOTNULL,null);
    }

    public MySearchList isNotNull(SupplierFunc<?> columnFieldName){
        return isNotNull(null,columnFieldName);
    }

    /**
     * 排序
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList sort(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SORT,value);
    }

    public MySearchList sort(String columnFieldName,Object value){
        return sort(null,columnFieldName,value);
    }

    public MySearchList sort(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SORT,value);
    }

    public MySearchList sort(SupplierFunc<?> columnFieldName,Object value){
        return sort(null,columnFieldName,value);
    }


    /**
     * between
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList between(String tableName,String columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.BETWEEN,Arrays.asList(value1,value2));
    }

    public MySearchList between(String columnFieldName,Object value1,Object value2){
        return between(null,columnFieldName,value1,value2);
    }

    public MySearchList between(String tableName,SupplierFunc<?> columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.BETWEEN,Arrays.asList(value1,value2));
    }

    public MySearchList between(SupplierFunc<?> columnFieldName,Object value1,Object value2){
        return between(null,columnFieldName,value1,value2);
    }


    /**
     * not between
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList notBetween(String tableName,String columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOT_BETWEEN,Arrays.asList(value1,value2));
    }

    public MySearchList notBetween(String columnFieldName,Object value1,Object value2){
        return notBetween(null,columnFieldName,value1,value2);
    }

    public MySearchList notBetween(String tableName,SupplierFunc<?> columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOT_BETWEEN,Arrays.asList(value1,value2));
    }

    public MySearchList notBetween(SupplierFunc<?> columnFieldName,Object value1,Object value2){
        return notBetween(null,columnFieldName,value1,value2);
    }

    /**
     * 插入sql语句
     * @param sql
     * @param value
     * @return
     */
    public MySearchList sql(String sql,Object value){
        return add(sql,SqlOperator.SQL,value);
    }

    /**
     * exists
     * @param sql
     * @param value
     * @return
     */
    public MySearchList exists(String sql,Object value){
        return add(sql,SqlOperator.EXISTS,value);
    }

    /**
     * not exists
     * @param sql
     * @param value
     * @return
     */
    public MySearchList notExists(String sql,Object value){
        return add(sql,SqlOperator.NOT_EXISTS,value);
    }


    /**
     * 等于
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList eqp(String tableName,String columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQP,getColumnName(valueTableName,value));
    }

    public MySearchList eqp(String tableName,String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQP,getColumnName(valueTableName,value));
    }

    public MySearchList eqp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,SupplierFunc<?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.EQP, getColumnName(valueTableName, value));
    }

    public MySearchList eqp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQP,getColumnName(valueTableName,value));
    }

    public MySearchList eqp(String columnFieldName,String value){
        return eqp(null,columnFieldName,null,value);
    }

    public MySearchList eqp(String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return eqp(null,columnFieldName,valueTableName,value);
    }

    public MySearchList eqp(String columnFieldName,SupplierFunc<?> value) {
        return eqp(null, columnFieldName, null, value);
    }

    public MySearchList eqp(SupplierFunc<?> columnFieldName,SupplierFunc<?> value){
        return eqp(null,columnFieldName,null,value);
    }

    public MySearchList eqp(String tableName,SupplierFunc<?> columnFieldName,String value){
        return eqp(tableName,columnFieldName,null,value);
    }


    /**
     * 大于
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList gtp(String tableName,String columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTP,getColumnName(valueTableName,value));
    }

    public MySearchList gtp(String tableName,String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTP,getColumnName(valueTableName,value));
    }

    public MySearchList gtp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,SupplierFunc<?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.GTP, getColumnName(valueTableName, value));
    }

    public MySearchList gtp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTP,getColumnName(valueTableName,value));
    }

    public MySearchList gtp(String columnFieldName,String value){
        return gtp(null,columnFieldName,null,value);
    }

    public MySearchList gtp(String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return gtp(null,columnFieldName,valueTableName,value);
    }

    public MySearchList gtp(String columnFieldName,SupplierFunc<?> value) {
        return gtp(null, columnFieldName, null, value);
    }

    public MySearchList gtp(SupplierFunc<?> columnFieldName,SupplierFunc<?> value){
        return gtp(null,columnFieldName,null,value);
    }

    public MySearchList gtp(String tableName,SupplierFunc<?> columnFieldName,String value){
        return gtp(tableName,columnFieldName,null,value);
    }


    /**
     * 大于等于
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList gtep(String tableName,String columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTEP,getColumnName(valueTableName,value));
    }

    public MySearchList gtep(String tableName,String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTEP,getColumnName(valueTableName,value));
    }

    public MySearchList gtep(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,SupplierFunc<?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.GTEP, getColumnName(valueTableName, value));
    }

    public MySearchList gtep(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTEP,getColumnName(valueTableName,value));
    }

    public MySearchList gtep(String columnFieldName,String value){
        return gtep(null,columnFieldName,null,value);
    }

    public MySearchList gtep(String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return gtep(null,columnFieldName,valueTableName,value);
    }

    public MySearchList gtep(String columnFieldName,SupplierFunc<?> value) {
        return gtep(null, columnFieldName, null, value);
    }

    public MySearchList gtep(SupplierFunc<?> columnFieldName,SupplierFunc<?> value){
        return gtep(null,columnFieldName,null,value);
    }

    public MySearchList gtep(String tableName,SupplierFunc<?> columnFieldName,String value){
        return gtep(tableName,columnFieldName,null,value);
    }


    /**
     * 小于
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList ltp(String tableName,String columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTP,getColumnName(valueTableName,value));
    }

    public MySearchList ltp(String tableName,String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTP,getColumnName(valueTableName,value));
    }

    public MySearchList ltp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,SupplierFunc<?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.LTP, getColumnName(valueTableName, value));
    }

    public MySearchList ltp(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTP,getColumnName(valueTableName,value));
    }

    public MySearchList ltp(String columnFieldName,String value){
        return ltp(null,columnFieldName,null,value);
    }

    public MySearchList ltp(String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return ltp(null,columnFieldName,valueTableName,value);
    }

    public MySearchList ltp(String columnFieldName,SupplierFunc<?> value) {
        return ltp(null, columnFieldName, null, value);
    }

    public MySearchList ltp(SupplierFunc<?> columnFieldName,SupplierFunc<?> value){
        return ltp(null,columnFieldName,null,value);
    }

    public MySearchList ltp(String tableName,SupplierFunc<?> columnFieldName,String value){
        return ltp(tableName,columnFieldName,null,value);
    }

    /**
     * 小于等于
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList ltep(String tableName,String columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTEP,getColumnName(valueTableName,value));
    }

    public MySearchList ltep(String tableName,String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTEP,getColumnName(valueTableName,value));
    }

    public MySearchList ltep(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,SupplierFunc<?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.LTEP, getColumnName(valueTableName, value));
    }

    public MySearchList ltep(String tableName,SupplierFunc<?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTEP,getColumnName(valueTableName,value));
    }

    public MySearchList ltep(String columnFieldName,String value){
        return ltep(null,columnFieldName,null,value);
    }

    public MySearchList ltep(String columnFieldName,String valueTableName,SupplierFunc<?> value){
        return ltep(null,columnFieldName,valueTableName,value);
    }

    public MySearchList ltep(String columnFieldName,SupplierFunc<?> value) {
        return ltep(null, columnFieldName, null, value);
    }

    public MySearchList ltep(SupplierFunc<?> columnFieldName,SupplierFunc<?> value){
        return ltep(null,columnFieldName,null,value);
    }

    public MySearchList ltep(String tableName,SupplierFunc<?> columnFieldName,String value){
        return ltep(tableName,columnFieldName,null,value);
    }





    //update

    /**
     * set
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList set(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SET,value);
    }

    public MySearchList set(String columnFieldName,Object value){
        return set(null,columnFieldName,value);
    }

    public MySearchList set(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SET,value);
    }

    public MySearchList set(SupplierFunc<?> columnFieldName,Object value){
        return set(null,columnFieldName,value);
    }

    /**
     * 加
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList add(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.ADD,value);
    }

    public MySearchList add(String columnFieldName,Object value){
        return add(null,columnFieldName,value);
    }

    public MySearchList add(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.ADD,value);
    }

    public MySearchList add(SupplierFunc<?> columnFieldName,Object value) {
        return add(null, columnFieldName, value);
    }

    /**
     * 减
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList subtract(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SUBTRACT,value);
    }

    public MySearchList subtract(String columnFieldName,Object value){
        return subtract(null,columnFieldName,value);
    }

    public MySearchList subtract(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SUBTRACT,value);
    }

    public MySearchList subtract(SupplierFunc<?> columnFieldName,Object value) {
        return subtract(null, columnFieldName, value);
    }

    /**
     * 乘
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList multiply(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.MULTIPLY,value);
    }

    public MySearchList multiply(String columnFieldName,Object value){
        return multiply(null,columnFieldName,value);
    }

    public MySearchList multiply(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.MULTIPLY,value);
    }

    public MySearchList multiply(SupplierFunc<?> columnFieldName,Object value){
        return multiply(null,columnFieldName,value);
    }

    /**
     * 除
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList divide(String tableName,String columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.DIVIDE,value);
    }

    public MySearchList divide(String columnFieldName,Object value){
        return divide(null,columnFieldName,value);
    }

    public MySearchList divide(String tableName,SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.DIVIDE,value);
    }

    public MySearchList divide(SupplierFunc<?> columnFieldName,Object value){
        return divide(null,columnFieldName,value);
    }









    /**
     * 拼装列名
     * @param tableName
     * @param columnName
     * @return
     */
    private static String getColumnName(String tableName,SupplierFunc<?> columnName){
        return getColumnName(tableName,WsFieldUtils.getFieldName(columnName));
    }

    private static String getColumnName(String tableName,String columnName){
        if(WsStringUtils.isBlank(tableName)){
            return columnName;
        }else {
            return tableName + '.' + columnName;
        }
    }

    /**
     * 判断值是不是正确
     * @param fieldName
     * @param operator
     * @param value
     */
    private static void checkValue(String fieldName,SqlOperator operator,Object value){
        switch (operator){
            case EQ:
            case NE:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case LIKE:
            case SET:
            case ADD:
            case SUBTRACT:
            case DIVIDE:
            case MULTIPLY:
                if(!WsBeanUtils.isBaseType(value.getClass())){
                    throw new IllegalArgumentException(fieldName+"的参数必须为基本类型，当前的类型是："+value.getClass());
                }
                break;
            case SORT:
            case EQP:
            case LTP:
            case GTP:
            case GTEP:
            case LTEP:
            case NEP:
                if(!(value instanceof String)){
                    throw new RuntimeException(fieldName+"的参数必须为字符串类型，当前的类型是："+value.getClass());
                }else {
                    if(WsStringUtils.isBlank((String)value)){
                        throw new RuntimeException(fieldName+"参数不能为空");
                    }
                }
                break;
            case IN:
            case NIN:
            case BETWEEN:
            case NOT_BETWEEN:
                if(!WsBeanUtils.isArray(value.getClass())){
                    throw new RuntimeException(fieldName+"的参数必须是数组类型，当前的类型是："+value.getClass());
                }
                break;
            case NULL:
            case NOTNULL:
            case SQL:
            case EXISTS:
            case NOT_EXISTS:
            case OR:
            case AND:break;
            default:
                throw new RuntimeException("不支持的查询方式");
        }
    }




    public MySearchList and(MySearchList... mySearchLists) {
        ands.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public MySearchList and(Supplier<MySearchList> supplier) {
        ands.add(supplier.get());
        return this;
    }

    public MySearchList or(MySearchList... mySearchLists) {
        ors.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public MySearchList or(Supplier<MySearchList> supplier) {
        ors.add(supplier.get());
        return this;
    }

    @SafeVarargs
    public final MySearchList and(Consumer<MySearchList>... consumers){
        for(Consumer<MySearchList> consumer:consumers){
            MySearchList mySearchList = MySearchList.create();
            consumer.accept(mySearchList);
            if(WsListUtils.isNotEmpty(mySearchList.getAll()) || WsListUtils.isNotEmpty(mySearchList.getAnds()) || WsListUtils.isNotEmpty(mySearchList.getOrs())){
                ands.add(mySearchList);
            }
        }

        return this;
    }

    @SafeVarargs
    public final MySearchList or(Consumer<MySearchList>... consumers){
        for(Consumer<MySearchList> consumer:consumers){
            MySearchList mySearchList = MySearchList.create();
            consumer.accept(mySearchList);
            if(WsListUtils.isNotEmpty(mySearchList.getAll()) || WsListUtils.isNotEmpty(mySearchList.getAnds()) || WsListUtils.isNotEmpty(mySearchList.getOrs())){
                ors.add(mySearchList);
            }
        }
        return this;
    }

    /**
     * 连接其他表
     *
     * @param tableNickName     已存在的表名
     * @param joinTableClass    连接对象类型
     * @param joinTableNickName 连接对象表名
     * @param tableColumn       已存在表的字段
     * @param joinColumn        连接表的字段
     * @param joinType          连接类型
     * @param <T>
     * @return
     */
    public <T> MySearchList join(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn, JoinType joinType) {
        TableRelation tableRelation = new TableRelation();
        tableRelation.setJoinTableClass(joinTableClass);
        tableRelation.setTableNickName(tableNickName);
        tableRelation.setJoinTableNickName(joinTableNickName);
        tableRelation.setTableColumn(tableColumn);
        tableRelation.setJoinTableColumn(joinColumn);
        tableRelation.setJoinType(joinType);
        joins.add(tableRelation);
        return this;
    }

    public <T> MySearchList join(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.INNER);
    }

    public <T> MySearchList join(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn);
    }

    public <T> MySearchList leftJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.LEFT);
    }

    public <T> MySearchList leftJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.LEFT);
    }

    public <T> MySearchList innerJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.INNER);
    }

    public <T> MySearchList innerJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.INNER);
    }

    public <T> MySearchList rightJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.RIGHT);
    }

    public <T> MySearchList rightJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, JoinType.RIGHT);
    }

    public List<TableRelation> getJoins() {
        return joins;
    }


    public List<MySearchList> getAnds() {
        return ands;
    }

    public List<MySearchList> getOrs() {
        return ors;
    }

    public List<MySearch> getOrderSearches() {
        return orderSearches;
    }

    public JoinType getDefaultJoinType() {
        return defaultJoinType;
    }

    public MySearchList setDefaultJoinType(JoinType defaultJoinType) {
        this.defaultJoinType = defaultJoinType;
        return this;
    }

    /**
     * 内联
     * @param tClass
     * @return
     */
    public TableRelation innerJoin(Class<?> tClass){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(JoinType.INNER);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    };

    /**
     * 左联
     * @param tClass
     * @return
     */
    public TableRelation leftJoin(Class<?> tClass){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(JoinType.LEFT);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    };

    /**
     * 右连
     * @param tClass
     * @return
     */
    public TableRelation rightJoin(Class<?> tClass){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(JoinType.RIGHT);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    };

    /**
     * 表连接
     * @param consumer
     * @return
     */
    public TableRelation join(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinTableClass(tClass);
        consumer.accept(tableRelation);
        if(WsStringUtils.isBlank(tableRelation.getJoinTableNickName())){
            tableRelation.setJoinTableNickName(tableRelation.getJoinTableClass().getSimpleName());
        }
        joins.add(tableRelation);
        return tableRelation;
    }


    /**
     * 内联
     * @param tClass
     * @return
     */
    public MySearchList innerJoin(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = join(tClass,consumer);
        tableRelation.setJoinType(JoinType.INNER);
        return this;
    };

    /**
     * 左联
     * @param tClass
     * @return
     */
    public MySearchList leftJoin(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = join(tClass,consumer);
        tableRelation.setJoinType(JoinType.LEFT);
        return this;
    };

    /**
     * 右连
     * @param tClass
     * @return
     */
    public MySearchList rightJoin(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = join(tClass,consumer);
        tableRelation.setJoinType(JoinType.RIGHT);
        return this;
    };

    /**
     * 获取主表别名
     * @return
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 设置主表别名
     * @param alias
     * @return
     */
    public MySearchList setAlias(String alias) {
        this.alias = alias;
        return this;
    }
}

