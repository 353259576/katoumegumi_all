package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.entity.SqlLimit;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 查询条件构造器
 * @author ws
 */
public class MySearchList {

    private final List<String> columnNameList = new ArrayList<>();

    private final List<MySearch> orderSearches = new ArrayList<>();

    private final List<MySearchList> ands = new ArrayList<>();

    private final List<MySearchList> ors = new ArrayList<>();

    private final List<TableRelation> joins = new ArrayList<>();

    private List<MySearch> mySearches = new ArrayList<>();

    private Class<?> mainClass;

    private String alias;

    private TableJoinType defaultJoinType = TableJoinType.INNER_JOIN;

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
        MySearch mySearch;
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


    /*public MySearchList setPageVO(Page pageVO) {
        SqlLimit sqlLimit = new SqlLimit();
        sqlLimit.setCurrent(pageVO.getCurrent());
        sqlLimit.setSize(pageVO.getSize());
        sqlLimit.build();
        this.sqlLimit = sqlLimit;
        return this;
    }*/

    public SqlLimit getSqlLimit() {
        return sqlLimit;
    }

    public MySearchList setSqlLimit(Consumer<SqlLimit> sqlLimitConsumer) {
        SqlLimit sqlLimit = new SqlLimit();
        sqlLimitConsumer.accept(sqlLimit);
        sqlLimit.build();
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

    public <T> MySearchList eq(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQ,value);
    }

    public <T> MySearchList eq(SFunction<T,?> columnFieldName,Object value){
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
        return ne(null,columnFieldName,value);
    }

    public <T> MySearchList ne(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NE,value);
    }

    public <T> MySearchList ne(SFunction<T,?> columnFieldName,Object value){
        return ne(null,columnFieldName,value);
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

    public <T> MySearchList gt(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GT,value);
    }

    public <T> MySearchList gt(SFunction<T,?> columnFieldName,Object value){
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

    public <T> MySearchList gte(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTE,value);
    }

    public <T> MySearchList gte(SFunction<T,?> columnFieldName,Object value){
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

    public <T> MySearchList lt(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LT,value);
    }

    public <T> MySearchList lt(SFunction<T,?> columnFieldName,Object value){
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
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTE,value);
    }

    public MySearchList lte(SupplierFunc<?> columnFieldName,Object value){
        return lte(null,columnFieldName,value);
    }

    public <T> MySearchList lte(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTE,value);
    }

    public <T> MySearchList lte(SFunction<T,?> columnFieldName,Object value){
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

    public <T> MySearchList in(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.IN,value);
    }

    public <T> MySearchList in(SFunction<T,?> columnFieldName,Object value){
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

    public <T> MySearchList nIn(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NIN,value);
    }

    public <T> MySearchList nIn(SFunction<T,?> columnFieldName,Object value){
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

    public MySearchList like(SupplierFunc<?> columnFieldName,Object value) {
        return like(null, columnFieldName, value);
    }

    public <T> MySearchList like(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LIKE,value);
    }

    public <T> MySearchList like(SFunction<T,?> columnFieldName,Object value){
        return like(null,columnFieldName,value);
    }


    /**
     * 为空
     * @param tableName
     * @param columnFieldName
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

    public <T> MySearchList isNull(String tableName,SFunction<T,?> columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NULL,null);
    }

    public <T> MySearchList isNull(SFunction<T,?> columnFieldName){
        return isNull(null,columnFieldName);
    }





    /**
     * 不为空
     * @param tableName
     * @param columnFieldName
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

    public <T> MySearchList isNotNull(String tableName,SFunction<T,?> columnFieldName){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOTNULL,null);
    }

    public <T> MySearchList isNotNull(SFunction<T,?> columnFieldName){
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

    public <T> MySearchList sort(String tableName,SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.SORT,value);
    }

    public <T> MySearchList sort(SFunction<T,?> columnFieldName,Object value){
        return sort(null,columnFieldName,value);
    }


    /**
     * between
     * @param tableName
     * @param columnFieldName
     * @param value1
     * @param value2
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

    public <T> MySearchList between(String tableName,SFunction<T,?> columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.BETWEEN,Arrays.asList(value1,value2));
    }

    public <T> MySearchList between(SFunction<T,?> columnFieldName,Object value1,Object value2){
        return between(null,columnFieldName,value1,value2);
    }


    /**
     * not between
     * @param tableName
     * @param columnFieldName
     * @param value1
     * @param value2
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

    public <T> MySearchList notBetween(String tableName,SFunction<T,?> columnFieldName,Object value1,Object value2){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.NOT_BETWEEN,Arrays.asList(value1,value2));
    }

    public <T> MySearchList notBetween(SFunction<T,?> columnFieldName,Object value1,Object value2){
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

    public MySearchList exists(MySearchList mySearchList){
        return add("",SqlOperator.EXISTS,mySearchList);
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

    public MySearchList notExists(MySearchList mySearchList){
        return add("",SqlOperator.NOT_EXISTS,mySearchList);
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

    public <T> MySearchList eqp(String tableName,String columnFieldName,String valueTableName,SFunction<T,?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQP,getColumnName(valueTableName,value));
    }

    public  MySearchList eqp(String tableName,SFunction<?,?> columnFieldName,String valueTableName,SFunction<?,?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.EQP, getColumnName(valueTableName, value));
    }

    public MySearchList eqp(String tableName,SFunction<?,?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.EQP,getColumnName(valueTableName,value));
    }

    public MySearchList eqp(String columnFieldName,String value){
        return eqp(null,columnFieldName,null,value);
    }

    public MySearchList eqp(String columnFieldName,String valueTableName,SFunction<?,?> value){
        return eqp(null,columnFieldName,valueTableName,value);
    }

    public MySearchList eqp(String columnFieldName,SFunction<?,?> value) {
        return eqp(null, columnFieldName, null, value);
    }

    public <T> MySearchList eqp(SFunction<T,?> columnFieldName,SFunction<T,?> value){
        return eqp(null,columnFieldName,null,value);
    }

    public <T> MySearchList eqp(String tableName,SFunction<T,?> columnFieldName,String value){
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

    public <T> MySearchList gtp(String tableName,String columnFieldName,String valueTableName,SFunction<T,?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTP,getColumnName(valueTableName,value));
    }

    public MySearchList gtp(String tableName,SFunction<?,?> columnFieldName,String valueTableName,SFunction<?,?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.GTP, getColumnName(valueTableName, value));
    }

    public <T> MySearchList gtp(String tableName,SFunction<T,?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTP,getColumnName(valueTableName,value));
    }

    public MySearchList gtp(String columnFieldName,String value){
        return gtp(null,columnFieldName,null,value);
    }

    public <T> MySearchList gtp(String columnFieldName,String valueTableName,SFunction<T,?> value){
        return gtp(null,columnFieldName,valueTableName,value);
    }

    public <T> MySearchList gtp(String columnFieldName,SFunction<T,?> value) {
        return gtp(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtp(SFunction<T,?> columnFieldName,SFunction<T,?> value){
        return gtp(null,columnFieldName,null,value);
    }

    public <T> MySearchList gtp(String tableName,SFunction<T,?> columnFieldName,String value){
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

    public <T> MySearchList gtep(String tableName,String columnFieldName,String valueTableName,SFunction<T,?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTEP,getColumnName(valueTableName,value));
    }

    public MySearchList gtep(String tableName,SFunction<?,?> columnFieldName,String valueTableName,SFunction<?,?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.GTEP, getColumnName(valueTableName, value));
    }

    public <T> MySearchList gtep(String tableName,SFunction<T,?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.GTEP,getColumnName(valueTableName,value));
    }

    public MySearchList gtep(String columnFieldName,String value){
        return gtep(null,columnFieldName,null,value);
    }

    public <T> MySearchList gtep(String columnFieldName,String valueTableName,SFunction<T,?> value){
        return gtep(null,valueTableName, columnFieldName, value);
    }

    public <T> MySearchList gtep(String columnFieldName,SFunction<T,?> value) {
        return gtep(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtep(SFunction<T,?> columnFieldName,SFunction<T,?> value){
        return gtep(null,columnFieldName,null,value);
    }

    public <T> MySearchList gtep(String tableName,SFunction<T,?> columnFieldName,String value){
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

    public <T> MySearchList ltp(String tableName,String columnFieldName,String valueTableName,SFunction<T,?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTP,getColumnName(valueTableName,value));
    }

    public MySearchList ltp(String tableName,SFunction<?,?> columnFieldName,String valueTableName,SFunction<?,?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.LTP, getColumnName(valueTableName, value));
    }

    public <T> MySearchList ltp(String tableName,SFunction<T,?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTP,getColumnName(valueTableName,value));
    }

    public MySearchList ltp(String columnFieldName,String value){
        return ltp(null,columnFieldName,null,value);
    }

    public <T> MySearchList ltp(String columnFieldName,String valueTableName,SFunction<T,?> value){
        return ltp(null,columnFieldName,valueTableName,value);
    }

    public <T> MySearchList ltp(String columnFieldName,SFunction<T,?> value) {
        return ltp(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltp(SFunction<T,?> columnFieldName,SFunction<T,?> value){
        return ltp(null,columnFieldName,null,value);
    }

    public <T> MySearchList ltp(String tableName,SFunction<T,?> columnFieldName,String value){
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

    public <T> MySearchList ltep(String tableName,String columnFieldName,String valueTableName,SFunction<T,?> value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTEP,getColumnName(valueTableName,value));
    }

    public MySearchList ltep(String tableName,SFunction<?,?> columnFieldName,String valueTableName,SFunction<?,?> value) {
        return add(getColumnName(tableName, columnFieldName), SqlOperator.LTEP, getColumnName(valueTableName, value));
    }

    public <T> MySearchList ltep(String tableName,SFunction<T,?> columnFieldName,String valueTableName,String value){
        return add(getColumnName(tableName,columnFieldName),SqlOperator.LTEP,getColumnName(valueTableName,value));
    }

    public MySearchList ltep(String columnFieldName,String value){
        return ltep(null,columnFieldName,null,value);
    }

    public <T> MySearchList ltep(String columnFieldName,String valueTableName,SFunction<T,?> value){
        return ltep(null,columnFieldName,valueTableName,value);
    }

    public <T> MySearchList ltep(String columnFieldName,SFunction<T,?> value) {
        return ltep(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltep(SFunction<T,?> columnFieldName,SFunction<T,?> value){
        return ltep(null,columnFieldName,null,value);
    }

    public <T> MySearchList ltep(String tableName,SFunction<T,?> columnFieldName,String value){
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

    public MySearchList set(SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.SET,value);
    }

    public <T> MySearchList set(SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.SET,value);
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

    public MySearchList add(SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.ADD,value);
    }

    public <T> MySearchList add(SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.ADD,value);
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

    public MySearchList subtract(SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.SUBTRACT,value);
    }

    public <T> MySearchList subtract(SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.SUBTRACT,value);
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

    public MySearchList multiply(SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.MULTIPLY,value);
    }

    public <T> MySearchList multiply(SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.MULTIPLY,value);
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

    public MySearchList divide(SupplierFunc<?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.DIVIDE,value);
    }

    public <T> MySearchList divide(SFunction<T,?> columnFieldName,Object value){
        return add(getColumnName(null,columnFieldName),SqlOperator.DIVIDE,value);
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

    private static String getColumnName(String tableName,SFunction<?,?> columnName){
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
            case ADD:
            case SUBTRACT:
            case DIVIDE:
            case MULTIPLY:
                if(value == null){
                    throw new IllegalArgumentException(fieldName+"的参数不能为空");
                }
                if(!WsBeanUtils.isBaseType(value.getClass())){
                    throw new IllegalArgumentException(fieldName+"的参数必须为基本类型，当前的类型是："+value.getClass());
                }
                break;
            case SET:
                if(value != null && !WsBeanUtils.isBaseType(value.getClass())){
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
                if(!(WsBeanUtils.isArray(value.getClass())||value instanceof MySearchList)){
                    throw new RuntimeException(fieldName+"的参数必须是数组类型，当前的类型是："+value.getClass());
                }
                break;
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
    public <T> MySearchList join(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn, TableJoinType joinType) {
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
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.INNER_JOIN);
    }

    public <T> MySearchList join(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn);
    }

    public <T> MySearchList leftJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.LEFT_JOIN);
    }

    public <T> MySearchList leftJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.LEFT_JOIN);
    }

    public <T> MySearchList innerJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.INNER_JOIN);
    }

    public <T> MySearchList innerJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.INNER_JOIN);
    }

    public <T> MySearchList rightJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.RIGHT_JOIN);
    }

    public <T> MySearchList rightJoin(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(tableNickName, joinTableClass, joinTableNickName, tableColumn, joinColumn, TableJoinType.RIGHT_JOIN);
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

    public TableJoinType getDefaultJoinType() {
        return defaultJoinType;
    }

    public MySearchList setDefaultJoinType(TableJoinType defaultJoinType) {
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
        tableRelation.setJoinType(TableJoinType.INNER_JOIN);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

    /**
     * 左联
     * @param tClass
     * @return
     */
    public TableRelation leftJoin(Class<?> tClass){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(TableJoinType.LEFT_JOIN);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

    /**
     * 右连
     * @param tClass
     * @return
     */
    public TableRelation rightJoin(Class<?> tClass){
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(TableJoinType.RIGHT_JOIN);
        tableRelation.setJoinTableClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

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
        tableRelation.setJoinType(TableJoinType.INNER_JOIN);
        return this;
    }

    /**
     * 左联
     * @param tClass
     * @return
     */
    public MySearchList leftJoin(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = join(tClass,consumer);
        tableRelation.setJoinType(TableJoinType.LEFT_JOIN);
        return this;
    }

    /**
     * 右连
     * @param tClass
     * @return
     */
    public MySearchList rightJoin(Class<?> tClass,Consumer<TableRelation> consumer){
        TableRelation tableRelation = join(tClass,consumer);
        tableRelation.setJoinType(TableJoinType.RIGHT_JOIN);
        return this;
    }

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

    /**
     * 设置查询的字段的名称
     * @param columnName
     * @return
     */
    public MySearchList singleColumnName(String columnName){
        this.columnNameList.add(columnName);
        return this;
    }

    public MySearchList singleColumnName(String tableName,String fieldName){
        if(WsStringUtils.isBlank(tableName)){
            this.singleColumnName(fieldName);
        }else {
            this.singleColumnName(tableName + '.'+fieldName);
        }
        return this;
    }

    public <T> MySearchList singleColumnName(String tableName,SFunction<T,?> fieldName){
        if(WsStringUtils.isBlank(tableName)){
            this.singleColumnName(WsFieldUtils.getFieldName(fieldName));
        }else {
            this.singleColumnName(tableName + '.'+WsFieldUtils.getFieldName(fieldName));
        }
        return this;
    }

    public <T> MySearchList singleColumnName(SFunction<T,?> fieldName){
        this.singleColumnName(WsFieldUtils.getFieldName(fieldName));
        return this;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public MySearchList condition(boolean condition,Consumer<MySearchList> consumer){
        if(condition){
            consumer.accept(this);
        }
        return this;
    }
}

