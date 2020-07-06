package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.JoinType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
     * 缓存实体对应的对象属性与列名的关联
     */
    public static Map<Class<?>, FieldColumnRelationMapper> mapperMap = new ConcurrentHashMap<>();
    /**
     * 是否转换列名
     */
    public static volatile boolean fieldNameChange = true;
    /**
     * 记录where所需要的值
     */
    private final List baseWhereValueList = new ArrayList();
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
    private Map<String, FieldColumnRelationMapper> localMapperMap = new HashMap<>();
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
    }

    public static <T> int getSqlType(Class<T> tClass) {
        if (WsBeanUtils.isBaseType(tClass)) {
            if (WsFieldUtils.classCompare(tClass, String.class)) {
                return Types.VARCHAR;
            } else if (tClass.equals(Integer.class) || tClass.equals(int.class)) {
                return Types.INTEGER;
            } else if (tClass.equals(Long.class) || tClass.equals(long.class)) {
                return Types.BIGINT;
            } else if (tClass.equals(Short.class) || tClass.equals(short.class)) {
                return Types.SMALLINT;
            } else if (tClass.equals(Float.class) || tClass.equals(float.class)) {
                return Types.FLOAT;
            } else if (tClass.equals(Double.class) || tClass.equals(double.class)) {
                return Types.DOUBLE;
            } else if (tClass.equals(Date.class) || tClass.equals(LocalDateTime.class) || tClass.equals(LocalDate.class) || tClass.equals(java.sql.Date.class)) {
                return Types.TIME;
            } else if (tClass.equals(BigDecimal.class)) {
                return Types.DECIMAL;
            }
            return Types.JAVA_OBJECT;
        } else {
            return Types.JAVA_OBJECT;
        }
    }

    /**
     * 解析实体对象
     *
     * @param clazz
     * @return
     */
    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(clazz);
        if (fieldColumnRelationMapper != null) {
            return fieldColumnRelationMapper;
        }
        Annotation annotation = clazz.getAnnotation(Entity.class);
        if (annotation == null) {
            annotation = clazz.getAnnotation(Table.class);
        }
        if (annotation != null) {
            return hibernateAnalysisClassRelation(clazz);
        }
        //annotation = clazz.getAnnotation(TableName.class);
        return mybatisPlusAnalysisClassRelation(clazz);
    }

    /**
     * 解析hibernate注解
     *
     * @param clazz
     * @return
     */
    private static FieldColumnRelationMapper hibernateAnalysisClassRelation(Class<?> clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        Table table = clazz.getAnnotation(Table.class);
        if (WsStringUtils.isBlank(table.name())) {
            fieldColumnRelationMapper.setTableName(getChangeColumnName(table.name()));
        } else {
            fieldColumnRelationMapper.setTableName(table.name());
        }

        fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;
        for (Field field : fields) {

            Transient aTransient = field.getAnnotation(Transient.class);
            if (aTransient != null) {
                continue;
            }

            if (WsBeanUtils.isBaseType(field.getType())) {
                boolean isId = false;
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    isId = true;
                }
                Column column = field.getAnnotation(Column.class);

                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation();
                fieldColumnRelation.setFieldClass(field.getType());
                fieldColumnRelation.setFieldName(field.getName());
                field.setAccessible(true);
                fieldColumnRelation.setField(field);
                fieldColumnRelationMapper.getFieldColumnRelationMap().put(field.getName(), fieldColumnRelation);
                if (column == null || WsStringUtils.isBlank(column.name())) {
                    fieldColumnRelation.setColumnName(getChangeColumnName(field.getName()));
                } else {
                    fieldColumnRelation.setColumnName(column.name());
                }
                fieldColumnRelation.setId(isId);
                if (isId) {
                    fieldColumnRelationMapper.getIdSet().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            } else {
                boolean isArray = false;
                Class<?> joinClass = field.getType();
                if (WsFieldUtils.classCompare(field.getType(), Collection.class)) {
                    String className = field.getGenericType().getTypeName();
                    className = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">"));
                    try {
                        joinClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("不存在的类");
                    }
                    isArray = true;
                } else if (field.getType().isArray()) {
                    String className = field.getGenericType().getTypeName();
                    className = className.substring(0, className.length() - 2);
                    try {
                        joinClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("不存在的类");
                    }
                    isArray = true;
                }

                FieldColumnRelationMapper mapper = analysisClassRelation(joinClass);
                if (mapper != null) {
                    FieldJoinClass fieldJoinClass = new FieldJoinClass();
                    fieldJoinClass.setNickName(field.getName());
                    fieldJoinClass.setJoinClass(joinClass);
                    fieldJoinClass.setJoinType(JoinType.LEFT);
                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    fieldJoinClass.setArray(isArray);
                    field.setAccessible(true);
                    fieldJoinClass.setField(field);
                    if (joinColumn != null) {
                        String name = joinColumn.name();
                        if (WsStringUtils.isBlank(name)) {
                            name = fieldColumnRelationMapper.getIdSet().get(0).getColumnName();
                        }
                        String referenced = joinColumn.referencedColumnName();
                        if (WsStringUtils.isBlank(referenced)) {
                            referenced = mapper.getIdSet().get(0).getColumnName();
                        }
                        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                        if (oneToMany == null) {
                            fieldJoinClass.setAnotherJoinColumn(referenced);
                            fieldJoinClass.setJoinColumn(name);
                        } else {
                            fieldJoinClass.setAnotherJoinColumn(name);
                            fieldJoinClass.setJoinColumn(referenced);
                        }

                    }
                    fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
                }
            }
        }
        fieldColumnRelationMapper.setClazz(clazz);
        if (fieldColumnRelationMapper != null) {
            mapperMap.put(clazz, fieldColumnRelationMapper);
        }

        return fieldColumnRelationMapper;
    }

    /**
     * 解析mybatis plus注解
     *
     * @param clazz
     * @return
     */
    private static FieldColumnRelationMapper mybatisPlusAnalysisClassRelation(Class<?> clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper();
        TableName table = clazz.getAnnotation(TableName.class);
        if (table == null) {
            fieldColumnRelationMapper.setTableName(getChangeColumnName(clazz.getSimpleName()));
            fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
        } else {
            if (WsStringUtils.isBlank(table.value())) {
                fieldColumnRelationMapper.setTableName(getChangeColumnName(clazz.getSimpleName()));
                fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
            } else {
                fieldColumnRelationMapper.setTableName(table.value());
                fieldColumnRelationMapper.setNickName(clazz.getSimpleName());
            }
        }
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;
        for (Field field : fields) {
            Transient aTransient = field.getAnnotation(Transient.class);
            if (aTransient != null) {
                continue;
            }
            if (WsBeanUtils.isBaseType(field.getType())) {
                TableId id = field.getAnnotation(TableId.class);
                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation();
                fieldColumnRelation.setFieldClass(field.getType());
                fieldColumnRelation.setFieldName(field.getName());
                field.setAccessible(true);
                fieldColumnRelation.setField(field);
                fieldColumnRelationMapper.getFieldColumnRelationMap().put(field.getName(), fieldColumnRelation);
                if (id == null) {
                    TableField column = field.getAnnotation(TableField.class);
                    if (column == null || WsStringUtils.isBlank(column.value())) {
                        fieldColumnRelation.setColumnName(getChangeColumnName(field.getName()));
                    } else {
                        fieldColumnRelation.setColumnName(column.value());
                    }
                    fieldColumnRelation.setId(false);
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                } else {
                    if (WsStringUtils.isBlank(id.value())) {
                        fieldColumnRelation.setColumnName(getChangeColumnName(getChangeColumnName(field.getName())));
                    } else {
                        fieldColumnRelation.setColumnName(id.value());
                    }

                    fieldColumnRelationMapper.getIdSet().add(fieldColumnRelation);
                }
            } else {
                boolean isArray = false;
                Class<?> joinClass = field.getType();
                if (WsFieldUtils.classCompare(field.getType(), Collection.class)) {
                    String className = field.getGenericType().getTypeName();
                    className = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">"));
                    try {
                        joinClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("不存在的类");
                    }
                    isArray = true;
                } else if (field.getType().isArray()) {
                    String className = field.getGenericType().getTypeName();
                    className = className.substring(0, className.length() - 2);
                    try {
                        joinClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("不存在的类");
                    }
                    isArray = true;
                }
                if (analysisClassRelation(joinClass) != null) {
                    FieldJoinClass fieldJoinClass = new FieldJoinClass();
                    fieldJoinClass.setNickName(field.getName());
                    fieldJoinClass.setJoinType(JoinType.LEFT);
                    fieldJoinClass.setJoinClass(joinClass);
                    fieldJoinClass.setArray(isArray);
                    field.setAccessible(true);
                    fieldJoinClass.setField(field);
                    fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
                }
            }
        }
        mapperMap.put(clazz, fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }

    /**
     * 对表列名进行转换
     *
     * @param fieldName
     * @return
     */
    public static String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
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
     * 比对象转换成表查询条件
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
            /*if (fieldColumnRelationMapper.getMap() == null) {
                fieldColumnRelationMapper.setMap(localMapperMap);
            } else {
                localMapperMap = fieldColumnRelationMapper.getMap();
            }*/
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
                            if(sqlInterceptor.useCondition(baseMapper)){
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
                    FieldColumnRelationMapper tableMapper = localMapperMap.get(baseTableName + "." + tableRelation.getTableNickName());
                    FieldColumnRelationMapper mainMapper = analysisClassRelation(mainClass);
                    String tableName;
                    if (mainMapper.equals(tableMapper)) {
                        tableName = mainMapper.getNickName();
                    } else {
                        tableName = mainMapper.getNickName() + "." + tableRelation.getTableNickName();
                    }
                    selectSql.append(createJoinSql(tableName, tableMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), joinType));


                } else {
                    selectSql.append(createJoinSql(tableNickName, baseMapper.getFieldColumnRelationByField(tableRelation.getTableColumn()).getColumnName(), mapper.getTableName(), joinTableNickName, mapper.getFieldColumnRelationByField(tableRelation.getJoinTableColumn()).getColumnName(), joinType));

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
        if (mySearchList.getPageVO() != null) {
            return mysqlPaging(mySearchList.getPageVO(), selectSql.toString());
        }

        return selectSql.toString();

    }

    private String searchListBaseCountSQLProcessor() {
        if (searchSql == null) {
            searchListBaseSQLProcessor();
        }
        StringBuilder stringBuilder = new StringBuilder("select count(*) from (");
        stringBuilder.append(searchSql).append(" ) as searchCount");
        return stringBuilder.toString();
    }

    private String mysqlPaging(Page page, String selectSql) {
        if (page.getCurrent() == 0L) {
            page.setCurrent(1);
        }
        return selectSql + " limit " + (page.getCurrent() - 1) * page.getSize() + "," + page.getSize();
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
        StringBuilder tableColumn;
        String prefixString;
        String fieldName;
        FieldColumnRelationMapper mapper;
        FieldColumnRelation fieldColumnRelation = null;

        if (!mySearch.getOperator().equals(SqlOperator.SQL)) {
            if (mySearch.getFieldName().contains(".")) {
                StringBuilder stringBuffer = new StringBuilder();
                stringBuffer.append('`');
                StringBuilder fieldPrefix = new StringBuilder();
                fieldPrefix.append(prefix);
                String[] strs = WsStringUtils.splitArray(mySearch.getFieldName(), '.');
                int i = 0;
                for (; i < strs.length - 1; i++) {
                    fieldPrefix.append('.');
                    fieldPrefix.append(strs[i]);
                }
                prefixString = fieldPrefix.toString();
                fieldName = strs[i];
                mapper = localMapperMap.get(prefixString);
                fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
                stringBuffer.append(getAbbreviation(fieldPrefix.toString()));
                stringBuffer.append('`');
                stringBuffer.append('.')
                        .append('`')
                        .append(fieldColumnRelation.getColumnName())
                        .append('`');
                tableColumn = stringBuffer;
            } else {
                prefixString = prefix;
                fieldName = mySearch.getFieldName();
                StringBuilder stringBuffer = new StringBuilder();
                mapper = localMapperMap.get(prefixString);
                fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
                stringBuffer.append('`')
                        .append(getAbbreviation(prefix))
                        .append('`')
                        .append('.')
                        .append('`')
                        .append(fieldColumnRelation.getColumnName())
                        .append('`');
                tableColumn = stringBuffer;
            }
        } else {
            tableColumn = new StringBuilder();
        }


        switch (mySearch.getOperator()) {
            case EQ:
                tableColumn.append(" = ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LIKE:
                tableColumn.append(" like ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case GT:
                tableColumn.append(" > ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LT:
                tableColumn.append(" < ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case GTE:
                tableColumn.append(" >= ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case LTE:
                tableColumn.append(" <= ?");
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case IN:
                if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                    Collection collection = (Collection) mySearch.getValue();
                    Iterator iterator = collection.iterator();
                    List<String> symbols = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Object o = iterator.next();
                        symbols.add("?");
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
                        baseWhereValueList.add(WsBeanUtils.objectToT(o, fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                    tableColumn.append(')');
                } else {
                    throw new RuntimeException("非数组类型");
                }

                break;
            case NIN:
                if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                    Collection collection = (Collection) mySearch.getValue();
                    Iterator iterator = collection.iterator();
                    List<String> symbols = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Object o = iterator.next();
                        symbols.add("?");
                        baseWhereValueList.add(WsBeanUtils.objectToT(o, fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" not in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                    tableColumn.append(')');

                } else if (mySearch.getValue().getClass().isArray()) {
                    Object[] os = (Object[]) mySearch.getValue();
                    List<String> symbols = new ArrayList<>();
                    for (Object o : os) {
                        symbols.add("?");
                        baseWhereValueList.add(WsBeanUtils.objectToT(o, fieldColumnRelation.getFieldClass()));
                    }
                    tableColumn.append(" no in");
                    tableColumn.append('(');
                    tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                    tableColumn.append(')');
                } else {
                    throw new RuntimeException("非数组类型");
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
                baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(), fieldColumnRelation.getFieldClass()));
                break;
            case SORT:
                tableColumn.append(' ');
                tableColumn.append(mySearch.getValue());
                break;
            case SQL:

                tableColumn.append(translateTableNickName(mySearch.getFieldName()));
                if (mySearch.getValue() != null) {
                    if (mySearch.getValue() instanceof Collection) {
                        Collection collection = (Collection) mySearch.getValue();
                        Iterator iterator = collection.iterator();
                        while (iterator.hasNext()) {
                            baseWhereValueList.add(iterator.next());
                        }
                    } else if (mySearch.getValue().getClass().isArray()) {
                        Object[] os = (Object[]) mySearch.getValue();
                        for (Object o : os) {
                            baseWhereValueList.add(o);
                        }
                    } else {
                        baseWhereValueList.add(mySearch.getValue());
                    }
                }

                //tableColumn.append(mySearch.getValue());
                break;
            case EQP:
                tableColumn.append(" = ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
                break;
            case NEP:
                tableColumn.append(" != ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
                break;
            case GTP:
                tableColumn.append(" > ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
                break;
            case LTP:
                tableColumn.append(" < ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
                break;
            case GTEP:
                tableColumn.append(" >= ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
                break;
            case LTEP:
                tableColumn.append(" <= ").append(translateTableNickName(WsStringUtils.anyToString(mySearch.getValue())));
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
            for (MySearchList searchList : ands) {
                List<String> andStrings = searchListWhereSqlProcessor(searchList, prefix);
                if (andStrings.size() != 0) {
                    if (andStrings.size() == 1) {
                        stringList.add(WsStringUtils.jointListString(andStrings, " and "));
                    } else {
                        stringList.add("(" + WsStringUtils.jointListString(andStrings, " and ") + ")");
                    }
                }

            }
        }
        List<MySearchList> ors = mySearchList.getOrs();
        if (!WsListUtils.isEmpty(ors)) {
            for (MySearchList searchList : ors) {
                List<String> orStrings = searchListWhereSqlProcessor(searchList, prefix);
                if (orStrings.size() != 0) {
                    if (orStrings.size() == 1) {
                        stringList.add(WsStringUtils.jointListString(orStrings, " or "));
                    } else {
                        stringList.add("(" + WsStringUtils.jointListString(orStrings, " or ") + ")");
                    }
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
        if(WsListUtils.isNotEmpty(selectSqlInterceptorMap)) {
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
        String baseSql = "select " + WsStringUtils.jointListString(list, ",") + " from `" + tableName + "` `" + getAbbreviation(fieldColumnRelationMapper.getNickName()) + "` " + WsStringUtils.jointListString(joinString, " ");
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

                if (!checkFieldJoinClass(fieldJoinClass)) {
                    FieldJoinClass newFieldJoinClass = selfFieldJoinClass(tableNickName, fieldJoinClass, mySearchList.getJoins());
                    if (newFieldJoinClass != null) {
                        fieldJoinClass = newFieldJoinClass;
                    }
                }

                if (WsStringUtils.isNotBlank(fieldJoinClass.getJoinColumn())) {
                    if(fieldJoinClass.getNickName().contains(".")){
                        lastTableNickName = analysisClassRelation(mainClass).getNickName() + '.' + fieldJoinClass.getNickName();
                    }else {
                        lastTableNickName = tableNickName + '.' + fieldJoinClass.getNickName();
                    }

                    FieldColumnRelationMapper mapper = analysisClassRelation(fieldJoinClass.getJoinClass());
                    localMapperMap.put(lastTableNickName, mapper);
                    String joinType;
                    if (fieldJoinClass.getJoinType() != null) {
                        switch (fieldJoinClass.getJoinType()) {
                            case LEFT:
                                joinType = "LEFT JOIN";
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


                    joinString.add(createJoinSql(tableNickName, fieldJoinClass.getJoinColumn(), mapper.getTableName(), lastTableNickName, fieldJoinClass.getAnotherJoinColumn(), joinType));

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
            if(usedTableRelation.contains(tableRelation)){
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
            //iterator.remove();
            usedTableRelation.add(tableRelation);

            if (WsListUtils.isNotEmpty(selectSqlInterceptorMap)) {
                for (FieldColumnRelation fieldColumnRelation : mapper.getFieldColumnRelations()) {
                    AbstractSqlInterceptor sqlInterceptor = selectSqlInterceptorMap.get(fieldColumnRelation.getFieldName());
                    if (sqlInterceptor != null) {
                        if(sqlInterceptor.useCondition(mapper)) {
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
     * 整理数据
     *
     * @param mapList
     * @return
     */
    public List<Map> handleMap(List<Map> mapList) {
        List<Map> list = new ArrayList<>(mapList.size());

        Map<String, List<String>> stringListMap = new HashMap<>();

        for (Map map : mapList) {
            Map<String, Map> stringMapMap = new HashMap<>();
            Set<Map.Entry> entries = map.entrySet();
            for (Map.Entry entry : entries) {
                if (entry.getValue() == null) {
                    continue;
                }
                String keyString = getParticular((String) entry.getKey());
                List<String> keyPrefixs = stringListMap.get(keyString);
                if (keyPrefixs == null) {
                    keyPrefixs = WsStringUtils.split(keyString, '.');
                    stringListMap.put(keyString, keyPrefixs);
                }

                Map valueMap = stringMapMap;
                Map prevMap = valueMap;
                String kp;
                int length = keyPrefixs.size();
                for (int i = 1; i < length - 1; i++) {
                    kp = keyPrefixs.get(i);
                    valueMap = (Map) prevMap.get(kp);
                    if (valueMap == null) {
                        valueMap = new HashMap();
                        prevMap.put(kp, valueMap);
                    }
                    prevMap = valueMap;
                }
                valueMap.put(keyPrefixs.get(length - 1), entry.getValue());
            }
            list.add(stringMapMap);
            map.clear();
        }
        mapList.clear();
        return list;
    }

    /**
     * 合并数据
     *
     * @param maps
     * @return
     */
    public List<Map> mergeMapList(List<Map> maps) {
        return mergeMapList(maps, mainClass);
    }

    private List<Map> mergeMapList(List<Map> maps, Class<?> tClass) {
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(tClass);

        List<FieldJoinClass> fieldJoinClassList = fieldColumnRelationMapper.getFieldJoinClasses();

        if (WsListUtils.isEmpty(fieldJoinClassList)) {
            return maps;
        }

        Map<String, Class<?>> objectMap = new HashMap<>();
        Set<String> objectTypeSet = new HashSet<>();
        for (FieldJoinClass fieldJoinClass : fieldJoinClassList) {
            if (WsFieldUtils.isArrayType(fieldJoinClass.getField())) {
                objectMap.put(fieldJoinClass.getNickName(), WsFieldUtils.getClassListType(fieldJoinClass.getField()));
            } else {
                objectTypeSet.add(fieldJoinClass.getNickName());
                objectMap.put(fieldJoinClass.getNickName(), fieldJoinClass.getField().getType());
            }
        }
        List<String> idSet = null;
        List<FieldColumnRelation> idFieldColumnRelationList = fieldColumnRelationMapper.getIdSet();
        if (WsListUtils.isNotEmpty(idFieldColumnRelationList)) {
            idSet = new LinkedList<>();
            for (FieldColumnRelation fieldColumnRelation : idFieldColumnRelationList) {
                idSet.add(fieldColumnRelation.getFieldName());
            }
        }

        HashMap<ResultMapIds, Map> set = new HashMap<>();
        Set<String> nameSet = new HashSet<>();
        Iterator<Map> iterator = maps.iterator();
        ResultMapIds resultMapIds;
        Map m1, m2;
        while (iterator.hasNext()) {
            m1 = iterator.next();
            resultMapIds = new ResultMapIds(m1, idSet);
            m2 = set.get(resultMapIds);
            if (m2 == null) {
                set.put(resultMapIds, m1);
                //newMaps.add(m1);
            } else {
                iterator.remove();
                Set<Map.Entry> entries = m2.entrySet();
                for (Map.Entry entry : entries) {
                    if (entry.getValue() instanceof Map) {
                        List list = new ArrayList();
                        list.add(entry.getValue());
                        list.add(m1.get(entry.getKey()));
                        entry.setValue(list);
                        nameSet.add((String) entry.getKey());
                    } else if (entry.getValue() instanceof List) {
                        nameSet.add((String) entry.getKey());
                        ((List) entry.getValue()).add(m1.get(entry.getKey()));
                    }
                }
            }
        }
        set.clear();
        set = null;
        if (nameSet.size() > 0) {
            for (Map map : maps) {
                for (String name : nameSet) {
                    Object o = map.get(name);
                    if (o instanceof List) {
                        List<Map> mapList = mergeMapList((List<Map>) o, objectMap.get(name));
                        if (mapList != null && mapList.size() != 0) {
                            map.put(name, mapList);
                        }
                    }
                }
            }
        }
        nameSet = null;


        return maps;

    }

    /**
     * 转化为对象
     *
     * @param list
     * @param <T>
     * @return
     */
    public <T> List<T> loadingObject(List<Map> list) {
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(mainClass);
        String prefix = fieldColumnRelationMapper.getNickName();
        List<T> newList = new ArrayList<>(list.size());

        for (Map childMap : list) {
            Object o = WsBeanUtils.createObject(mainClass);
            newList.add((T) o);
            loadingObject(o, childMap, fieldColumnRelationMapper, prefix);
        }


        return newList;
    }

    private void loadingObject(Object parentObject, Map parentMap, FieldColumnRelationMapper parentMapper, String prefix) {
        Set<Map.Entry> entries = parentMap.entrySet();
        for (Map.Entry entry : entries) {
            String key = (String) entry.getKey();
            Field field = null;
            Object oValue = entry.getValue();
            String nowPreFix = prefix + "." + key;
            if(oValue instanceof byte[]){
                oValue = new String((byte[])oValue);
            }
            Object nowObject = oValue;
            if (oValue instanceof Map) {
                FieldJoinClass fieldJoinClass = parentMapper.getFieldJoinClassByFieldName(key);
                field = fieldJoinClass.getField();
                FieldColumnRelationMapper nowMapper = localMapperMap.get(nowPreFix);
                nowObject = WsBeanUtils.createObject(nowMapper.getClazz());
                loadingObject(nowObject, (Map) oValue, nowMapper, nowPreFix);
                if (fieldJoinClass.isArray()) {
                    List list = new ArrayList();
                    list.add(nowObject);
                    nowObject = list;
                }
            } else if (oValue instanceof List) {
                FieldJoinClass fieldJoinClass = parentMapper.getFieldJoinClassByFieldName(key);
                field = fieldJoinClass.getField();
                FieldColumnRelationMapper nowMapper = localMapperMap.get(nowPreFix);
                List list = new ArrayList();
                nowObject = list;
                List mapList = (List) oValue;
                for (Object o : mapList) {
                    Object co = WsBeanUtils.createObject(nowMapper.getClazz());
                    list.add(co);
                    loadingObject(co, (Map) o, nowMapper, nowPreFix);
                }
            } else {
                FieldColumnRelation relation = parentMapper.getFieldColumnRelationByField(key);
                field = relation.getField();
                nowObject = WsBeanUtils.objectToT(nowObject, relation.getFieldClass());
            }
            try {
                //assert field != null;
                if (nowObject instanceof List) {
                    if (WsFieldUtils.isArrayType(field)) {
                        field.set(parentObject, nowObject);
                    } else {
                        List list = (List) nowObject;
                        if (list.size() > 0) {
                            field.set(parentObject, list.get(0));
                        }

                    }
                } else {
                    field.set(parentObject, nowObject);
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }
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
        for (int i = 1; i < tList.size(); i++) {
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
    public <T> UpdateSqlEntity update(T t) {
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
            if (o != null) {
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
        String searchSql = searchListBaseSQLProcessor();
        FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mySearchList.getMainClass());
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
                + WsStringUtils.jointListString(setList, ",") + " " + searchSql.substring(searchSql.indexOf(" where "));
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
                case DIVIDE:
                case MULTIPLY:
                case SUBTRACT:
                    break;
                default:
                    continue;
            }
            FieldColumnRelationMapper fieldColumnRelationMapper = analysisClassRelation(mySearch.getTableClass());
            FieldColumnRelation fieldColumnRelation = fieldColumnRelationMapper.getFieldColumnRelationByField(mySearch.getFieldName());
            String columnName = fieldColumnRelation.getColumnName();
            switch (mySearch.getOperator()) {
                case SET:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = ? ";
                    setStrList.add(str);
                    break;
                case ADD:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = " + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + " + ? ";
                    setStrList.add(str);
                    break;
                case SUBTRACT:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = " + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + " - ? ";
                    setStrList.add(str);
                    break;
                case MULTIPLY:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = " + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + " * ? ";
                    setStrList.add(str);
                    break;
                case DIVIDE:
                    str = guardKeyword(getAbbreviation(prefix)) + '.' + guardKeyword(columnName) + " = " + guardKeyword(getAbbreviation(prefix)) + "." + guardKeyword(columnName) + " / ? ";
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

    /**
     * 合并生成数据（没写完废弃）
     *
     * @param mapList
     * @param <T>
     * @return
     */
    @Deprecated
    private <T> List<T> oneLoopMargeMap(List<Map> mapList) {
        FieldColumnRelationMapper fieldColumnRelationMapper = mapperMap.get(mainClass);
        List<FieldColumnRelation> idList = fieldColumnRelationMapper.getIdSet();
        List<FieldJoinClass> joinClasses = fieldColumnRelationMapper.getFieldJoinClasses();
        Map<String, List<String>> cacheNameList = new HashMap<>();
        List tList = new ArrayList();
        for (Map map : mapList) {

            Object o = WsBeanUtils.createObject(mainClass);
            tList.add(o);


            Map<String, Object> objectMap = new HashMap<>();
            Map<Class<?>, Object> classObjectMap = new HashMap<>();
            classObjectMap.put(mainClass, o);
            objectMap.put(fieldColumnRelationMapper.getNickName(), o);

            Set<Map.Entry> entrySet = map.entrySet();
            for (Map.Entry entry : entrySet) {
                String keyName = (String) entry.getKey();
                List<String> keyNameList = cacheNameList.get(keyName);
                if (keyNameList == null) {
                    keyNameList = WsStringUtils.split(keyName, '.');
                    cacheNameList.put(keyName, keyNameList);
                }
                Object nowObject = objectMap.get(keyNameList.get(0));
                FieldColumnRelation fieldColumnRelation = fieldColumnRelationMapper.getFieldColumnRelationByField(keyNameList.get(keyNameList.size() - 1));
                Field field = fieldColumnRelation.getField();
                if (WsBeanUtils.isBaseType(field.getType())) {

                }
                try {
                    field.set(nowObject, WsBeanUtils.objectToT(entry.getValue(), field.getType()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return (List<T>) tList;
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
            value = createAbbreviation(keyword);
            abbreviationMap.put(keyword, value);
            particularMap.put(value, keyword);
            return value;
        } else {
            return value;
        }
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
    private String translateTableNickName(String searchSql) {
        char[] cs = searchSql.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder replaceSb = new StringBuilder();
        char c;
        boolean isReplace = false;
        for (int i = 0; i < cs.length; i++) {
            c = cs[i];
            if (isReplace) {
                if (c == '}') {
                    stringBuilder.append(getAbbreviation(replaceSb.toString()));
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

}



