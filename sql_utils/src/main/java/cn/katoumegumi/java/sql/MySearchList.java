package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.sql.common.OrderByTypeEnums;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.model.component.SqlEquation;
import cn.katoumegumi.java.sql.model.component.SqlLimit;
import cn.katoumegumi.java.sql.model.query.QueryColumn;
import cn.katoumegumi.java.sql.model.query.QueryElement;
import cn.katoumegumi.java.sql.model.query.QuerySqlString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 查询条件构造器
 *
 * @author ws
 */
public class MySearchList {

    private final List<QueryElement> columnNameList = new ArrayList<>();
    private final List<MySearch> orderSearches = new ArrayList<>();
    private final List<MySearchList> ands = new ArrayList<>();
    private final List<MySearchList> ors = new ArrayList<>();
    private final List<TableRelation> joins = new ArrayList<>();
    private boolean isSingleColumn = false;
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


//    /**
//     * 拼装列名
//     *
//     * @param tableName
//     * @param columnName
//     * @return
//     */
//    private static String Column.name(String tableName, SFunction<?, ?> columnName) {
//        return Column.name(tableName, WsReflectUtils.getFieldName(columnName));
//    }
//
//    private static String Column.name(String tableName, String columnName) {
//        if (WsStringUtils.isBlank(tableName)) {
//            return columnName;
//        } else {
//            return tableName + '.' + columnName;
//        }
//    }

    /**
     * 判断值是不是正确
     *
     * @param column
     * @param operator
     * @param value
     */
    private static void checkValue(QueryElement column, SqlOperator operator, Object value) {
        switch (operator) {
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
                if (value == null) {
                    throw new NullPointerException(column + "的参数不能为空");
                }
                if (!WsBeanUtils.isBaseType(value.getClass())) {
                    throw new IllegalArgumentException(column + "的参数必须为基本类型，当前的类型是：" + value.getClass());
                }
                break;
            case SET:
                if (value != null && !WsBeanUtils.isBaseType(value.getClass())) {
                    throw new IllegalArgumentException(column + "的参数必须为基本类型，当前的类型是：" + value.getClass());
                }
                break;
            case ORDER_BY:
                if (value == null) {
                    throw new NullPointerException("排序方式不能为空");
                }
                if (value instanceof String) {
                    String type = ((String) value).toLowerCase();
                    OrderByTypeEnums.getByType(type);
                } else if (!(value instanceof OrderByTypeEnums)) {
                    throw new IllegalArgumentException("排序方式错误:" + value);
                }
                break;
            case EQP:
            case LTP:
            case GTP:
            case GTEP:
            case LTEP:
            case NEP:
                if (!(value instanceof QueryColumn)) {
                    throw new IllegalArgumentException(column + "的参数必须为字符串类型，当前的类型是：" + value.getClass());
                }
                break;
            case IN:
            case NIN:
                if (!(WsBeanUtils.isArray(value.getClass()) || value instanceof MySearchList)) {
                    throw new IllegalArgumentException(column + "类型不支持，当前的类型是：" + value.getClass());
                }
                if (WsCollectionUtils.isEmpty(value)) {
                    throw new NullPointerException("数组为空");
                }
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                if (!WsBeanUtils.isArray(value.getClass())) {
                    throw new IllegalArgumentException(column + "的参数必须是数组类型，当前的类型是：" + value.getClass());
                }
                if (WsCollectionUtils.isEmpty(value)) {
                    throw new NullPointerException("数组为空");
                }
                break;
            case NULL:
            case NOTNULL:
            case SQL:
            case EXISTS:
            case NOT_EXISTS:
            case OR:
            case AND:
                break;
            default:
                throw new IllegalArgumentException("不支持的查询方式");
        }
    }

    public MySearchList add(MySearch mySearch) {
        mySearches.add(mySearch);
        return this;
    }

    public MySearchList add(QueryElement column, SqlOperator operator, Object value) {
        checkValue(column, operator, value);
        if (operator.equals(SqlOperator.ORDER_BY)) {
            orderSearches.add(new MySearch(column, operator, value instanceof String ? OrderByTypeEnums.getByType(((String) value).toUpperCase()) : value));
            return this;
        }
        mySearches.add(new MySearch(column, operator, value));
        return this;
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

//    public MySearch remove(String name) {
//        MySearch mySearch;
//        for (int i = 0; i < mySearches.size(); i++) {
//            mySearch = mySearches.get(i);
//            if (mySearch.getColumn().equals(name)) {
//                mySearches.remove(i);
//                return mySearch;
//            }
//        }
//        return null;
//    }
//
//    public MySearch get(String value) {
//        for (MySearch m : mySearches) {
//            if (m.getColumn().equals(value)) {
//                return m;
//            }
//        }
//        return null;
//    }
//
//    public MySearch get(String value, SqlOperator sqlOperator) {
//        for (MySearch m : mySearches) {
//            if (m.getColumn().equals(value) && m.getOperator().equals(sqlOperator)) {
//                return m;
//            }
//        }
//        return null;
//    }

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
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList eq(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQ, value);
    }

    public MySearchList eq(String columnFieldName, Object value) {
        return eq(null, columnFieldName, value);
    }

    public <T> MySearchList eq(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQ, value);
    }

    public <T> MySearchList eq(SFunction<T, ?> columnFieldName, Object value) {
        return eq(null, columnFieldName, value);
    }

    /**
     * 不等于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList ne(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NE, value);
    }

    public MySearchList ne(String columnFieldName, Object value) {
        return ne(null, columnFieldName, value);
    }

    public <T> MySearchList ne(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NE, value);
    }

    public <T> MySearchList ne(SFunction<T, ?> columnFieldName, Object value) {
        return ne(null, columnFieldName, value);
    }

    /**
     * 大于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList gt(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GT, value);
    }

    public MySearchList gt(String columnFieldName, Object value) {
        return gt(null, columnFieldName, value);
    }

    public <T> MySearchList gt(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GT, value);
    }

    public <T> MySearchList gt(SFunction<T, ?> columnFieldName, Object value) {
        return gt(null, columnFieldName, value);
    }

    /**
     * 大于等于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList gte(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTE, value);
    }

    public MySearchList gte(String columnFieldName, Object value) {
        return gte(null, columnFieldName, value);
    }

    public <T> MySearchList gte(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTE, value);
    }

    public <T> MySearchList gte(SFunction<T, ?> columnFieldName, Object value) {
        return gte(null, columnFieldName, value);
    }

    /**
     * 小于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList lt(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LT, value);
    }

    public MySearchList lt(String columnFieldName, Object value) {
        return lt(null, columnFieldName, value);
    }

    public <T> MySearchList lt(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LT, value);
    }

    public <T> MySearchList lt(SFunction<T, ?> columnFieldName, Object value) {
        return lt(null, columnFieldName, value);
    }

    /**
     * 小于等于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList lte(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTE, value);
    }

    public MySearchList lte(String columnFieldName, Object value) {
        return lte(null, columnFieldName, value);
    }

    public <T> MySearchList lte(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTE, value);
    }

    public <T> MySearchList lte(SFunction<T, ?> columnFieldName, Object value) {
        return lte(null, columnFieldName, value);
    }

    /**
     * in
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList in(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.IN, value);
    }

    public MySearchList in(String columnFieldName, Object value) {
        return in(null, columnFieldName, value);
    }

    public <T> MySearchList in(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.IN, value);
    }

    public <T> MySearchList in(SFunction<T, ?> columnFieldName, Object value) {
        return in(null, columnFieldName, value);
    }

    /**
     * not in
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList nIn(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NIN, value);
    }

    public MySearchList nIn(String columnFieldName, Object value) {
        return nIn(null, columnFieldName, value);
    }

    public <T> MySearchList nIn(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NIN, value);
    }

    public <T> MySearchList nIn(SFunction<T, ?> columnFieldName, Object value) {
        return nIn(null, columnFieldName, value);
    }

    /**
     * 模糊查询
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList like(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LIKE, value);
    }

    public MySearchList like(String columnFieldName, Object value) {
        return like(null, columnFieldName, value);
    }

    public <T> MySearchList like(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LIKE, value);
    }

    public <T> MySearchList like(SFunction<T, ?> columnFieldName, Object value) {
        return like(null, columnFieldName, value);
    }

    /**
     * 为空
     *
     * @param tableName
     * @param columnFieldName
     * @return
     */
    public MySearchList isNull(String tableName, String columnFieldName) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NULL, null);
    }

    public MySearchList isNull(String columnFieldName) {
        return isNull(null, columnFieldName);
    }

    public <T> MySearchList isNull(String tableName, SFunction<T, ?> columnFieldName) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NULL, null);
    }

    public <T> MySearchList isNull(SFunction<T, ?> columnFieldName) {
        return isNull(null, columnFieldName);
    }

    /**
     * 不为空
     *
     * @param tableName
     * @param columnFieldName
     * @return
     */
    public MySearchList isNotNull(String tableName, String columnFieldName) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NOTNULL, null);
    }

    public MySearchList isNotNull(String columnFieldName) {
        return isNotNull(null, columnFieldName);
    }

    public <T> MySearchList isNotNull(String tableName, SFunction<T, ?> columnFieldName) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NOTNULL, null);
    }

    public <T> MySearchList isNotNull(SFunction<T, ?> columnFieldName) {
        return isNotNull(null, columnFieldName);
    }

    /**
     * 排序
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList sort(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.ORDER_BY, value);
    }

    public MySearchList sort(String columnFieldName, Object value) {
        String[] pathAndName = WsStringUtils.splitArray(columnFieldName,'.');
        boolean isColumn = pathAndName.length <= 1;
        if (isColumn){
            for (String s : pathAndName) {
                if (!s.matches("[a-zA-Z]")) {
                    isColumn = false;
                    break;
                }
            }
        }
        if (isColumn){
            return add(QueryColumn.of(pathAndName.length == 1 ?null:pathAndName[0],pathAndName[1]),SqlOperator.ORDER_BY,value);
        }else {
            return add(QuerySqlString.of(columnFieldName),SqlOperator.ORDER_BY,value);
        }
    }

    public <T> MySearchList sort(String tableName, SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.ORDER_BY, value);
    }

    public <T> MySearchList sort(SFunction<T, ?> columnFieldName, Object value) {
        return sort(null, columnFieldName, value);
    }

    /**
     * between
     *
     * @param tableName
     * @param columnFieldName
     * @param value1
     * @param value2
     * @return
     */
    public MySearchList between(String tableName, String columnFieldName, Object value1, Object value2) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.BETWEEN, Arrays.asList(value1, value2));
    }

    public MySearchList between(String columnFieldName, Object value1, Object value2) {
        return between(null, columnFieldName, value1, value2);
    }

    public <T> MySearchList between(String tableName, SFunction<T, ?> columnFieldName, Object value1, Object value2) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.BETWEEN, Arrays.asList(value1, value2));
    }

    public <T> MySearchList between(SFunction<T, ?> columnFieldName, Object value1, Object value2) {
        return between(null, columnFieldName, value1, value2);
    }

    /**
     * not between
     *
     * @param tableName
     * @param columnFieldName
     * @param value1
     * @param value2
     * @return
     */
    public MySearchList notBetween(String tableName, String columnFieldName, Object value1, Object value2) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NOT_BETWEEN, Arrays.asList(value1, value2));
    }

    public MySearchList notBetween(String columnFieldName, Object value1, Object value2) {
        return notBetween(null, columnFieldName, value1, value2);
    }

    public <T> MySearchList notBetween(String tableName, SFunction<T, ?> columnFieldName, Object value1, Object value2) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.NOT_BETWEEN, Arrays.asList(value1, value2));
    }

    public <T> MySearchList notBetween(SFunction<T, ?> columnFieldName, Object value1, Object value2) {
        return notBetween(null, columnFieldName, value1, value2);
    }

    /**
     * 插入sql语句
     *
     * @param sql
     * @param value
     * @return
     */
    public MySearchList sql(String sql, Object value) {
        return add(QuerySqlString.of(sql), SqlOperator.SQL, value);
    }

    /**
     * exists
     *
     * @param sql
     * @param value
     * @return
     */
    public MySearchList exists(String sql, Object value) {
        return add(QuerySqlString.of(sql), SqlOperator.EXISTS, value);
    }

    public MySearchList exists(MySearchList mySearchList) {
        return add(null, SqlOperator.EXISTS, mySearchList);
    }

    /**
     * not exists
     *
     * @param sql
     * @param value
     * @return
     */
    public MySearchList notExists(String sql, Object value) {
        return add(QuerySqlString.of(sql), SqlOperator.NOT_EXISTS, value);
    }

    public MySearchList notExists(MySearchList mySearchList) {
        return add(null, SqlOperator.NOT_EXISTS, mySearchList);
    }

    /**
     * 等于
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList eqp(String tableName, String columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList eqp(String tableName, String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQP, QueryColumn.of(valueTableName, value));
    }

    public <T, J> MySearchList eqp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, SFunction<J, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList eqp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.EQP, QueryColumn.of(valueTableName, value));
    }

    public MySearchList eqp(String columnFieldName, String value) {
        return eqp(null, columnFieldName, null, value);
    }

    public <T> MySearchList eqp(String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return eqp(null, columnFieldName, valueTableName, value);
    }

    public <T> MySearchList eqp(String columnFieldName, SFunction<T, ?> value) {
        return eqp(null, columnFieldName, null, value);
    }

    public <T> MySearchList eqp(SFunction<T, ?> columnFieldName, SFunction<T, ?> value) {
        return eqp(null, columnFieldName, null, value);
    }

    public <T> MySearchList eqp(String tableName, SFunction<T, ?> columnFieldName, String value) {
        return eqp(tableName, columnFieldName, null, value);
    }

    /**
     * 大于
     *
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList gtp(String tableName, String columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList gtp(String tableName, String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTP, QueryColumn.of(valueTableName, value));
    }

    public <T, K> MySearchList gtp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, SFunction<K, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList gtp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTP, QueryColumn.of(valueTableName, value));
    }

    public MySearchList gtp(String columnFieldName, String value) {
        return gtp(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtp(String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return gtp(null, columnFieldName, valueTableName, value);
    }

    public <T> MySearchList gtp(String columnFieldName, SFunction<T, ?> value) {
        return gtp(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtp(SFunction<T, ?> columnFieldName, SFunction<T, ?> value) {
        return gtp(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtp(String tableName, SFunction<T, ?> columnFieldName, String value) {
        return gtp(tableName, columnFieldName, null, value);
    }

    /**
     * 大于等于
     *
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList gtep(String tableName, String columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTEP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList gtep(String tableName, String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTEP, QueryColumn.of(valueTableName, value));
    }

    public <T, K> MySearchList gtep(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, SFunction<K, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTEP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList gtep(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.GTEP, QueryColumn.of(valueTableName, value));
    }

    public MySearchList gtep(String columnFieldName, String value) {
        return gtep(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtep(String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return gtep(null, valueTableName, columnFieldName, value);
    }

    public <T> MySearchList gtep(String columnFieldName, SFunction<T, ?> value) {
        return gtep(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtep(SFunction<T, ?> columnFieldName, SFunction<T, ?> value) {
        return gtep(null, columnFieldName, null, value);
    }

    public <T> MySearchList gtep(String tableName, SFunction<T, ?> columnFieldName, String value) {
        return gtep(tableName, columnFieldName, null, value);
    }

    /**
     * 小于
     *
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList ltp(String tableName, String columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList ltp(String tableName, String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTP, QueryColumn.of(valueTableName, value));
    }

    public <T, K> MySearchList ltp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, SFunction<K, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList ltp(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTP, QueryColumn.of(valueTableName, value));
    }

    public MySearchList ltp(String columnFieldName, String value) {
        return ltp(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltp(String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return ltp(null, columnFieldName, valueTableName, value);
    }

    public <T> MySearchList ltp(String columnFieldName, SFunction<T, ?> value) {
        return ltp(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltp(SFunction<T, ?> columnFieldName, SFunction<T, ?> value) {
        return ltp(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltp(String tableName, SFunction<T, ?> columnFieldName, String value) {
        return ltp(tableName, columnFieldName, null, value);
    }

    /**
     * 小于等于
     *
     * @param tableName
     * @param columnFieldName
     * @param valueTableName
     * @param value
     * @return
     */
    public MySearchList ltep(String tableName, String columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTEP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList ltep(String tableName, String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTEP, QueryColumn.of(valueTableName, value));
    }

    public <T, K> MySearchList ltep(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, SFunction<K, ?> value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTEP, QueryColumn.of(valueTableName, value));
    }

    public <T> MySearchList ltep(String tableName, SFunction<T, ?> columnFieldName, String valueTableName, String value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.LTEP, QueryColumn.of(valueTableName, value));
    }

    public MySearchList ltep(String columnFieldName, String value) {
        return ltep(null, columnFieldName, null, value);
    }


    //update

    public <T> MySearchList ltep(String columnFieldName, String valueTableName, SFunction<T, ?> value) {
        return ltep(null, columnFieldName, valueTableName, value);
    }

    public <T> MySearchList ltep(String columnFieldName, SFunction<T, ?> value) {
        return ltep(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltep(SFunction<T, ?> columnFieldName, SFunction<T, ?> value) {
        return ltep(null, columnFieldName, null, value);
    }

    public <T> MySearchList ltep(String tableName, SFunction<T, ?> columnFieldName, String value) {
        return ltep(tableName, columnFieldName, null, value);
    }

    /**
     * set
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList set(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.SET, value);
    }

    public MySearchList set(String columnFieldName, Object value) {
        return set(null, columnFieldName, value);
    }

    public <T> MySearchList set(SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(null, columnFieldName), SqlOperator.SET, value);
    }

    /**
     * 加
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList add(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.ADD, value);
    }

    public MySearchList add(String columnFieldName, Object value) {
        return add(null, columnFieldName, value);
    }

    public <T> MySearchList add(SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(null, columnFieldName), SqlOperator.ADD, value);
    }

    /**
     * 减
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList subtract(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.SUBTRACT, value);
    }

    public MySearchList subtract(String columnFieldName, Object value) {
        return subtract(null, columnFieldName, value);
    }

    public <T> MySearchList subtract(SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(null, columnFieldName), SqlOperator.SUBTRACT, value);
    }

    /**
     * 乘
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList multiply(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.MULTIPLY, value);
    }

    public MySearchList multiply(String columnFieldName, Object value) {
        return multiply(null, columnFieldName, value);
    }

    public <T> MySearchList multiply(SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(null, columnFieldName), SqlOperator.MULTIPLY, value);
    }

    /**
     * 除
     *
     * @param tableName
     * @param columnFieldName
     * @param value
     * @return
     */
    public MySearchList divide(String tableName, String columnFieldName, Object value) {
        return add(QueryColumn.of(tableName, columnFieldName), SqlOperator.DIVIDE, value);
    }

    public MySearchList divide(String columnFieldName, Object value) {
        return divide(null, columnFieldName, value);
    }

    public <T> MySearchList divide(SFunction<T, ?> columnFieldName, Object value) {
        return add(QueryColumn.of(null, columnFieldName), SqlOperator.DIVIDE, value);
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
    public final MySearchList and(Consumer<MySearchList>... consumers) {
        for (Consumer<MySearchList> consumer : consumers) {
            MySearchList mySearchList = MySearchList.create();
            consumer.accept(mySearchList);
            if (WsCollectionUtils.isNotEmpty(mySearchList.getAll()) || WsCollectionUtils.isNotEmpty(mySearchList.getAnds()) || WsCollectionUtils.isNotEmpty(mySearchList.getOrs())) {
                ands.add(mySearchList);
            }
        }

        return this;
    }

    @SafeVarargs
    public final MySearchList or(Consumer<MySearchList>... consumers) {
        for (Consumer<MySearchList> consumer : consumers) {
            MySearchList mySearchList = MySearchList.create();
            consumer.accept(mySearchList);
            if (WsCollectionUtils.isNotEmpty(mySearchList.getAll()) || WsCollectionUtils.isNotEmpty(mySearchList.getAnds()) || WsCollectionUtils.isNotEmpty(mySearchList.getOrs())) {
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
        tableRelation.setJoinEntityClass(joinTableClass);
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
     *
     * @param tClass
     * @return
     */
    public TableRelation innerJoin(Class<?> tClass) {
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(TableJoinType.INNER_JOIN);
        tableRelation.setJoinEntityClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

    /**
     * 左联
     *
     * @param tClass
     * @return
     */
    public TableRelation leftJoin(Class<?> tClass) {
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(TableJoinType.LEFT_JOIN);
        tableRelation.setJoinEntityClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

    /**
     * 右连
     *
     * @param tClass
     * @return
     */
    public TableRelation rightJoin(Class<?> tClass) {
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinType(TableJoinType.RIGHT_JOIN);
        tableRelation.setJoinEntityClass(tClass);
        tableRelation.setJoinTableNickName(tClass.getSimpleName());
        return tableRelation;
    }

    /**
     * 表连接
     *
     * @param consumer
     * @return
     */
    public TableRelation join(Class<?> tClass, Consumer<TableRelation> consumer) {
        TableRelation tableRelation = new TableRelation(this);
        tableRelation.setJoinEntityClass(tClass);
        consumer.accept(tableRelation);
        if (WsStringUtils.isBlank(tableRelation.getJoinTableNickName())) {
            tableRelation.setJoinTableNickName(tableRelation.getJoinEntityClass().getSimpleName());
        }
        joins.add(tableRelation);
        return tableRelation;
    }


    /**
     * 内联
     *
     * @param tClass
     * @return
     */
    public MySearchList innerJoin(Class<?> tClass, Consumer<TableRelation> consumer) {
        TableRelation tableRelation = join(tClass, consumer);
        tableRelation.setJoinType(TableJoinType.INNER_JOIN);
        return this;
    }

    /**
     * 左联
     *
     * @param tClass
     * @return
     */
    public MySearchList leftJoin(Class<?> tClass, Consumer<TableRelation> consumer) {
        TableRelation tableRelation = join(tClass, consumer);
        tableRelation.setJoinType(TableJoinType.LEFT_JOIN);
        return this;
    }

    /**
     * 右连
     *
     * @param tClass
     * @return
     */
    public MySearchList rightJoin(Class<?> tClass, Consumer<TableRelation> consumer) {
        TableRelation tableRelation = join(tClass, consumer);
        tableRelation.setJoinType(TableJoinType.RIGHT_JOIN);
        return this;
    }

    /**
     * 获取主表别名
     *
     * @return
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 设置主表别名
     *
     * @param alias
     * @return
     */
    public MySearchList setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * 设置查询的字段的名称
     *
     * @param columnName
     * @return
     */
    public MySearchList singleColumnName(String columnName) {
        this.columnNameList.add(QueryColumn.of(columnName));
        this.isSingleColumn = true;
        return this;
    }
    public MySearchList singleColumnName(String path,String columnName) {
        if (WsStringUtils.isBlank(path)){
            return singleColumnName(columnName);
        }
        this.columnNameList.add(QueryColumn.of(path,columnName));
        this.isSingleColumn = true;
        return this;
    }

    /*public MySearchList singleColumnName(String tableName, String fieldName) {
        if (WsStringUtils.isBlank(tableName)) {
            this.singleColumnName(fieldName);
        } else {
            this.singleColumnName(tableName + '.' + fieldName);
        }
        return this;
    }*/

    public <T> MySearchList singleColumnName(String tableName, SFunction<T, ?> fieldName) {
        if (WsStringUtils.isBlank(tableName)) {
            this.singleColumnName(WsReflectUtils.getFieldName(fieldName));
        } else {
            this.singleColumnName(tableName, WsReflectUtils.getFieldName(fieldName));
        }
        return this;
    }

    public <T> MySearchList singleColumnName(SFunction<T, ?> fieldName) {
        this.singleColumnName(WsReflectUtils.getFieldName(fieldName));
        return this;
    }

    public List<QueryElement> getColumnNameList() {
        return columnNameList;
    }

    public MySearchList condition(boolean condition, Consumer<MySearchList> consumer) {
        if (condition) {
            consumer.accept(this);
        }
        return this;
    }


    public MySearchList sqlEquation(Consumer<SqlEquation> consumer) {
        SqlEquation sqlEquation = new SqlEquation();
        consumer.accept(sqlEquation);
        mySearches.add(new MySearch(null, SqlOperator.EQUATION, sqlEquation));
        return this;
    }

    /**
     * 过滤update search
     *
     * @return
     */
    public List<MySearch> filterUpdateSearch() {
        List<MySearch> updateSearchList = new ArrayList<>();
        Iterator<MySearch> iterator = this.getAll().iterator();
        while (iterator.hasNext()) {
            MySearch search = iterator.next();
            switch (search.getOperator()) {
                case ADD:
                case SUBTRACT:
                case DIVIDE:
                case MULTIPLY:
                case SET:
                    updateSearchList.add(search);
                    iterator.remove();
                    break;
                default:
                    break;
            }
        }
        return updateSearchList;
    }

    public boolean isSingleColumn() {
        return isSingleColumn;
    }
}

