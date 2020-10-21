package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.ReturnEntity;
import cn.katoumegumi.java.sql.entity.ReturnEntityId;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ws
 */
public class SQLModelUtils {

    private static final Logger log = LoggerFactory.getLogger(SQLModelUtils.class);
    /**
     * 插入参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> insertSqlInterceptorMap = new HashMap<>();
    /**
     * 修改参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> updateSqlInterceptorMap = new HashMap<>();
    /**
     * 查询参数注入
     */
    private static final Map<String, AbstractSqlInterceptor> selectSqlInterceptorMap = new HashMap<>();


    /**
     * 记录where所需要的值
     */
    private final List<Object> baseWhereValueList = new ArrayList<>();
    /**
     * 表查询条件
     */
    private final MySearchList mySearchList;

    /**
     * 已经使用过的表关联关系
     */
    private final Set<TableRelation> usedTableRelation = new HashSet<>();


    /**
     * 简写数据
     */
    private final Map<String, String> abbreviationMap = new HashMap<>();

    /**
     * 详细数据
     */
    private final Map<String, String> particularMap = new HashMap<>();

    /**
     * 缩写防重复
     */
    private final AtomicInteger abbreviationNum = new AtomicInteger();
    /**
     * 本地对象与表的对应关系
     */
    private final Map<String, FieldColumnRelationMapper> localMapperMap = new HashMap<>();

    /**
     * 主表的class类型
     */
    private Class<?> mainClass;
    /**
     * 基本查询语句
     */
    private String searchSql;


    public SQLModelUtils(MySearchList mySearchList) {
        this.mySearchList = mySearchList;
        mainClass = mySearchList.getMainClass();
        if (WsStringUtils.isEmpty(mySearchList.getAlias())) {
            getAbbreviation(mainClass.getSimpleName());
        } else {
            setAbbreviation(mainClass.getSimpleName(), mySearchList.getAlias());
        }
        String prefix = mainClass.getSimpleName();
        String tableName = null;
        String joinTableName = null;
        for (TableRelation relation : mySearchList.getJoins()) {
            tableName = relation.getTableNickName();
            if (WsStringUtils.isNotBlank(tableName)) {
                tableName = translateToTableName(tableName);
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
            if (WsStringUtils.isNotBlank(relation.getAlias())) {
                joinTableName = translateToTableName(relation.getJoinTableNickName());
                joinTableName = getNoPrefixTableName(joinTableName);
                relation.setJoinTableNickName(joinTableName);
                joinTableName = prefix + "." + joinTableName;
                setAbbreviation(joinTableName, relation.getAlias());
            }
            tableName = null;
            joinTableName = null;

        }
    }


    public static FieldColumnRelationMapper analysisClassRelation(Class<?> mainClass){
        return FieldColumnRelationMapperFactory.analysisClassRelation(mainClass);
    }



    /**
     * 预防数据库关键词
     *
     * @param keyword
     * @return
     */
    private static String guardKeyword(String keyword) {
        return '`' + keyword + '`';
    }

    /**
     * 对象转换成表查询条件
     *
     * @param o
     * @return
     */
    public static MySearchList ObjectToMySearchList(Object o) {
        FieldColumnRelationMapper mapper = analysisClassRelation(o.getClass());
        List<FieldColumnRelation> ids = mapper.getIdSet();
        List<FieldColumnRelation> columns = mapper.getFieldColumnRelations();
        MySearchList mySearchList = MySearchList.create(o.getClass());
        if (WsListUtils.isNotEmpty(ids)) {
            for (FieldColumnRelation relation : ids) {
                if (WsBeanUtils.isBaseType(relation.getFieldClass())) {
                    try {
                        Object value = relation.getField().get(o);
                        if (value != null) {
                            mySearchList.eq(relation.getFieldName(), value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (WsListUtils.isNotEmpty(columns)) {
            for (FieldColumnRelation relation : columns) {
                if (WsBeanUtils.isBaseType(relation.getFieldClass())) {
                    try {
                        Object value = relation.getField().get(o);
                        if (value != null) {
                            mySearchList.eq(relation.getFieldName(), value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
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
            insertSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isUpdate()) {
            updateSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
        if (sqlInterceptor.isSelect()) {
            selectSqlInterceptorMap.put(sqlInterceptor.fieldName(), sqlInterceptor);
        }
    }

    /**
     * 查询
     *
     * @return
     */
    public SelectSqlEntity select() {
        SelectSqlEntity selectSqlEntity = new SelectSqlEntity();
        String selectSql = searchListBaseSQLProcessor();
        String countSql = searchListBaseCountSQLProcessor();
        selectSqlEntity.setSelectSql(selectSql);
        selectSqlEntity.setCountSql(countSql);
        selectSqlEntity.setValueList(baseWhereValueList);
        return selectSqlEntity;
    }

    /**
     * 生成sql语句
     *
     * @return
     */
    private String searchListBaseSQLProcessor() {
        StringBuilder selectSql = new StringBuilder();
        FieldColumnRelationMapper fieldColumnRelationMapper;
        if (WsStringUtils.isBlank(searchSql)) {
            mainClass = mySearchList.getMainClass();
            selectSql.append(modelToSqlSelect(mySearchList.getMainClass()));
            List<TableRelation> list = mySearchList.getJoins();
            fieldColumnRelationMapper = analysisClassRelation(mySearchList.getMainClass());
            String baseTableName = fieldColumnRelationMapper.getNickName();
            for (TableRelation tableRelation : list) {
                if (usedTableRelation.contains(tableRelation)) {
                    usedTableRelation.remove(tableRelation);
                    continue;
                }
                String tableNickName;
                FieldColumnRelationMapper mapper = analysisClassRelation(tableRelation.getJoinTableClass());
                String joinTableNickName = baseTableName + "." + tableRelation.getJoinTableNickName();
                localMapperMap.put(joinTableNickName, mapper);
                if (WsStringUtils.isBlank(tableRelation.getTableNickName())) {
                    tableNickName = baseTableName;
                } else {
                    tableNickName = baseTableName + "." + tableRelation.getTableNickName();
                }
                FieldColumnRelationMapper baseMapper = localMapperMap.get(tableNickName);

                if (WsListUtils.isNotEmpty(selectSqlInterceptorMap)) {
                    for (FieldColumnRelation fieldColumnRelation : mapper.getFieldColumnRelations()) {
                        AbstractSqlInterceptor sqlInterceptor = selectSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                        if (sqlInterceptor != null) {
                            if (sqlInterceptor.useCondition(baseMapper)) {
                                Object o = sqlInterceptor.selectFill();
                                if (o != null) {
                                    mySearchList.eq(tableRelation.getJoinTableNickName() + "." + sqlInterceptor.fieldName(), o);
                                }
                            }
                            break;
                        }
                    }
                }

                String joinType;
                if (tableRelation.getJoinType() != null) {
                    switch (tableRelation.getJoinType()) {
                        case LEFT:
                            joinType = " LEFT JOIN ";
                            break;
                        case INNER:
                            joinType = " INNER JOIN ";
                            break;
                        case RIGHT:
                            joinType = " RIGHT JOIN ";
                            break;
                        default:
                            joinType = " INNER JOIN ";
                            break;
                    }
                } else {
                    joinType = " INNER JOIN ";
                }
                if (WsStringUtils.isNotBlank(tableRelation.getTableNickName())) {
                    FieldColumnRelationMapper tableMapper = null;
                    if (tableRelation.getTableNickName().startsWith(baseTableName)) {
                        tableMapper = localMapperMap.get(tableRelation.getTableNickName());
                    } else {
                        tableMapper = localMapperMap.get(baseTableName + "." + tableRelation.getTableNickName());
                    }
                    FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
                    String tableName;
                    if (mainMapper.equals(tableMapper)) {
                        tableName = mainMapper.getNickName();
                    } else {
                        if (tableRelation.getTableNickName().startsWith(mainMapper.getNickName())) {
                            tableName = tableRelation.getTableNickName();
                        } else {
                            tableName = mainMapper.getNickName() + "." + tableRelation.getTableNickName();
                        }
                    }
                    selectSql.append(createJoinSql(tableName, tableMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), joinType));

                    if (tableRelation.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(tableRelation.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            selectSql.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }

                } else {
                    selectSql.append(createJoinSql(tableNickName, baseMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), joinType));

                    if (tableRelation.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(tableRelation.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            selectSql.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }

                }


            }
            if (!(mySearchList.getAll().isEmpty() && mySearchList.getAnds().isEmpty() && mySearchList.getOrs().isEmpty())) {
                selectSql.append(" where ");
                List<String> whereStrings = searchListWhereSqlProcessor(mySearchList, baseTableName);
                selectSql.append(WsStringUtils.jointListString(whereStrings, " and "));
            }

            //缓存sql查询语句
            searchSql = selectSql.toString();
        } else {
            fieldColumnRelationMapper = analysisClassRelation(mySearchList.getMainClass());
            selectSql.append(searchSql);
        }


        List<MySearch> orderSearches = mySearchList.getOrderSearches();
        List<String> list1 = new ArrayList<>();
        for (MySearch mySearch : orderSearches) {
            list1.add(createWhereColumn(fieldColumnRelationMapper.getNickName(), mySearch));
        }
        if (list1.size() > 0) {
            selectSql.append(" order by ")
                    .append(WsStringUtils.jointListString(list1, ","));
        }
        if (mySearchList.getSqlLimit() != null) {
            return mysqlPaging(mySearchList.getSqlLimit(), selectSql.toString());
        }

        return selectSql.toString();

    }

    private String searchListBaseCountSQLProcessor() {
        if (searchSql == null) {
            searchListBaseSQLProcessor();
        }
        return "select count(*) from (" + searchSql + " ) as searchCount";
    }

    private String mysqlPaging(SqlLimit limit, String selectSql) {
        return selectSql + " limit " + limit.getOffset() + "," + limit.getSize();
    }

    /**
     * 生成whereSql语句
     *
     * @param prefix
     * @param mySearch
     * @return
     */
    private String createWhereColumn(String prefix, MySearch mySearch) {
        switch (mySearch.getOperator()) {
            case SET:
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
                return null;
            default:
                break;
        }

        StringBuilder tableColumn = null;
        ColumnBaseEntity columnBaseEntity = null;
        FieldColumnRelation fieldColumnRelation = null;
        String value = null;
        if (mySearch.getOperator().equals(SqlOperator.SQL) || mySearch.getOperator().equals(SqlOperator.EXISTS)) {
            tableColumn = new StringBuilder();
        } else {
            if (mySearch.getOperator().equals(SqlOperator.SORT) && mySearch.getFieldName().endsWith(")")) {
                tableColumn = new StringBuilder();
                tableColumn.append(mySearch.getFieldName());
            } else {
                tableColumn = new StringBuilder();
                columnBaseEntity = getColumnBaseEntity(mySearch.getFieldName(), prefix);
                tableColumn.append(guardKeyword(columnBaseEntity.getAlias()));
                tableColumn.append(".");
                tableColumn.append(guardKeyword(columnBaseEntity.getColumnName()));
                fieldColumnRelation = columnBaseEntity.getFieldColumnRelation();
            }

        }


        switch (mySearch.getOperator()) {
            case EQ:
                tableColumn.append(" = ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LIKE:
                tableColumn.append(" like ");
                assert fieldColumnRelation != null;
                String fuzzyWord = WsBeanUtils.objectToT(mySearch.getValue(), String.class);
                assert fuzzyWord != null;
                int start = 0;
                int end = fuzzyWord.length();
                if (fuzzyWord.charAt(0) == '%') {
                    start = 1;
                }
                if (fuzzyWord.charAt(fuzzyWord.length() - 1) == '%') {
                    end = end - 1;
                }
                tableColumn.append(" concat('%',?,'%') ");
                baseWhereValueList.add(fuzzyWord.substring(start, end));
                break;
            case GT:
                tableColumn.append(" > ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LT:
                tableColumn.append(" < ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case GTE:
                tableColumn.append(" >= ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LTE:
                tableColumn.append(" <= ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case NIN: tableColumn.append(" not");
            case IN:
                if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                    Collection<?> collection = (Collection<?>) mySearch.getValue();
                    Iterator<?> iterator = collection.iterator();
                    List<String> symbols = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Object o = iterator.next();
                        symbols.add("?");
                        assert fieldColumnRelation != null;
                        baseWhereValueList.add(WsBeanUtils.objectToT(o, fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                    tableColumn.append(')');

                } else if (mySearch.getValue().getClass().isArray()) {
                    Object[] os = (Object[]) mySearch.getValue();
                    List<String> symbols = new ArrayList<>();
                    for (Object o : os) {
                        symbols.add("?");
                        assert fieldColumnRelation != null;
                        baseWhereValueList.add(WsBeanUtils.objectToT(o, fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                    tableColumn.append(')');
                } else {
                    throw new RuntimeException(columnBaseEntity.getFieldName()+"参数非数组类型");
                }

                break;
            case NULL:
                tableColumn.append(" is null");
                break;
            case NOTNULL:
                tableColumn.append(" is not null");
                break;
            case NE:
                tableColumn.append(" != ?");
                assert fieldColumnRelation != null;
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case SORT:
                tableColumn.append(' ');
                tableColumn.append(mySearch.getValue());
                break;
            case SQL:

                tableColumn.append(translateTableNickName(prefix, mySearch.getFieldName()));
                if (mySearch.getValue() != null) {
                    if (mySearch.getValue() instanceof Collection) {
                        Collection<?> collection = (Collection<?>) mySearch.getValue();
                        baseWhereValueList.addAll(collection);
                    } else if (mySearch.getValue().getClass().isArray()) {
                        Object[] os = (Object[]) mySearch.getValue();
                        baseWhereValueList.addAll(Arrays.asList(os));
                    } else {
                        baseWhereValueList.add(mySearch.getValue());
                    }
                }

                //tableColumn.append(mySearch.getValue());
                break;
            case NOT_EXISTS:tableColumn.append(" not");
            case EXISTS:
                tableColumn.append(" exists (");
                tableColumn.append(translateTableNickName(prefix, mySearch.getFieldName()));
                tableColumn.append(") ");
                if (mySearch.getValue() != null) {
                    if (mySearch.getValue() instanceof Collection) {
                        Collection<?> collection = (Collection<?>) mySearch.getValue();
                        baseWhereValueList.addAll(collection);
                    } else if (mySearch.getValue().getClass().isArray()) {
                        Object[] os = (Object[]) mySearch.getValue();
                        baseWhereValueList.addAll(Arrays.asList(os));
                    } else {
                        baseWhereValueList.add(mySearch.getValue());
                    }
                }

                //tableColumn.append(mySearch.getValue());
                break;
            case EQP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" = ").append(value);
                break;
            case NEP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" != ").append(value);
                break;
            case GTP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" > ").append(value);
                break;
            case LTP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" < ").append(value);
                break;
            case GTEP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" >= ").append(value);
                break;
            case LTEP:
                columnBaseEntity = getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
                value = guardKeyword(columnBaseEntity.getAlias()) + "." + guardKeyword(columnBaseEntity.getColumnName());
                tableColumn.append(" <= ").append(value);
                break;
            case NOT_BETWEEN: tableColumn.append(" not");
            case BETWEEN:
                assert columnBaseEntity != null;
                if(WsBeanUtils.isArray(mySearch.getValue().getClass())) {
                    tableColumn.append(" between ");
                    if (mySearch.getValue().getClass().isArray()) {
                        Object[] objects = (Object[]) mySearch.getValue();
                        if (objects.length != 2) {

                            throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                        }
                        tableColumn
                                .append(WsBeanUtils.objectToT(objects[0], columnBaseEntity.getField().getType()))
                                .append(" AND ")
                                .append(WsBeanUtils.objectToT(objects[1], columnBaseEntity.getField().getType()));
                        baseWhereValueList.add(objects[0]);
                        baseWhereValueList.add(objects[1]);
                    } else {
                        Collection<?> collection = (Collection<?>) mySearch.getValue();
                        if (collection.size() != 2) {
                            throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                        }
                        Iterator<?> iterator = collection.iterator();
                        tableColumn
                                .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()))
                                .append(" AND ")
                                .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()));
                        baseWhereValueList.addAll(collection);
                    }
                }
                break;
            default:
                break;
        }
        return tableColumn.toString();
    }

    private List<String> searchListWhereSqlProcessor(MySearchList mySearchList, String prefix) {
        Iterator<MySearch> iterator = mySearchList.iterator();
        List<String> stringList = new ArrayList<>();
        while (iterator.hasNext()) {
            MySearch mySearch = iterator.next();
            String whereSqlPart = createWhereColumn(prefix, mySearch);
            if (WsStringUtils.isNotBlank(whereSqlPart)) {
                stringList.add(whereSqlPart);
            }

        }

        List<MySearchList> ands = mySearchList.getAnds();
        if (!WsListUtils.isEmpty(ands)) {
            List<String> andStrList = new ArrayList<>();
            for (MySearchList searchList : ands) {
                List<String> andStrings = searchListWhereSqlProcessor(searchList, prefix);
                int andStringsSize = andStrings.size();
                if (andStringsSize != 0) {
                    if (andStringsSize == 1) {
                        andStrList.add(WsStringUtils.jointListString(andStrings, " and "));
                    } else {
                        andStrList.add("(" + WsStringUtils.jointListString(andStrings, " and ") + ")");
                    }
                }
            }
            if (WsListUtils.isNotEmpty(andStrList)) {
                if (andStrList.size() == 1) {
                    stringList.add(WsStringUtils.jointListString(andStrList, " and "));
                } else {
                    stringList.add("(" + WsStringUtils.jointListString(andStrList, " and ") + ")");
                }

            }
        }
        List<MySearchList> ors = mySearchList.getOrs();
        if (!WsListUtils.isEmpty(ors)) {
            List<String> orStrList = new ArrayList<>();
            for (MySearchList searchList : ors) {
                List<String> orStrings = searchListWhereSqlProcessor(searchList, prefix);
                int orStringsSize = orStrings.size();
                if (orStringsSize != 0) {
                    if (orStringsSize == 1) {
                        orStrList.add(WsStringUtils.jointListString(orStrings, " and "));
                    } else {
                        orStrList.add("(" + WsStringUtils.jointListString(orStrings, " and ") + ")");
                    }
                }

            }
            if (WsListUtils.isNotEmpty(orStrList)) {
                if (orStrList.size() == 1) {
                    stringList.add(WsStringUtils.jointListString(orStrList, " or "));
                } else {
                    stringList.add("(" + WsStringUtils.jointListString(orStrList, " or ") + ")");
                }

            }
        }
        return stringList;
    }

    /**
     * 创建字段语句
     *
     * @param nickName   昵称
     * @param columnName 数据库字段名
     * @param fieldName  属性名
     * @return
     */
    private String createOneSelectColumn(String nickName, String columnName, String fieldName) {
        String sNickName = getAbbreviation(nickName);
        //String columnNickName = getAbbreviation(createColumnNickName(nickName,fieldName));

        String sColumnNickName = sNickName + '.' + fieldName;
        String columnNickName = nickName + '.' + fieldName;
        abbreviationMap.put(columnNickName, sColumnNickName);
        particularMap.put(sColumnNickName, columnNickName);

        return createColumnName(sNickName, columnName) + " " + guardKeyword(sNickName + '.' + fieldName);
    }

    /**
     * 创建表连接语句
     *
     * @param tableNickName     主表别名
     * @param tableColumn       主表连接数据库字段
     * @param joinTableName     连接表名
     * @param joinTableNickName 连接表别名
     * @param joinColumn        连接表数据库字段
     * @param joinType          连接类型
     * @return
     */
    private String createJoinSql(String tableNickName, String tableColumn, String joinTableName, String joinTableNickName, String joinColumn, String joinType) {

        String sJoinTableNickName = getAbbreviation(joinTableNickName);
        String sTableNickName = getAbbreviation(tableNickName);
        return ' ' + joinType
                + guardKeyword(joinTableName) +
                ' ' +
                guardKeyword(sJoinTableNickName) +
                " on " +
                guardKeyword(sTableNickName) +
                '.' +
                guardKeyword(tableColumn) +
                " = " +
                guardKeyword(sJoinTableNickName) +
                '.' +
                guardKeyword(joinColumn);
    }

    //创建查询语句
    private String modelToSqlSelect(Class<?> clazz) {

        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(clazz);
        if (WsListUtils.isNotEmpty(selectSqlInterceptorMap)) {
            for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationMapper.getFieldColumnRelations()) {
                AbstractSqlInterceptor sqlInterceptor = selectSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                if (sqlInterceptor != null) {
                    if (sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                        Object o = sqlInterceptor.selectFill();
                        if (o != null) {
                            mySearchList.eq(sqlInterceptor.fieldName(), o);
                        }
                    }
                    break;

                }
            }
        }
        String tableName = fieldColumnRelationMapper.getTableName();
        String tableNickName = fieldColumnRelationMapper.getNickName();
        localMapperMap.put(tableNickName, fieldColumnRelationMapper);
        List<String> list = new ArrayList<>();
        List<String> joinString = new ArrayList<>();
        selectJoin(tableNickName, list, joinString, fieldColumnRelationMapper);
        String baseSql = "select " + String.join( ",",list) + " from `" + tableName + "` `" + getAbbreviation(fieldColumnRelationMapper.getNickName()) + "` " + String.join(" ",joinString);
        fieldColumnRelationMapper.setBaseSql(baseSql);
        return fieldColumnRelationMapper.getBaseSql();
    }

    /**
     * 拼接查询
     */
    private void selectJoin(String tableNickName, List<String> selectString, List<String> joinString, FieldColumnRelationMapper fieldColumnRelationMapper) {

        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationMapper.getIdSet()) {
            selectString.add(createOneSelectColumn(tableNickName, fieldColumnRelation.getColumnName(), fieldColumnRelation.getFieldName()));
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationMapper.getFieldColumnRelations()) {
            selectString.add(createOneSelectColumn(tableNickName, fieldColumnRelation.getColumnName(), fieldColumnRelation.getFieldName()));
        }
        String lastTableNickName;
        if (!fieldColumnRelationMapper.getFieldJoinClasses().isEmpty()) {

            for (FieldJoinClass fieldJoinClass : fieldColumnRelationMapper.getFieldJoinClasses()) {


                FieldJoinClass newFieldJoinClass = selfFieldJoinClass(tableNickName, fieldJoinClass, mySearchList.getJoins());
                if (newFieldJoinClass != null) {
                    fieldJoinClass = newFieldJoinClass;
                }

                if (WsStringUtils.isNotBlank(fieldJoinClass.getJoinColumn())) {
                    if (fieldJoinClass.getNickName().contains(".")) {
                        lastTableNickName = analysisClassRelation(mainClass).getNickName() + '.' + fieldJoinClass.getNickName();
                    } else {
                        lastTableNickName = tableNickName + '.' + fieldJoinClass.getNickName();
                    }

                    FieldColumnRelationMapper mapper = analysisClassRelation(fieldJoinClass.getJoinClass());
                    localMapperMap.put(lastTableNickName, mapper);
                    String joinType;
                    if (fieldJoinClass.getJoinType() != null) {
                        switch (fieldJoinClass.getJoinType()) {
                            case LEFT:
                                joinType = "LEFT JOIN ";
                                break;
                            case INNER:
                                joinType = " INNER JOIN ";
                                break;
                            case RIGHT:
                                joinType = " RIGHT JOIN ";
                                break;
                            default:
                                joinType = " INNER JOIN ";
                                break;
                        }
                    } else {
                        joinType = " INNER JOIN ";
                    }


                    StringBuilder joinStr = new StringBuilder(createJoinSql(tableNickName, fieldJoinClass.getJoinColumn(), mapper.getTableName(), lastTableNickName, fieldJoinClass.getAnotherJoinColumn(), joinType));


                    if (fieldJoinClass.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(fieldJoinClass.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            joinStr.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }
                    joinString.add(joinStr.toString());
                    selectJoin(lastTableNickName, selectString, joinString, mapper);
                }
            }
        }
    }

    /**
     * 判断连接条件是否符合
     *
     * @param fieldJoinClass
     * @return
     */
    private boolean checkFieldJoinClass(FieldJoinClass fieldJoinClass) {
        return !(WsStringUtils.isBlank(fieldJoinClass.getJoinColumn()) || WsStringUtils.isBlank(fieldJoinClass.getAnotherJoinColumn()));
    }

    /**
     * 完善FieldJoinClass
     *
     * @param tableNickName
     * @param fieldJoinClass
     * @param tableRelationList
     */
    private FieldJoinClass selfFieldJoinClass(String tableNickName, FieldJoinClass fieldJoinClass, List<TableRelation> tableRelationList) {
        if (WsListUtils.isEmpty(tableRelationList)) {
            return null;
        }
        int firstIndex = tableNickName.indexOf('.');
        //String prefix = analysisClassRelation(mainClass).getNickName();
        String prefix = null;
        if (firstIndex != -1) {
            prefix = tableNickName.substring(0, firstIndex);
        } else {
            prefix = tableNickName;
        }
        Iterator<TableRelation> iterator = tableRelationList.iterator();
        TableRelation tableRelation = null;
        while (iterator.hasNext()) {
            tableRelation = iterator.next();
            if (usedTableRelation.contains(tableRelation)) {
                continue;
            }
            if (WsStringUtils.isNotBlank(tableRelation.getTableNickName())) {
                if (firstIndex == -1) {
                    continue;
                }
                if (!tableNickName.equals(prefix + '.' + tableRelation.getTableNickName())) {
                    continue;
                }
            } else {
                if (firstIndex != -1) {
                    continue;
                }
            }

            if (!fieldJoinClass.getJoinClass().equals(tableRelation.getJoinTableClass())) {
                continue;
            }
            if (!(tableNickName + "." + fieldJoinClass.getNickName()).equals(prefix + '.' + tableRelation.getJoinTableNickName())) {
                continue;
            }

            FieldJoinClass oldFieldJoinClass = fieldJoinClass;
            fieldJoinClass = new FieldJoinClass();
            fieldJoinClass.setArray(oldFieldJoinClass.isArray());
            fieldJoinClass.setNickName(oldFieldJoinClass.getNickName());
            fieldJoinClass.setField(oldFieldJoinClass.getField());
            fieldJoinClass.setJoinClass(oldFieldJoinClass.getJoinClass());


            FieldColumnRelationMapper mainMapper = localMapperMap.get(tableNickName);
            fieldJoinClass.setJoinColumn(mainMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName());

            FieldColumnRelationMapper mapper = analysisClassRelation(fieldJoinClass.getJoinClass());
            fieldJoinClass.setAnotherJoinColumn(mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName());
            fieldJoinClass.setNickName(tableRelation.getJoinTableNickName());
            fieldJoinClass.setJoinType(tableRelation.getJoinType());
            fieldJoinClass.setBaseTableNickName(tableRelation.getTableNickName());
            fieldJoinClass.setConditionSearchList(tableRelation.getConditionSearchList());

            //iterator.remove();
            usedTableRelation.add(tableRelation);

            if (WsListUtils.isNotEmpty(selectSqlInterceptorMap)) {
                for (FieldColumnRelation fieldColumnRelation : mapper.getFieldColumnRelations()) {
                    AbstractSqlInterceptor sqlInterceptor = selectSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                    if (sqlInterceptor != null) {
                        if (sqlInterceptor.useCondition(mapper)) {
                            Object o = sqlInterceptor.selectFill();
                            if (o != null) {
                                mySearchList.eq(fieldJoinClass.getNickName() + "." + sqlInterceptor.fieldName(), o);
                            }
                        }
                        break;

                    }
                }
            }


            return fieldJoinClass;


        }
        return null;
    }


    /**
     * 单个添加
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> InsertSqlEntity insertSql(T t) {
        InsertSqlEntity entity = new InsertSqlEntity();
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mainClass);
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<FieldColumnRelation> validList = new ArrayList<>();
        List valueList = new ArrayList();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();

        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIdSet();

        for (FieldColumnRelation fieldColumnRelation : idList) {
            Field field = fieldColumnRelation.getField();
            try {
                Object o = field.get(t);
                if (o != null) {
                    columnNameList.add(fieldColumnRelation.getColumnName());
                    placeholderList.add("?");
                    validList.add(fieldColumnRelation);
                    valueList.add(o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            Field field = fieldColumnRelation.getField();
            AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(analysisClassRelation(mainClass))) {
                o = sqlInterceptor.insertFill();
            } else {
                try {
                    o = field.get(t);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
            if (o != null) {
                columnNameList.add(fieldColumnRelation.getColumnName());
                placeholderList.add("?");
                validList.add(fieldColumnRelation);
                valueList.add(o);
            }
        }

        String insertSql = "insert into " + fieldColumnRelationMapper.getTableName() + "(`" + WsStringUtils.jointListString(columnNameList, "`,`") + "`) value(" + WsStringUtils.jointListString(placeholderList, ",") + ")";
        entity.setInsertSql(insertSql);
        entity.setUsedField(validList);
        entity.setIdList(fieldColumnRelationMapper.getIdSet());
        entity.setValueList(valueList);
        return entity;
    }

    /**
     * 批量添加
     *
     * @param tList
     * @param <T>
     * @return
     */
    public <T> InsertSqlEntity insertSqlBatch(List<T> tList) {
        if (tList == null) {
            throw new RuntimeException("添加不能为空");
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(tList.get(0).getClass());
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<FieldColumnRelation> validField = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();
        List valueList = new ArrayList();


        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIdSet();
        for (FieldColumnRelation fieldColumnRelation : idList) {
            Field field = fieldColumnRelation.getField();
            try {
                Object o = field.get(tList.get(0));
                if (o != null) {
                    validField.add(fieldColumnRelation);
                    columnNameList.add(fieldColumnRelation.getColumnName());
                    placeholderList.add("?");
                    valueList.add(o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            Field field = fieldColumnRelation.getField();

            AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.insertFill();
            } else {
                try {
                    o = field.get(tList.get(0));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
            if (o != null) {
                validField.add(fieldColumnRelation);
                columnNameList.add(fieldColumnRelation.getColumnName());
                placeholderList.add("?");
                valueList.add(o);
            }
        }
        String placeholderSql = "(" + WsStringUtils.jointListString(placeholderList, ",") + ")";
        placeholderList = new ArrayList<>();
        placeholderList.add(placeholderSql);
        int size = tList.size();
        for (int i = 1; i < size; i++) {
            for (FieldColumnRelation fieldColumnRelation : validField) {
                Field field = fieldColumnRelation.getField();
                AbstractSqlInterceptor sqlInterceptor = insertSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                Object o = null;
                if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                    o = sqlInterceptor.insertFill();
                } else {
                    try {
                        o = field.get(tList.get(i));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
                valueList.add(o);
            }
            placeholderList.add(placeholderSql);
        }
        InsertSqlEntity insertSqlEntity = new InsertSqlEntity();
        String insertSql = "insert into " + fieldColumnRelationMapper.getTableName() + "(`" + WsStringUtils.jointListString(columnNameList, "`,`") + "`) values" + WsStringUtils.jointListString(placeholderList, ",");
        insertSqlEntity.setInsertSql(insertSql);
        insertSqlEntity.setUsedField(validField);
        insertSqlEntity.setIdList(fieldColumnRelationMapper.getIdSet());
        insertSqlEntity.setValueList(valueList);
        return insertSqlEntity;
    }


    /**
     * 修改通过对象
     *
     * @param t
     * @param <T>
     * @return
     */
    public <T> UpdateSqlEntity update(T t, boolean isAll) {
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(t.getClass());
        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIdSet();
        List<FieldColumnRelation> columnList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<String> columnStrList = new ArrayList<>();
        List<String> idStrList = new ArrayList<>();
        List valueList = new ArrayList();

        List<FieldColumnRelation> validColumnList = new ArrayList<>();
        List<FieldColumnRelation> validIdList = new ArrayList<>();

        for (FieldColumnRelation fieldColumnRelation : columnList) {

            AbstractSqlInterceptor sqlInterceptor = updateSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            Object o = null;
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                o = sqlInterceptor.updateFill();
            } else {
                try {
                    o = fieldColumnRelation.getField().get(t);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (isAll || o != null) {
                String str = guardKeyword(fieldColumnRelation.getColumnName()) + " = ? ";
                columnStrList.add(str);
                valueList.add(o);
                validColumnList.add(fieldColumnRelation);
            }
        }
        for (FieldColumnRelation fieldColumnRelation : idList) {
            try {
                Object o = fieldColumnRelation.getField().get(t);
                if (o != null) {
                    String str = guardKeyword(fieldColumnRelation.getColumnName()) + " = ? ";
                    idStrList.add(str);
                    valueList.add(o);
                    validIdList.add(fieldColumnRelation);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (idStrList.size() == 0) {
            throw new RuntimeException("id不能为空");
        }
        String updateSql = "UPDATE " + fieldColumnRelationMapper.getTableName() + " SET " + WsStringUtils.jointListString(columnStrList, ",") + " where " + WsStringUtils.jointListString(idStrList, " and ");
        UpdateSqlEntity updateSqlEntity = new UpdateSqlEntity();
        updateSqlEntity.setUpdateSql(updateSql);
        updateSqlEntity.setIdList(validIdList);
        updateSqlEntity.setUsedField(validColumnList);
        updateSqlEntity.setValueList(valueList);
        return updateSqlEntity;
    }

    /**
     * 修改通过MySearchList
     *
     * @param mySearchList
     * @return
     */
    public UpdateSqlEntity update(MySearchList mySearchList) {
        if (mySearchList.getMainClass() == null) {
            mySearchList.setMainClass(mainClass);
        }
        //String searchSql = searchListBaseSQLProcessor();
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mySearchList.getMainClass());
        localMapperMap.put(fieldColumnRelationMapper.getNickName(), fieldColumnRelationMapper);
        List<String> whereStringList = searchListWhereSqlProcessor(mySearchList, fieldColumnRelationMapper.getNickName());
        String searchSql = WsStringUtils.jointListString(whereStringList, " and ");


        List<AbstractSqlInterceptor> interceptorList = new ArrayList<>();
        List<FieldColumnRelation> fieldColumnRelationList = fieldColumnRelationMapper.getFieldColumnRelations();
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationList) {
            AbstractSqlInterceptor sqlInterceptor = updateSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
            if (sqlInterceptor != null && sqlInterceptor.useCondition(fieldColumnRelationMapper)) {
                interceptorList.add(sqlInterceptor);
            }
        }
        for (AbstractSqlInterceptor sqlInterceptor : interceptorList) {
            MySearch mySearch = mySearchList.get(sqlInterceptor.fieldName(), SqlOperator.SET);
            if (mySearch == null) {
                mySearchList.set(sqlInterceptor.fieldName(), sqlInterceptor.updateFill());
            } else {
                mySearch.setValue(sqlInterceptor.updateFill());
            }
        }

        List<String> setList = createUpdateSetSql(mySearchList, fieldColumnRelationMapper.getNickName());
        String updateSql = "UPDATE `"
                + fieldColumnRelationMapper.getTableName()
                + "` `" + getAbbreviation(fieldColumnRelationMapper.getNickName())
                + "` SET "
                + WsStringUtils.jointListString(setList, ",") + " where " + searchSql;
        List valueList = new ArrayList();
        List setValueList = new ArrayList();
        for (MySearch mySearch : mySearchList.getAll()) {
            switch (mySearch.getOperator()) {
                case SET:
                case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                    setValueList.add(mySearch.getValue());
                default:
                    break;
            }
        }
        valueList.addAll(setValueList);
        valueList.addAll(baseWhereValueList);
        UpdateSqlEntity updateSqlEntity = new UpdateSqlEntity();
        updateSqlEntity.setUpdateSql(updateSql);
        updateSqlEntity.setValueList(valueList);
        return updateSqlEntity;
    }

    private List<String> createUpdateSetSql(MySearchList mySearchList, String prefix) {
        List<String> setStrList = new ArrayList<>();
        String str;
        for (MySearch mySearch : mySearchList.getAll()) {
            switch (mySearch.getOperator()) {
                case SET:
                case ADD:
                case DIVIDE:
                case MULTIPLY:
                case SUBTRACT:
                    break;
                default:
                    continue;
            }
            FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mainClass);
            FieldColumnRelation fieldColumnRelation = fieldColumnRelationMapper.getFieldColumnRelationByField(mySearch.getFieldName());
            String columnName = fieldColumnRelation.getColumnName();
            switch (mySearch.getOperator()) {
                case SET:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = ? ";
                    setStrList.add(str);
                    break;
                case ADD:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = IFNULL(" + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + ",0) + ? ";
                    setStrList.add(str);
                    break;
                case SUBTRACT:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = IFNULL(" + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + ",0) - ? ";
                    setStrList.add(str);
                    break;
                case MULTIPLY:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = IFNULL(" + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + ",0) * ? ";
                    setStrList.add(str);
                    break;
                case DIVIDE:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = IFNULL(" + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + ",0) / ? ";
                    setStrList.add(str);
                    break;
                default:
                    break;
            }
        }
        if (setStrList.size() == 0) {
            throw new RuntimeException("修改内容不能为空");
        }
        return setStrList;

    }

    public DeleteSqlEntity delete() {
        String searchSql = searchListBaseSQLProcessor();
        int index = searchSql.indexOf(" where ");
        if (index < 0) {
            throw new RuntimeException("删除不能没有条件");
        }
        searchSql = searchSql.substring(index);
        FieldColumnRelationMapper mapper = analysisClassRelation(mainClass);
        String deleteSql = "DELETE "+getAbbreviation(mapper.getNickName())+" FROM " + guardKeyword(mapper.getTableName()) + " " + guardKeyword(getAbbreviation(mapper.getNickName())) + " " + searchSql;
        DeleteSqlEntity deleteSqlEntity = new DeleteSqlEntity();
        deleteSqlEntity.setDeleteSql(deleteSql);
        deleteSqlEntity.setValueList(baseWhereValueList);
        return deleteSqlEntity;
    }


    public <T> List<T> oneLoopMargeMap(ResultSet resultSet) {
        try {
            int length = 0;
            int classNum = localMapperMap.size();
            ResultSetMetaData resultSetMetaData = null;

            resultSetMetaData = resultSet.getMetaData();
            length = resultSetMetaData.getColumnCount();

            if (length == 0) {
                return new ArrayList<>(0);
            }
            FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
            String baseTableName = mainMapper.getNickName();


            List<List<String>> columnNameListList = new ArrayList<>(length);
            List<FieldColumnRelationMapper> mapperList = new ArrayList<>(length);
            List<FieldColumnRelation> columnRelationList = new ArrayList<>(length);


            String columnName = null;
            for (int i = 0; i < length; i++) {
                columnName = resultSetMetaData.getColumnLabel(i + 1);
                List<String> nameList = WsStringUtils.split(columnName, '.');
                nameList.set(0, getParticular(nameList.get(0)));
                FieldColumnRelationMapper mapper = localMapperMap.get(nameList.get(0));
                FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByField(nameList.get(1));
                columnNameListList.add(nameList);
                mapperList.add(mapper);
                columnRelationList.add(fieldColumnRelation);
            }

            List<ReturnEntity> returnEntityList = new ArrayList<>();
            Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap = new HashMap<>();

            Map<String, ReturnEntity> returnEntityMap;
            while (resultSet.next()) {
                returnEntityMap = new HashMap<>(classNum);
                Object value;
                for (int i = 0; i < length; ++i) {
                    value = resultSet.getObject(i + 1);
                    List<String> nameList = columnNameListList.get(i);
                    FieldColumnRelationMapper mapper = mapperList.get(i);
                    FieldColumnRelation fieldColumnRelation = columnRelationList.get(i);
                    ReturnEntity returnEntity = returnEntityMap.computeIfAbsent(nameList.get(0), columnTypeName -> {
                        ReturnEntity entity = new ReturnEntity();
                        entity.setFieldColumnRelationMapper(mapper);
                        Object[] idList = new Object[mapper.getIdSet().size()];
                        Object[] columnList = new Object[mapper.getFieldColumnRelations().size()];
                        ReturnEntity[] returnEntities = new ReturnEntity[mapper.getFieldJoinClasses().size()];
                        entity.setIdValueList(idList);
                        entity.setColumnValueList(columnList);
                        entity.setJoinEntityList(returnEntities);
                        return entity;
                    });

                    if (fieldColumnRelation.isId()) {
                        returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    } else {
                        returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    }
                }
                ReturnEntity returnEntity = returnEntityMap.get(baseTableName);
                if(returnEntity != null) {
                    ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity,baseTableName);
                    //ReturnEntityUtils.packageReturnEntity(idReturnEntityMap, returnEntityMap, mainEntity, baseTableName);
                    if (returnEntity.equals(mainEntity)) {
                        returnEntityList.add(mainEntity);
                    }
                }
            }
            if (returnEntityList.size() == 0) {
                return new ArrayList<>(0);
            }
            List<T> list = (List<T>) new ArrayList(returnEntityList.size());
            for (ReturnEntity returnEntity : returnEntityList) {
                list.add((T) returnEntity.getValue());
            }

            return list;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new ArrayList<>(0);
        }


    }

    /**
     * 合并生成数据
     *
     * @param mapList
     * @param <T>
     * @return
     */
    public <T> List<T> oneLoopMargeMap(List<Map> mapList) {
        if (WsListUtils.isEmpty(mapList)) {
            return new ArrayList<>(0);
        }

        FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
        String baseTableName = mainMapper.getNickName();

        List<ReturnEntity> returnEntityList = new ArrayList<>(mapList.size());
        List<List<String>> columnNameListList = new ArrayList<>();
        List<FieldColumnRelationMapper> mapperList = new ArrayList<>(mapList.size());
        List<FieldColumnRelation> columnRelationList = new ArrayList<>(mapList.size());
        Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap = new HashMap<>(mapList.size());

        Map firstMap = mapList.get(0);
        Set<Map.Entry> entrySet = firstMap.entrySet();

        for (Map.Entry entry : entrySet) {
            List<String> nameList = WsStringUtils.split((String) entry.getKey(), '.');
            nameList.set(0, getParticular(nameList.get(0)));
            FieldColumnRelationMapper mapper = localMapperMap.get(nameList.get(0));
            FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByField(nameList.get(1));

            columnNameListList.add(nameList);
            mapperList.add(mapper);
            columnRelationList.add(fieldColumnRelation);
        }


        Map<String, ReturnEntity> returnEntityMap;
        for (Map map : mapList) {
            returnEntityMap = new HashMap<>();
            entrySet = map.entrySet();
            int i = 0;
            for (Map.Entry entry : entrySet) {
                if(entry.getValue() == null){
                    ++i;
                    continue;
                }
                List<String> nameList = columnNameListList.get(i);
                FieldColumnRelationMapper mapper = mapperList.get(i);
                FieldColumnRelation fieldColumnRelation = columnRelationList.get(i);
                ReturnEntity returnEntity = returnEntityMap.computeIfAbsent(nameList.get(0), columnTypeName -> {
                    ReturnEntity entity = new ReturnEntity();
                    entity.setFieldColumnRelationMapper(mapper);
                    Object[] idList = new Object[mapper.getIdSet().size()];
                    Object[] columnList = new Object[mapper.getFieldColumnRelations().size()];
                    ReturnEntity[] returnEntities = new ReturnEntity[mapper.getFieldJoinClasses().size()];
                    entity.setIdValueList(idList);
                    entity.setColumnValueList(columnList);
                    entity.setJoinEntityList(returnEntities);
                    return entity;
                });

                if (fieldColumnRelation.isId()) {
                    returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                } else {
                    returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                }
                ++i;
            }
            ReturnEntity returnEntity = returnEntityMap.get(baseTableName);
            if(returnEntity != null) {
                ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity,baseTableName);
                //ReturnEntityUtils.packageReturnEntity(idReturnEntityMap, returnEntityMap, mainEntity, baseTableName);
                if (returnEntity.equals(mainEntity)) {
                    returnEntityList.add(mainEntity);
                }
            }
        }

        List list = new ArrayList(returnEntityList.size());
        for (ReturnEntity returnEntity : returnEntityList) {
            list.add(returnEntity.getValue());
        }

        return list;
    }




    /**
     * 创建table nick name
     *
     * @param strs
     * @return
     */
    private String createTableNickName(String... strs) {
        return "`" + WsStringUtils.jointListString(strs, "`.`") + "`";
    }

    /**
     * 创建table column name
     *
     * @param tableNickName
     * @param columnName
     * @return
     */
    private String createColumnName(String tableNickName, String columnName) {
        return guardKeyword(tableNickName) + '.' + guardKeyword(columnName);
    }

    /**
     * 创建table column nickName
     *
     * @param tableNickName
     * @param fieldName
     * @return
     */
    private String createColumnNickName(String tableNickName, String fieldName) {
        return tableNickName + '.' + fieldName;
    }

    /**
     * 获取简称
     *
     * @param keyword
     * @return
     */
    private String getAbbreviation(String keyword) {
        String value = abbreviationMap.get(keyword);
        if (value == null) {
            value = particularMap.get(keyword);
            if (value == null) {
                value = createAbbreviation(keyword);
                abbreviationMap.put(keyword, value);
                particularMap.put(value, keyword);
                return value;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    /**
     * 设置简称
     *
     * @param keyword
     * @return
     */
    private String setAbbreviation(String keyword, String value) {
        abbreviationMap.put(keyword, value);
        particularMap.put(value, keyword);
        return value;
    }

    /**
     * 创建简称
     *
     * @param keyword
     * @return
     */
    private String createAbbreviation(String keyword) {
        if (keyword.length() < 2) {
            return keyword + '_' + abbreviationNum.getAndAdd(1);
        } else {
            return keyword.substring(0, 1) + '_' + abbreviationNum.getAndAdd(1);
        }
    }

    /**
     * 获取详细名称
     *
     * @param value
     * @return
     */
    private String getParticular(String value) {
        return particularMap.get(value);
    }

    /**
     * 转换sql语句中表名为简写
     *
     * @param searchSql
     * @return
     */
    private String translateTableNickName(String prefix, String searchSql) {
        char[] cs = searchSql.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder replaceSb = new StringBuilder();
        char c;
        boolean isReplace = false;
        String replaceStr = null;
        for (char value : cs) {
            c = value;
            if (isReplace) {
                if (c == '}') {
                    replaceStr = getParticular(replaceSb.toString());
                    if (replaceStr == null) {
                        if (replaceSb.toString().startsWith(prefix)) {
                            stringBuilder.append(getAbbreviation(replaceSb.toString()));
                        } else {
                            stringBuilder.append(getAbbreviation(prefix + "." + replaceSb.toString()));
                        }
                    } else {
                        stringBuilder.append(replaceSb.toString());
                    }
                    isReplace = false;
                } else {
                    replaceSb.append(c);
                }
            } else {
                if (c == '{') {
                    replaceSb = new StringBuilder();
                    isReplace = true;
                } else {
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();

    }


    /**
     * 把简写转换成详细
     *
     * @param prefix
     * @param searchSql
     * @return
     */
    private String translateToTableName(String searchSql) {
        char[] cs = searchSql.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder replaceSb = new StringBuilder();
        char c;
        boolean isReplace = false;
        for (char value : cs) {
            c = value;
            if (isReplace) {
                if (c == '}') {
                    stringBuilder.append(getNoPrefixTableName(getParticular(replaceSb.toString())));

                    isReplace = false;
                } else {
                    replaceSb.append(c);
                }
            } else {
                if (c == '{') {
                    replaceSb = new StringBuilder();
                    isReplace = true;
                } else {
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();

    }

    /**
     * 去除表别名的主表名称
     *
     * @return
     */
    public String getNoPrefixTableName(String tableName) {
        if (WsStringUtils.isBlank(tableName)) {
            return null;
        }
        FieldColumnRelationMapper mapper = analysisClassRelation(mainClass);
        String prefix = mapper.getNickName();
        prefix = prefix + ".";
        if (tableName.startsWith(prefix)) {
            if (tableName.length() == prefix.length()) {
                return null;
            } else {
                tableName = tableName.substring(prefix.length());
            }
            return tableName;
        } else {
            return tableName;
        }
    }

    /**
     * 根据查询条件生成列基本信息
     *
     * @param mySearch
     * @return
     */
    private ColumnBaseEntity getColumnBaseEntity(String originalFieldName, String prefix) {
        ColumnBaseEntity columnBaseEntity = new ColumnBaseEntity();
        String prefixString = null;
        String fieldName;
        FieldColumnRelationMapper mapper;
        FieldColumnRelation fieldColumnRelation = null;
        String key = null;
        List<String> fieldNameList = WsStringUtils.split(originalFieldName, '.');
        int size = fieldNameList.size();
        if (size == 1) {
            mapper = localMapperMap.get(prefix);
            fieldName = fieldNameList.get(0);
            fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
            prefixString = mapper.getNickName();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            for (int i = 0; i < size - 1; i++) {
                sb.append(".");
                key = fieldNameList.get(i);
                key = getParticular(key);
                if (key == null) {
                    key = fieldNameList.get(i);
                } else {
                    key = getNoPrefixTableName(key);
                }
                if (key == null) {
                    sb.deleteCharAt(sb.length() - 1);
                } else {
                    if(i == 0){
                        if(prefix.equals(key)){
                            sb.deleteCharAt(sb.length() - 1);
                            continue;
                        }
                    }
                    sb.append(key);
                }

            }
            prefixString = sb.toString();
            mapper = localMapperMap.get(prefixString);
            if (mapper == null) {
                throw new RuntimeException(prefixString + "不存在");
            }
            fieldName = fieldNameList.get(size - 1);
            fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
        }
        columnBaseEntity.setTableName(mapper.getTableName());
        columnBaseEntity.setTableNickName(prefixString);
        columnBaseEntity.setColumnName(fieldColumnRelation.getColumnName());
        columnBaseEntity.setAlias(getAbbreviation(columnBaseEntity.getTableNickName()));
        columnBaseEntity.setFieldName(fieldColumnRelation.getFieldName());
        columnBaseEntity.setFieldColumnRelation(fieldColumnRelation);
        return columnBaseEntity;
    }


}