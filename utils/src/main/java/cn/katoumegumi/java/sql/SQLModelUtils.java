package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

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
     * 已经使用过的表关联关系
     */
    private final Set<TableRelation> usedTableRelation = new HashSet<>();

    /**
     * 记录where所需要的值
     */
    private final List<Object> baseWhereValueList = new ArrayList<>();

    /**
     * 表面和列名转换
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

    /**
     * 缓存的sqlEntity
     */
    private SqlEntity cacheSqlEntity;

    public SQLModelUtils(MySearchList mySearchList) {
        this(mySearchList, new TranslateNameUtils());
    }

    public SQLModelUtils(MySearchList mySearchList, TranslateNameUtils translateNameUtils) {
        this.mySearchList = mySearchList;
        this.translateNameUtils = translateNameUtils;
        mainClass = mySearchList.getMainClass();
        if (WsStringUtils.isBlank(mySearchList.getAlias())) {
            translateNameUtils.getAbbreviation(mainClass.getSimpleName());
        } else {
            translateNameUtils.setAbbreviation(mainClass.getSimpleName(), mySearchList.getAlias());
        }
        String prefix = mainClass.getSimpleName();
        translateNameUtils.addMainClassName(prefix);
        String tableName;
        String joinTableName;
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
            if (WsStringUtils.isNotBlank(relation.getJoinTableNickName())) {
                joinTableName = translateToTableName(relation.getJoinTableNickName());
                joinTableName = translateNameUtils.getNoPrefixTableName(joinTableName);
                relation.setJoinTableNickName(joinTableName);
                joinTableName = prefix + '.' + joinTableName;
                if (WsStringUtils.isBlank(relation.getAlias())) {
                    relation.setAlias(translateNameUtils.createAbbreviation(joinTableName));
                }
                translateNameUtils.setAbbreviation(joinTableName, relation.getAlias());
            }
            tableName = null;
            joinTableName = null;

        }
    }


    public static FieldColumnRelationMapper analysisClassRelation(Class<?> mainClass) {
        return FieldColumnRelationMapperFactory.analysisClassRelation(mainClass);
    }


    /**
     * 数据库关键词
     *
     * @param keyword
     * @return
     */
    public static String guardKeyword(String keyword) {
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
        FieldColumnRelationMapper mapper = analysisClassRelation(o.getClass());
        List<FieldColumnRelation> ids = mapper.getIds();
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
     * 通常的条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String commonConditionHandle(String condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " " + condition + " ?";
    }

    /**
     * like的条件处理
     *
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String likeConditionHandle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
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
        baseWhereValueList.add(fuzzyWord.substring(start, end));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " like concat('%',?,'%')";
    }

    /**
     * sql条件处理
     *
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String sqlConditionHandle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
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
        return translateNameUtils.translateTableNickName(prefix, mySearch.getFieldName());
    }

    /**
     * exists条件处理
     *
     * @param condition          false not
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String existsConditionHandle(boolean condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        StringBuilder tableColumn = new StringBuilder();
        if (!condition) {
            tableColumn.append(" not");
        }
        tableColumn.append(" exists (");
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(entity.getSelectSql());
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            tableColumn.append(translateNameUtils.translateTableNickName(prefix, mySearch.getFieldName()));
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
        }
        tableColumn.append(") ");
        return tableColumn.toString();
    }

    /**
     * between条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String betweenConditionHandle(boolean condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (!condition) {
            tableColumn.append(" not");
        }
        if (WsBeanUtils.isArray(mySearch.getValue().getClass())) {

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
        return tableColumn.toString();
    }

    /**
     * in条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String inConditionHandle(boolean condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (!condition) {
            tableColumn.append(" not");
        }
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(" in(");
            tableColumn.append(entity.getSelectSql());
            tableColumn.append(")");
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                Iterator<?> iterator = collection.iterator();
                List<String> symbols = new ArrayList<>();
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    symbols.add("?");
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
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
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
                }
                tableColumn.append(" in");
                tableColumn.append('(');
                tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                tableColumn.append(')');
            } else {
                throw new RuntimeException(columnBaseEntity.getFieldName() + "参数非数组类型");
            }
        }
        return tableColumn.toString();
    }

    /**
     * 创建字段语句
     *
     * @param nickName   昵称
     * @param columnName 数据库字段名
     * @param fieldName  属性名
     * @return
     */
    /*private String createOneSelectColumn(String nickName, String columnName, String fieldName) {
        String sNickName = translateNameUtils.getAbbreviation(nickName);
        //String sColumnNickName = sNickName + '.' + fieldName;
        //String columnNickName = nickName + '.' + fieldName;
        //translateNameUtils.setAbbreviation(columnNickName,sColumnNickName);
        return createColumnName(sNickName, columnName) + " " + createColumnNickName(sNickName,fieldName);
    }*/

    /**
     * null条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String nullConditionHandle(boolean condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {

        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        return condition ? SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " is null" :
                SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                        "." +
                        SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                        " is not null";
    }

    /**
     * 排序条件处理
     *
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String sortConditionHandle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        StringBuilder tableColumn = new StringBuilder();
        if (mySearch.getFieldName().endsWith(")")) {
            tableColumn.append(mySearch.getFieldName());
        } else {
            ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
            tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
            tableColumn.append(".");
            tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        }
        tableColumn.append(' ');
        tableColumn.append(mySearch.getValue());
        return tableColumn.toString();
    }

    /**
     * 通用无参数条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     **/
    public static String commonNoValueConditionHandle(String condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " " + condition + " " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    }

    /**
     * 修改条件的条件处理
     *
     * @param condition
     * @param translateNameUtils
     * @param mySearch
     * @param prefix
     * @param baseWhereValueList
     * @return
     */
    public static String commonUpdateConditionHandle(String condition, TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList) {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = IFNULL(" + SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) + ",0) " + condition + " ? ";
    }

    /**
     * 查询
     *
     * @return
     */
    public SelectSqlEntity select() {
        SelectSqlEntity selectSqlEntity = new SelectSqlEntity();
        String selectSql = searchListBaseSQLProcessor();
        selectSqlEntity.setSelectSql(selectSql);
        if (mySearchList.getSqlLimit() != null) {
            String countSql = searchListBaseCountSQLProcessor();
            selectSqlEntity.setCountSql(countSql);
        }
        selectSqlEntity.setValueList(baseWhereValueList);
        return selectSqlEntity;
    }

    /**
     * 生成sql语句
     *
     * @return
     */
    private String searchListBaseSQLProcessor() {
        StringBuilder selectSql;// = new StringBuilder();
        FieldColumnRelationMapper fieldColumnRelationMapper;
        if (cacheSqlEntity == null) {
            SqlEntity sqlEntity = modelToSqlSelect(mySearchList.getMainClass());
            //List<String> joinTableList = sqlEntity.getTableNameList();
            List<TableEntity> joinTableList = sqlEntity.getTableNameList();
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
                translateNameUtils.addLocalMapper(joinTableNickName, mapper);
                if (WsStringUtils.isBlank(tableRelation.getTableNickName())) {
                    tableNickName = baseTableName;
                } else {
                    tableNickName = baseTableName + "." + tableRelation.getTableNickName();
                }
                FieldColumnRelationMapper baseMapper = translateNameUtils.getLocalMapper(tableNickName);

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
                if (WsStringUtils.isNotBlank(tableRelation.getTableNickName())) {
                    FieldColumnRelationMapper tableMapper;
                    if (tableRelation.getTableNickName().startsWith(baseTableName)) {
                        tableMapper = translateNameUtils.getLocalMapper(tableRelation.getTableNickName());
                    } else {
                        tableMapper = translateNameUtils.getLocalMapper(baseTableName + "." + tableRelation.getTableNickName());
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
                    selectSql = new StringBuilder();
                    if (tableRelation.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(tableRelation.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            selectSql.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }
                    joinTableList.add(createJoinSql(tableName, tableMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), tableRelation.getJoinType(), selectSql.toString()));

                } else {
                    selectSql = new StringBuilder();
                    if (tableRelation.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(tableRelation.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            selectSql.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }
                    joinTableList.add(createJoinSql(tableNickName, baseMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), tableRelation.getJoinType(), selectSql.toString()));

                }
            }
            if (!(mySearchList.getAll().isEmpty() && mySearchList.getAnds().isEmpty() && mySearchList.getOrs().isEmpty())) {
                List<String> whereStrings = searchListWhereSqlProcessor(mySearchList, baseTableName);
                sqlEntity.getConditionList().addAll(whereStrings);
            }

            List<MySearch> orderSearches = mySearchList.getOrderSearches();
            List<String> list1 = new ArrayList<>();
            for (MySearch mySearch : orderSearches) {
                list1.add(createWhereColumn(fieldColumnRelationMapper.getNickName(), mySearch));
            }
            selectSql = new StringBuilder();
            if (list1.size() > 0) {
                selectSql.append(" order by ")
                        .append(WsStringUtils.jointListString(list1, ","));
            }
            if (mySearchList.getSqlLimit() != null) {
                sqlEntity.setSubjoin(mysqlPaging(mySearchList.getSqlLimit(), selectSql.toString()));
            } else {
                sqlEntity.setSubjoin(selectSql.toString());
            }

            cacheSqlEntity = sqlEntity;

        }
        return "select " + cacheSqlEntity.getColumnStr() + " " + cacheSqlEntity.getTableStr() + " " + cacheSqlEntity.getCondition() + cacheSqlEntity.getSubjoin();
    }

    private String searchListBaseCountSQLProcessor() {
        if (cacheSqlEntity == null) {
            searchListBaseSQLProcessor();
        }
        return "select count(*) " + cacheSqlEntity.getTableStr() + " " + cacheSqlEntity.getCondition();
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
        if (mySearch.getOperator().equals(SqlOperator.EQUATION)) {
            return sqlEquationHandel((SqlEquation) mySearch.getValue(), prefix);
        }
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
        return mySearch.getOperator().getHandle().handle(translateNameUtils, mySearch, prefix, baseWhereValueList);
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
    private TableEntity createJoinSql(String tableNickName, String tableColumn, String joinTableName, String joinTableNickName, String joinColumn, TableJoinType joinType, String condition) {

        String sJoinTableNickName = translateNameUtils.getAbbreviation(joinTableNickName);
        String sTableNickName = translateNameUtils.getAbbreviation(tableNickName);
        String c = " on " +
                guardKeyword(sTableNickName) +
                '.' +
                guardKeyword(tableColumn) +
                " = " +
                guardKeyword(sJoinTableNickName) +
                '.' +
                guardKeyword(joinColumn) + condition;
        return new TableEntity(joinType, joinTableName, sJoinTableNickName, c);
    }

    //创建查询语句
    private SqlEntity modelToSqlSelect(Class<?> clazz) {
        SqlEntity sqlEntity = new SqlEntity();
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
        translateNameUtils.addLocalMapper(tableNickName, fieldColumnRelationMapper);
        final List<TableEntity> joinString = sqlEntity.getTableNameList();
        joinString.add(new TableEntity(TableJoinType.FROM, tableName, translateNameUtils.getAbbreviation(fieldColumnRelationMapper.getNickName()), null));
        List<ColumnBaseEntity> list = null;
        if (WsListUtils.isEmpty(mySearchList.getColumnNameList())) {
            list = sqlEntity.getColumnList();
        }
        selectJoin(tableNickName, list, joinString, fieldColumnRelationMapper);
        if (list == null) {
            list = sqlEntity.getColumnList();
            for (String columnName : mySearchList.getColumnNameList()) {
                list.add(translateNameUtils.getColumnBaseEntity(columnName, tableNickName));
            }
        }
        return sqlEntity;
    }

    /**
     * 拼接查询
     */
    private void selectJoin(final String tableNickName, final List<ColumnBaseEntity> selectString, final List<TableEntity> joinString, final FieldColumnRelationMapper fieldColumnRelationMapper) {

        ColumnBaseEntity entity = null;
        if (selectString != null) {
            for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationMapper.getIds()) {
                entity = new ColumnBaseEntity(fieldColumnRelation, fieldColumnRelationMapper.getTableName(), tableNickName, translateNameUtils.getAbbreviation(tableNickName));
                selectString.add(entity);
            }
            for (FieldColumnRelation fieldColumnRelation : fieldColumnRelationMapper.getFieldColumnRelations()) {
                entity = new ColumnBaseEntity(fieldColumnRelation, fieldColumnRelationMapper.getTableName(), tableNickName, translateNameUtils.getAbbreviation(tableNickName));
                selectString.add(entity);
            }
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
                    translateNameUtils.addLocalMapper(lastTableNickName, mapper);
                    StringBuilder joinStr = new StringBuilder();
                    if (fieldJoinClass.getConditionSearchList() != null) {
                        List<String> whereStrList = searchListWhereSqlProcessor(fieldJoinClass.getConditionSearchList(), mainClass.getSimpleName());
                        if (WsListUtils.isNotEmpty(whereStrList)) {
                            joinStr.append(" AND ").append(WsStringUtils.jointListString(whereStrList, " AND "));
                        }
                    }
                    joinString.add(createJoinSql(tableNickName, fieldJoinClass.getJoinColumn(), mapper.getTableName(), lastTableNickName, fieldJoinClass.getAnotherJoinColumn(), fieldJoinClass.getJoinType(), joinStr.toString()));
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
            fieldJoinClass = new FieldJoinClass(oldFieldJoinClass.isArray(), oldFieldJoinClass.getJoinClass(), oldFieldJoinClass.getField());
            fieldJoinClass.setNickName(oldFieldJoinClass.getNickName());


            FieldColumnRelationMapper mainMapper = translateNameUtils.getLocalMapper(tableNickName);
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
        List<Object> valueList = new ArrayList<>();
        List<String> columnNameList = new ArrayList<>();
        List<String> placeholderList = new ArrayList<>();

        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();

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
        entity.setIdList(fieldColumnRelationMapper.getIds());
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
        List<Object> valueList = new ArrayList<>();


        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();
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
        insertSqlEntity.setIdList(fieldColumnRelationMapper.getIds());
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
        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIds();
        List<FieldColumnRelation> columnList = fieldColumnRelationMapper.getFieldColumnRelations();
        List<String> columnStrList = new ArrayList<>();
        List<String> idStrList = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();

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

        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mySearchList.getMainClass());
        translateNameUtils.addLocalMapper(fieldColumnRelationMapper.getNickName(), fieldColumnRelationMapper);

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

        List<String> whereStringList = searchListWhereSqlProcessor(mySearchList, fieldColumnRelationMapper.getNickName());
        if (WsListUtils.isEmpty(whereStringList)) {
            throw new RuntimeException("不允许全局修改");
        }
        String searchSql = WsStringUtils.jointListString(whereStringList, " and ");

        String updateSql = "UPDATE `"
                + fieldColumnRelationMapper.getTableName()
                + "` `" + translateNameUtils.getAbbreviation(fieldColumnRelationMapper.getNickName())
                + "` SET "
                + WsStringUtils.jointListString(setList, ",") + " where " + searchSql;
        UpdateSqlEntity updateSqlEntity = new UpdateSqlEntity();
        updateSqlEntity.setUpdateSql(updateSql);
        updateSqlEntity.setValueList(baseWhereValueList);
        return updateSqlEntity;
    }

    private List<String> createUpdateSetSql(MySearchList mySearchList, String prefix) {
        List<String> setStrList = new ArrayList<>();
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
            setStrList.add(mySearch.getOperator().getHandle().handle(translateNameUtils, mySearch, prefix, baseWhereValueList));
        }
        if (WsListUtils.isEmpty(setStrList)) {
            throw new RuntimeException("没有修改内容");
        }
        return setStrList;
    }

    public DeleteSqlEntity delete() {
        searchListBaseSQLProcessor();
        String searchSql = cacheSqlEntity.getCondition();
        //int index = searchSql.indexOf(" where ");
        if (WsStringUtils.isBlank(searchSql)) {
            throw new RuntimeException("删除不能没有条件");
        }
        //searchSql = searchSql.substring(index);
        FieldColumnRelationMapper mapper = analysisClassRelation(mainClass);
        String deleteSql = "DELETE " + translateNameUtils.getAbbreviation(mapper.getNickName()) + " FROM " + guardKeyword(mapper.getTableName()) + " " + guardKeyword(translateNameUtils.getAbbreviation(mapper.getNickName())) + " " + searchSql;
        DeleteSqlEntity deleteSqlEntity = new DeleteSqlEntity();
        deleteSqlEntity.setDeleteSql(deleteSql);
        deleteSqlEntity.setValueList(baseWhereValueList);
        return deleteSqlEntity;
    }

    public <T> List<T> margeMap(ResultSet resultSet) {
        if (WsListUtils.isEmpty(mySearchList.getColumnNameList())) {
            return oneLoopMargeMap(resultSet);
        } else {
            List<Object> tList = new ArrayList<>();
            ColumnBaseEntity columnBaseEntity = cacheSqlEntity.getColumnList().get(0);
            try {
                while (resultSet.next()) {
                    tList.add(WsBeanUtils.objectToT(resultSet.getObject(1), columnBaseEntity.getField().getType()));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return (List<T>) tList;
        }
    }

    public <T> List<T> oneLoopMargeMap(ResultSet resultSet) {
        try {
            int length = 0;
            int classNum = translateNameUtils.locationMapperSize();
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
                nameList.set(0, translateNameUtils.getParticular(nameList.get(0)));
                FieldColumnRelationMapper mapper = translateNameUtils.getLocalMapper(nameList.get(0));
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
                        return new ReturnEntity(mapper);
                    });

                    if (fieldColumnRelation.isId()) {
                        returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    } else {
                        returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = value;
                    }
                }
                ReturnEntity returnEntity = returnEntityMap.get(baseTableName);
                if (returnEntity != null) {
                    ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity, baseTableName);
                    //ReturnEntityUtils.packageReturnEntity(idReturnEntityMap, returnEntityMap, mainEntity, baseTableName);
                    if (returnEntity.equals(mainEntity)) {
                        returnEntityList.add(mainEntity);
                    }
                }
            }
            if (returnEntityList.size() == 0) {
                return new ArrayList<>(0);
            }
            List<Object> list = new ArrayList<>(returnEntityList.size());
            for (ReturnEntity returnEntity : returnEntityList) {
                list.add(returnEntity.getValue());
            }

            return (List<T>) list;


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
    public <T> List<T> oneLoopMargeMap(List<Map<Object, Object>> mapList) {
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

        Map<Object, Object> firstMap = mapList.get(0);
        Set<Map.Entry<Object, Object>> entrySet = firstMap.entrySet();

        for (Map.Entry<Object, Object> entry : entrySet) {
            List<String> nameList = WsStringUtils.split((String) entry.getKey(), '.');
            nameList.set(0, translateNameUtils.getParticular(nameList.get(0)));
            FieldColumnRelationMapper mapper = translateNameUtils.getLocalMapper(nameList.get(0));
            FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByField(nameList.get(1));

            columnNameListList.add(nameList);
            mapperList.add(mapper);
            columnRelationList.add(fieldColumnRelation);
        }


        Map<String, ReturnEntity> returnEntityMap;
        for (Map<Object, Object> map : mapList) {
            returnEntityMap = new HashMap<>();
            entrySet = map.entrySet();
            int i = 0;
            for (Map.Entry<Object, Object> entry : entrySet) {
                if (entry.getValue() == null) {
                    ++i;
                    continue;
                }
                List<String> nameList = columnNameListList.get(i);
                FieldColumnRelationMapper mapper = mapperList.get(i);
                FieldColumnRelation fieldColumnRelation = columnRelationList.get(i);
                ReturnEntity returnEntity = returnEntityMap.computeIfAbsent(nameList.get(0), columnTypeName -> {
                    return new ReturnEntity(mapper);
                });

                if (fieldColumnRelation.isId()) {
                    returnEntity.getIdValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                } else {
                    returnEntity.getColumnValueList()[mapper.getLocation(fieldColumnRelation)] = entry.getValue();
                }
                ++i;
            }
            ReturnEntity returnEntity = returnEntityMap.get(baseTableName);
            if (returnEntity != null) {
                ReturnEntity mainEntity = ReturnEntityUtils.getReturnEntity(idReturnEntityMap, returnEntityMap, returnEntity, baseTableName);
                //ReturnEntityUtils.packageReturnEntity(idReturnEntityMap, returnEntityMap, mainEntity, baseTableName);
                if (returnEntity.equals(mainEntity)) {
                    returnEntityList.add(mainEntity);
                }
            }
        }

        List<T> list = new ArrayList<>(returnEntityList.size());
        for (ReturnEntity returnEntity : returnEntityList) {
            list.add((T) returnEntity.getValue());
        }

        return list;
    }

    /**
     * 把简写转换成详细
     *
     * @param searchSql
     * @return
     */
    private String translateToTableName(String searchSql) {
        String[] strs = WsStringUtils.splitArray(searchSql, '.');
        String ns;
        String s;
        for (int i = 0; i < strs.length; i++) {
            s = strs[i];
            if (s.startsWith("{")) {
                s = s.substring(1, s.length() - 1);
                strs[i] = s;
            }
            ns = translateNameUtils.getParticular(s);
            if (ns != null) {
                strs[i] = ns;
            }
        }
        return WsStringUtils.jointListString(strs, ".");
    }

    public TranslateNameUtils getTranslateNameUtils() {
        return translateNameUtils;
    }

    public String sqlEquationHandel(SqlEquation sqlEquation, String prefix) {
        List<Integer> typeList = sqlEquation.getTypeList();
        List<Object> valueList = sqlEquation.getValueList();
        int length = typeList.size();
        StringBuilder sb = new StringBuilder();
        int type;
        Object value;
        for (int i = 0; i < length; ++i) {
            type = typeList.get(i);
            value = valueList.get(i);
            if (type == 1) {
                if (i > 0 && !typeList.get(i - 1).equals(2)) {
                    throw new RuntimeException("顺序错误,列前面必须为符号");
                }
                if (value instanceof SqlEquation) {
                    sb.append("(");
                    sb.append(sqlEquationHandel((SqlEquation) value, prefix)).append(' ');
                    sb.append(") ");
                } else {
                    ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity((String) value, prefix);
                    sb.append(guardKeyword(translateNameUtils.getAbbreviation(columnBaseEntity.getTableNickName())))
                            .append('.')
                            .append(guardKeyword(columnBaseEntity.getColumnName())).append(' ');
                }
            } else if (type == 2) {
                if (i > 0 && typeList.get(i - 1).equals(2)) {
                    throw new RuntimeException("顺序错误,符号不允许相连");
                }
                //SqlEquation.Symbol symbol = (SqlEquation.Symbol) value;
                //sb.append(symbol.getSymbol()).append(' ');
                sb.append(value);
            } else if (type == 3) {
                if (i > 0 && !typeList.get(i - 1).equals(2)) {
                    throw new RuntimeException("顺序错误,值前面必须为符号");
                }
                if (WsBeanUtils.isArray(value.getClass())) {
                    List<String> symbols = new ArrayList<>();
                    if (WsFieldUtils.classCompare(value.getClass(), Collection.class)) {
                        Collection<?> collection = (Collection<?>) value;
                        for (Object o : collection) {
                            symbols.add("?");
                            baseWhereValueList.add(o);
                        }
                        sb.append("(")
                                .append(WsStringUtils.jointListString(symbols, ","))
                                .append(") ");
                    } else {
                        Object[] values = (Object[]) value;
                        for (Object o : values) {
                            symbols.add("?");
                            baseWhereValueList.add(o);
                        }
                    }
                    sb.append("(")
                            .append(WsStringUtils.jointListString(symbols, ","))
                            .append(") ");

                } else {
                    sb.append("? ");
                    baseWhereValueList.add(value);
                }
            } else if (type == 4) {
                MySearchList searchList = (MySearchList) value;
                SQLModelUtils sqlModelUtils = new SQLModelUtils(searchList, translateNameUtils);
                SelectSqlEntity entity = sqlModelUtils.select();
                sb.append('(')
                        .append(entity.getSelectSql())
                        .append(')');
                baseWhereValueList.addAll(entity.getValueList());
            }

        }
        return sb.toString();
    }

}