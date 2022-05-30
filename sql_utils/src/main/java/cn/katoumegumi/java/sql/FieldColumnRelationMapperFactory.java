package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.common.TableJoinType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 用于生成FieldColumnRelationMapper
 *
 * @author 星梦苍天
 */
public class FieldColumnRelationMapperFactory {

    /**
     * 缓存实体对应的对象属性与列名的关联
     */
    private static final Map<Class<?>, FieldColumnRelationMapper> MAPPER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, FieldColumnRelationMapper> INCOMPLETE_MAPPER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, CountDownLatch> CLASS_COUNT_DOWN_LATCH_MAP = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(0, 200, 0L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("sqlUtils mapper生成线程");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });
    /**
     * 是否转换列名
     */
    public static boolean fieldNameChange = true;

    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz) {
        return analysisClassRelation(clazz, false);
    }

    /**
     * 解析实体对象
     *
     * @param clazz class
     * @param allowIncomplete 允许不完整的
     * @return 表与实例映射关系
     */
    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        FieldColumnRelationMapper fieldColumnRelationMapper = MAPPER_MAP.get(clazz);
        if (fieldColumnRelationMapper != null) {
            return fieldColumnRelationMapper;
        }
        if (allowIncomplete) {
            fieldColumnRelationMapper = INCOMPLETE_MAPPER_MAP.get(clazz);
            if (fieldColumnRelationMapper != null) {
                return fieldColumnRelationMapper;
            }
        }
        CountDownLatch countDownLatch = CLASS_COUNT_DOWN_LATCH_MAP.computeIfAbsent(clazz, c -> {
            CountDownLatch cdl = new CountDownLatch(1);
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
                    if (tableTemplate == null) {
                        Annotation annotation = clazz.getAnnotation(Entity.class);
                        if (annotation == null) {
                            annotation = clazz.getAnnotation(Table.class);
                        }
                        FieldColumnRelationMapper mapper;
                        if (annotation != null) {
                            mapper = hibernateAnalysisClassRelation(clazz);
                        } else {
                            mapper = mybatisPlusAnalysisClassRelation(clazz);
                        }
                        MAPPER_MAP.put(c, mapper);
                    } else {
                        Class<?> templateClass = tableTemplate.value();
                        FieldColumnRelationMapper baseMapper = analysisClassRelation(templateClass,true);
                        Field[] fields = WsFieldUtils.getFieldAll(clazz);
                        if (WsListUtils.isNotEmpty(fields)) {
                            FieldColumnRelationMapper mapper = new FieldColumnRelationMapper(baseMapper.getNickName(), baseMapper.getTableName(), clazz, baseMapper);

                            List<Field> baseTypeFieldList = new ArrayList<>();
                            List<Field> joinClassFieldList = new ArrayList<>();

                            for (Field field : fields) {
                                if (ignoreField(field)) {
                                    continue;
                                }
                                if (WsBeanUtils.isBaseType(field.getType())) {
                                    baseTypeFieldList.add(field);
                                } else {
                                    joinClassFieldList.add(field);
                                }
                            }

                            if (WsListUtils.isNotEmpty(baseTypeFieldList)) {
                                for (Field field : baseTypeFieldList) {
                                    FieldColumnRelation templateRelation = baseMapper.containsFieldColumnRelationByFieldName(field.getName());
                                    if (templateRelation != null) {
                                        FieldColumnRelation relation = new FieldColumnRelation(templateRelation.isId(), field.getName(), field, templateRelation.getColumnName(), field.getType());
                                        if (relation.isId()) {
                                            mapper.getIds().add(relation);
                                        } else {
                                            mapper.getFieldColumnRelations().add(relation);
                                        }
                                        mapper.putFieldColumnRelationMap(relation.getFieldName(), relation);
                                    }
                                }
                            }
                            INCOMPLETE_MAPPER_MAP.put(c, mapper);
                            if (WsListUtils.isNotEmpty(joinClassFieldList)) {
                                for (Field field : joinClassFieldList) {
                                    Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                                    FieldJoinClass fieldJoinClass = new FieldJoinClass(WsBeanUtils.isArray(field.getType()), joinClass, field);
                                    fieldJoinClass.setNickName(field.getName());
                                    fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                                    mapper.getFieldJoinClasses().add(fieldJoinClass);
                                }
                            }
                            mapper.markSignLocation();
                            MAPPER_MAP.put(c, mapper);
                            INCOMPLETE_MAPPER_MAP.remove(c);
                        }

                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    CLASS_COUNT_DOWN_LATCH_MAP.remove(c);
                    cdl.countDown();
                }


            });

            return cdl;
        });
        try {
            boolean k = countDownLatch.await(3, TimeUnit.MINUTES);
            if (k) {
                fieldColumnRelationMapper = MAPPER_MAP.get(clazz);
                if (fieldColumnRelationMapper == null) {
                    throw new RuntimeException("解析失败");
                }
                return fieldColumnRelationMapper;
            } else {
                throw new RuntimeException("解析超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("程序异常中断");
        }
    }

    /**
     * 解析hibernate注解
     *
     * @param clazz class
     * @return 表与对象映射关系
     */
    private static FieldColumnRelationMapper hibernateAnalysisClassRelation(Class<?> clazz) {
        if (ignoreClass(clazz)) {
            return null;
        }
        Table table = clazz.getAnnotation(Table.class);
        String tableName;
        if (WsStringUtils.isBlank(table.name())) {
            tableName = getChangeColumnName(table.name());
        } else {
            tableName = table.name();
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;

        List<Field> baseTypeFieldList = new ArrayList<>();
        List<Field> joinClassFieldList = new ArrayList<>();

        for (Field field : fields) {
            if (ignoreField(field)) {
                continue;
            }
            if (WsBeanUtils.isBaseType(field.getType())) {
                baseTypeFieldList.add(field);
            } else {
                joinClassFieldList.add(field);
            }
        }
        if (WsListUtils.isNotEmpty(baseTypeFieldList)) {
            for (Field field : baseTypeFieldList) {
                FieldColumnRelation fieldColumnRelation = createFieldColumnRelation(field);
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
                if (fieldColumnRelation.isId()) {
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            }
        }

        INCOMPLETE_MAPPER_MAP.put(clazz, fieldColumnRelationMapper);

        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
                boolean isArray = WsFieldUtils.isArrayType(field);
                Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                FieldColumnRelationMapper mapper = analysisClassRelation(joinClass, true);
                FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinClass, field);
                fieldJoinClass.setNickName(field.getName());
                fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                //field.setAccessible(true);
                if (joinColumn != null) {
                    String name = joinColumn.name();
                    if (WsStringUtils.isBlank(name)) {
                        name = fieldColumnRelationMapper.getIds().get(0).getColumnName();
                    }
                    String referenced = joinColumn.referencedColumnName();
                    if (WsStringUtils.isBlank(referenced)) {
                        referenced = mapper.getIds().get(0).getColumnName();
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
        fieldColumnRelationMapper.markSignLocation();
        MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
        INCOMPLETE_MAPPER_MAP.remove(clazz);
        return fieldColumnRelationMapper;
    }

    /**
     * 解析mybatis plus注解
     *
     * @param clazz class
     * @return 表与对象映射关系
     */
    private static FieldColumnRelationMapper mybatisPlusAnalysisClassRelation(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName;
        if (table == null) {
            tableName = getChangeColumnName(clazz.getSimpleName());
        } else {
            if (WsStringUtils.isBlank(table.value())) {
                tableName = getChangeColumnName(clazz.getSimpleName());
            } else {
                tableName = table.value();
            }
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;

        List<Field> baseTypeFieldList = new ArrayList<>();
        List<Field> joinClassFieldList = new ArrayList<>();

        for (Field field : fields) {
            if (ignoreField(field)) {
                continue;
            }
            if (WsBeanUtils.isBaseType(field.getType())) {
                baseTypeFieldList.add(field);
            } else {
                joinClassFieldList.add(field);
            }
        }

        if (WsListUtils.isNotEmpty(baseTypeFieldList)) {
            for (Field field : baseTypeFieldList) {
                FieldColumnRelation fieldColumnRelation = createFieldColumnRelation(field);
                if(fieldColumnRelation.isId()){
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                }else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
            }
        }
        INCOMPLETE_MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
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
                analysisClassRelation(joinClass, true);
                FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinClass, field);
                fieldJoinClass.setNickName(field.getName());
                fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
            }
        }
        fieldColumnRelationMapper.markSignLocation();
        MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
        INCOMPLETE_MAPPER_MAP.remove(clazz);
        return fieldColumnRelationMapper;
    }

    /**
     * 对没有指定名称的列名自动驼峰更改
     *
     * @param fieldName 对象字段名称
     * @return 返回表列名
     */
    public static String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
    }

    /**
     * 是否忽略field
     *
     * @param field 字段
     * @return 是否忽略的字段
     */
    public static boolean ignoreField(Field field) {
        Transient aTransient = field.getAnnotation(Transient.class);
        if (aTransient != null) {
            return true;
        }
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null && !tableField.exist();
    }


    public static FieldColumnRelation createFieldColumnRelation(Field field) {
        Annotation[] annotations = field.getAnnotations();
        boolean isId = false;
        String columnName = null;
        boolean getId = false;
        boolean getColumn = false;
        if (WsListUtils.isNotEmpty(annotations)) {
            for (Annotation annotation : annotations) {
                if (!getColumn && annotation instanceof Column) {
                    columnName = ((Column) annotation).name();
                    getColumn = true;
                } else if (!getColumn && annotation instanceof TableField) {
                    columnName = ((TableField) annotation).value();
                    getColumn = true;
                } else if (annotation instanceof TableId) {
                    isId = true;
                    columnName = ((TableId) annotation).value();
                    break;
                } else if (!getId && annotation instanceof Id) {
                    isId = true;
                    getId = true;
                }
            }
        }
        if (WsStringUtils.isBlank(columnName)) {
            columnName = getChangeColumnName(field.getName());
        }
        return new FieldColumnRelation(isId, field.getName(), field, columnName, field.getType());
    }

    /**
     * 是否忽略class
     *
     * @param clazz class
     * @return 是否忽略类
     */
    public static boolean ignoreClass(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        if (WsListUtils.isEmpty(annotations)) {
            return false;
        } else {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Table || annotation instanceof TableName || annotation instanceof TableTemplate) {
                    return false;
                }
            }
            return true;
        }
    }

}
