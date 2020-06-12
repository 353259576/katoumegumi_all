package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.SupplierFunc;
import cn.katoumegumi.java.common.WsFieldUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.function.Supplier;

/**
 * 查询条件构造器
 * @author 王松
 */
public class MySearchList {
    private List<MySearch> mySearches = new ArrayList<>();

    private final List<MySearch> orderSearches = new ArrayList<>();

    private final List<MySearchList> ands = new ArrayList<>();

    private final List<MySearchList> ors = new ArrayList<>();

    private final Map<Class, String> tableAndNickNameMap = new HashMap<>();

    private final List<TableRelation> joins = new ArrayList<>();

    private Class mainClass;

    private JoinType defaultJoinType = JoinType.INNER;

    private Page pageVO;

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

    public static MySearchList create(){
        return new MySearchList();
    }

    public static <T> MySearchList create(Class<T> tClass){
        return new MySearchList().setMainClass(tClass);
    }


    public MySearchList add(MySearch mySearch) {
        mySearches.add(mySearch);
        return this;
    }

    public MySearchList add(String fieldName, SqlOperator operator, Object value) {
        if (operator.equals(SqlOperator.SORT)) {
            orderSearches.add(new MySearch(fieldName, operator, value));
            return this;
        }

        if (value instanceof SupplierFunc) {
            value = WsFieldUtils.getFieldName((SupplierFunc<?>) value);
        }

        if (mainClass != null) {
            String tableName = tableAndNickNameMap.get(mainClass);
            mySearches.add(new MySearch(mainClass, tableName, fieldName, operator, value));
        } else {
            mySearches.add(new MySearch(fieldName, operator, value));
        }

        return this;
    }

    public <T> MySearchList add(SupplierFunc<T> supplierFunc, SqlOperator operator, Object value) {
        String name = WsFieldUtils.getFieldName(supplierFunc);
        return add(name, operator, value);
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

    public Page getPageVO() {
        return pageVO;
    }

    public MySearchList setPageVO(Page pageVO) {
        this.pageVO = pageVO;
        return this;
    }

    public Map createMybaitsMap() {
        Map map = new HashMap();
        for (MySearch m1 : mySearches) {
            map.put(m1.getFieldName(), m1.getValue());
        }
        if (pageVO != null) {
            map.put("page", pageVO);
        }
        return map;
    }


    public Map createMybaitsMapNoPage() {
        Map map = new HashMap();
        for (MySearch m1 : mySearches) {
            map.put(m1.getFieldName(), m1.getValue());
        }
        return map;
    }


    public MySearch get(String value) {
        for (MySearch m : mySearches) {
            if (m.getFieldName().equals(value)) {
                return m;
            }
        }
        return null;
    }

    public Class getMainClass() {
        return mainClass;
    }

    public MySearchList setMainClass(Class mainClass) {
        if (!tableAndNickNameMap.containsKey(mainClass)) {
            tableAndNickNameMap.put(mainClass, mainClass.getName());
        }
        this.mainClass = mainClass;
        return this;
    }

    /**
     * 等于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList eq(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.EQ, value);
    }

    /**
     * 模糊查询
     * @param supplierFunc
     * @param value 需要自己传入%
     * @param <T>
     * @return
     */
    public <T> MySearchList like(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.LIKE, value);
    }

    /**
     * 大于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList gt(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.GT, value);
    }

    /**
     * 大于等于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList gte(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.GTE, value);
    }

    /**
     * 小于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList lt(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.LT, value);
    }

    /**
     * 小于等于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList lte(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.LTE, value);
    }

    /**
     * in
     * @param supplierFunc
     * @param value 必须为list类型
     * @param <T>
     * @return
     */
    public <T> MySearchList in(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.IN, value);
    }

    /**
     * not in
     * @param supplierFunc
     * @param value 必须为list类型
     * @param <T>
     * @return
     */
    public <T> MySearchList nin(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.NIN, value);
    }

    /**
     * 字段为空
     * @param supplierFunc
     * @param value 不传
     * @param <T>
     * @return
     */
    public <T> MySearchList notNull(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.NOTNULL, value);
    }
    public <T> MySearchList notNull(SupplierFunc<T> supplierFunc) {
        return add(supplierFunc, SqlOperator.NOTNULL, null);
    }

    /**
     * 字段不为空
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList isNull(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.NULL, value);
    }

    public <T> MySearchList isNull(SupplierFunc<T> supplierFunc) {
        return add(supplierFunc, SqlOperator.NULL, null);
    }

    /**
     * 不等于
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList ne(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.NE, value);
    }

    /**
     * sql语句 sql语句表名用{}包围
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList sql(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.SQL, value);
    }

    /**
     * 排序
     * @param supplierFunc
     * @param value desc 或 asc
     * @param <T>
     * @return
     */
    public <T> MySearchList sort(SupplierFunc<T> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.SORT, value);
    }

    public <T> MySearchList eqp(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.EQP, WsFieldUtils.getFieldName(value));
    }

    public <T> MySearchList nep(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.NEP, WsFieldUtils.getFieldName(value));
    }

    public <T> MySearchList gtp(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.GTP, WsFieldUtils.getFieldName(value));
    }

    public <T> MySearchList ltp(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.LTP, WsFieldUtils.getFieldName(value));
    }

    public <T> MySearchList gtep(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.GTEP, WsFieldUtils.getFieldName(value));
    }

    public <T> MySearchList ltep(SupplierFunc<?> supplierFunc, SupplierFunc<?> value) {
        return add(supplierFunc, SqlOperator.LTEP, WsFieldUtils.getFieldName(value));
    }

    /**
     * 修改使用 set
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList set(SupplierFunc<?> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.SET, value);
    }

    /**
     * 增加
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList add(SupplierFunc<?> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.ADD, value);
    }

    /**
     * 减
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList subtract(SupplierFunc<?> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.SUBTRACT, value);
    }

    /**
     * 乘
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList multiply(SupplierFunc<?> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.MULTIPLY, value);
    }

    /**
     * 除
     * @param supplierFunc
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList divide(SupplierFunc<?> supplierFunc, Object value) {
        return add(supplierFunc, SqlOperator.DIVIDE, value);
    }

    /**
     * 等于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList eq(String column, Object value) {
        return add(column, SqlOperator.EQ, value);
    }

    /**
     * 模糊查询
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList like(String column, Object value) {
        return add(column, SqlOperator.LIKE, value);
    }

    /**
     * 大于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList gt(String column, Object value) {
        return add(column, SqlOperator.GT, value);
    }

    /**
     * 大于等于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList gte(String column, Object value) {
        return add(column, SqlOperator.GTE, value);
    }

    /**
     * 小于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList lt(String column, Object value) {
        return add(column, SqlOperator.LT, value);
    }

    /**
     * 小于等于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList lte(String column, Object value) {
        return add(column, SqlOperator.LTE, value);
    }

    /**
     * in
     * @param column
     * @param value 需要参数为list
     * @param <T>
     * @return
     */
    public <T> MySearchList in(String column, Object value) {
        return add(column, SqlOperator.IN, value);
    }

    /**
     * not in
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList nin(String column, Object value) {
        return add(column, SqlOperator.NIN, value);
    }

    /**
     * 不为空
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList notNull(String column, Object value) {
        return add(column, SqlOperator.NOTNULL, value);
    }

    public <T> MySearchList notNull(String column) {
        return add(column, SqlOperator.NOTNULL, null);
    }

    public <T> MySearchList isNull(String column, Object value) {
        return add(column, SqlOperator.NULL, value);
    }

    /**
     * 为空
     * @param column
     * @param <T>
     * @return
     */
    public <T> MySearchList isNull(String column) {
        return add(column, SqlOperator.NULL, null);
    }

    /**
     * 不等于
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList ne(String column, Object value) {
        return add(column, SqlOperator.NE, value);
    }

    /**
     * 嵌入sql语句
     * @param column
     * @param value
     * @param <T>
     * @return
     */
    public <T> MySearchList sql(String column, Object value) {
        return add(column, SqlOperator.SQL, value);
    }

    public <T> MySearchList sort(String column, Object value) {
        return add(column, SqlOperator.SORT, value);
    }

    public <T> MySearchList eqp(String column, String value) {
        return add(column, SqlOperator.EQP, value);
    }

    public <T> MySearchList nep(String column, String value) {
        return add(column, SqlOperator.NEP, value);
    }

    public <T> MySearchList gtp(String column, String value) {
        return add(column, SqlOperator.GTP, value);
    }

    public <T> MySearchList ltp(String column, String value) {
        return add(column, SqlOperator.LTP, value);
    }

    public <T> MySearchList gtep(String column, String value) {
        return add(column, SqlOperator.GTEP, value);
    }

    public <T> MySearchList ltep(String column, String value) {
        return add(column, SqlOperator.LTEP, value);
    }

    public <T> MySearchList set(String column, Object value) {
        return add(column, SqlOperator.SET, value);
    }

    public <T> MySearchList add(String column, Object value) {
        return add(column, SqlOperator.ADD, value);
    }

    public <T> MySearchList subtract(String column, Object value) {
        return add(column, SqlOperator.SUBTRACT, value);
    }

    public <T> MySearchList multiply(String column, Object value) {
        return add(column, SqlOperator.MULTIPLY, value);
    }

    public <T> MySearchList divide(String column, Object value) {
        return add(column, SqlOperator.DIVIDE, value);
    }

    public MySearchList and(MySearchList... mySearchLists) {
        ands.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public MySearchList and(Supplier<MySearchList> supplier){
        ands.add(supplier.get());
        return this;
    }

    public MySearchList or(MySearchList... mySearchLists) {
        ors.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public MySearchList or(Supplier<MySearchList> supplier){
        ors.add(supplier.get());
        return this;
    }

    /**
     * 连接其他表
     * @param tableNickName 已存在的表名
     * @param joinTableClass 连接对象类型
     * @param joinTableNickName 连接对象表名
     * @param tableColumn 已存在表的字段
     * @param joinColumn 连接表的字段
     * @param joinType 连接类型
     * @param <T>
     * @return
     */
    public <T> MySearchList join(String tableNickName, Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn,JoinType joinType) {
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
        return join(tableNickName,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.INNER);
    }
    public <T> MySearchList join(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn);
    }

    public <T> MySearchList leftJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.LEFT);
    }
    public <T> MySearchList leftJoin(String tableNickName,Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.LEFT);
    }
    public <T> MySearchList innerJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.INNER);
    }
    public <T> MySearchList innerJoin(String tableNickName,Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.INNER);
    }
    public <T> MySearchList rightJoin(Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.RIGHT);
    }
    public <T> MySearchList rightJoin(String tableNickName,Class<?> joinTableClass, String joinTableNickName, String tableColumn, String joinColumn) {
        return join(null,joinTableClass,joinTableNickName,tableColumn,joinColumn,JoinType.RIGHT);
    }




    public Map<Class, String> getTableAndNickNameMap() {
        return tableAndNickNameMap;
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

}

