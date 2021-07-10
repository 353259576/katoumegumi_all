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
import javafx.scene.control.TableColumn;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
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
    private static final Map<Class<?>, CountDownLatch> CLASS_COUNT_DOWN_LATCH_MAP = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(0, 16,0L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
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

    /**
     * 解析实体对象
     *
     * @param clazz
     * @return
     */
    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz) {
        FieldColumnRelationMapper fieldColumnRelationMapper = MAPPER_MAP.get(clazz);
        if (fieldColumnRelationMapper != null) {
            return fieldColumnRelationMapper;
        }
        CountDownLatch countDownLatch = CLASS_COUNT_DOWN_LATCH_MAP.computeIfAbsent(clazz, c -> {
            CountDownLatch cdl = new CountDownLatch(1);
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
                    if(tableTemplate == null) {
                        Annotation annotation = clazz.getAnnotation(Entity.class);
                        if (annotation == null) {
                            annotation = clazz.getAnnotation(Table.class);
                        }
                        FieldColumnRelationMapper mapper = null;
                        if (annotation != null) {
                            mapper = hibernateAnalysisClassRelation(clazz);
                        } else {
                            mapper = mybatisPlusAnalysisClassRelation(clazz);
                        }
                        MAPPER_MAP.put(c, mapper);
                    }else {
                        Class<?> templateClass = tableTemplate.value();
                        FieldColumnRelationMapper baseMapper = analysisClassRelation(templateClass);
                        Field[] fields = WsFieldUtils.getFieldAll(clazz);
                        if(WsListUtils.isNotEmpty(fields)) {
                            FieldColumnRelationMapper mapper = new FieldColumnRelationMapper(baseMapper.getNickName(),baseMapper.getTableName(),clazz,baseMapper);
                            for (Field field : fields) {
                                Transient annotation = field.getAnnotation(Transient.class);
                                if(annotation != null){
                                    continue;
                                }
                                if (WsBeanUtils.isBaseType(field.getType())) {
                                    FieldColumnRelation templateRelation = baseMapper.containsFieldColumnRelationByFieldName(field.getName());
                                    if (templateRelation != null) {
                                        FieldColumnRelation relation = new FieldColumnRelation(templateRelation.isId(), field.getName(), field, templateRelation.getColumnName(), field.getType());
                                        if(relation.isId()) {
                                            mapper.getIds().add(relation);
                                        }else {
                                            mapper.getFieldColumnRelations().add(relation);
                                        }
                                        mapper.putFieldColumnRelationMap(relation.getFieldName(),relation);
                                    }
                                } else {
                                    Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                                    FieldJoinClass fieldJoinClass = new FieldJoinClass(WsBeanUtils.isArray(field.getType()),joinClass,field);
                                    fieldJoinClass.setNickName(field.getName());
                                    fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                                    mapper.getFieldJoinClasses().add(fieldJoinClass);
                                }
                            }
                            mapper.markSignLocation();
                            MAPPER_MAP.put(c,mapper);
                        }

                    }
                }catch (Throwable e){
                    e.printStackTrace();
                    throw e;
                }finally {
                    CLASS_COUNT_DOWN_LATCH_MAP.remove(c);
                    cdl.countDown();
                }


            });

            return cdl;
        });
        try {
            boolean k = countDownLatch.await(1, TimeUnit.MINUTES);
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
     * @param clazz
     * @return
     */
    private static FieldColumnRelationMapper hibernateAnalysisClassRelation(Class<?> clazz) {
        if(!check(clazz)){
            return null;
        }
        Table table = clazz.getAnnotation(Table.class);
        String tableName = null;
        if (WsStringUtils.isBlank(table.name())) {
            tableName = getChangeColumnName(table.name());
        } else {
            tableName = table.name();
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        assert fields != null;
        for (Field field : fields) {
            //field.setAccessible(true);
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
                String columnName = null;
                if (column == null || WsStringUtils.isBlank(column.name())) {
                    columnName = getChangeColumnName(field.getName());
                } else {
                    columnName = column.name();
                }
                FieldColumnRelation fieldColumnRelation = new FieldColumnRelation(isId, field.getName(), field, columnName, field.getType());
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
                if (isId) {
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            } else {
                boolean isArray = WsFieldUtils.isArrayType(field);
                Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                /*Class<?> joinClass = field.getType();
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
                }*/

                FieldColumnRelationMapper mapper = analysisClassRelation(joinClass);
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
        return fieldColumnRelationMapper;
    }

    /**
     * 解析mybatis plus注解
     *
     * @param clazz
     * @return
     */
    private static FieldColumnRelationMapper mybatisPlusAnalysisClassRelation(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName = null;
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
        for (Field field : fields) {
            Transient aTransient = field.getAnnotation(Transient.class);
            if (aTransient != null) {
                continue;
            }
            field.setAccessible(true);
            if (WsBeanUtils.isBaseType(field.getType())) {
                TableId id = field.getAnnotation(TableId.class);
                FieldColumnRelation fieldColumnRelation = null;
                if (id == null) {
                    TableField column = field.getAnnotation(TableField.class);
                    if (column != null && !column.exist()) {
                        continue;
                    }
                    String columnName = null;
                    if (column == null || WsStringUtils.isBlank(column.value())) {
                        columnName = getChangeColumnName(field.getName());
                    } else {
                        columnName = column.value();
                    }
                    fieldColumnRelation = new FieldColumnRelation(false, field.getName(), field, columnName, field.getType());
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                } else {
                    String columnName = null;
                    if (WsStringUtils.isBlank(id.value())) {
                        columnName = getChangeColumnName(getChangeColumnName(field.getName()));
                    } else {
                        columnName = id.value();
                    }
                    fieldColumnRelation = new FieldColumnRelation(true, field.getName(), field, columnName, field.getType());
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                }
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
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
                analysisClassRelation(joinClass);
                FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinClass, field);
                fieldJoinClass.setNickName(field.getName());
                fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
            }
        }
        fieldColumnRelationMapper.markSignLocation();
        MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }

    /**
     * 对没有指定名称的列名自动驼峰更改
     *
     * @param fieldName
     * @return
     */
    public static String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
    }

    public static boolean check(Class<?> clazz){
        Annotation[] annotations = clazz.getAnnotations();
        if(WsListUtils.isEmpty(annotations)){
            return false;
        }else {
            for(Annotation annotation:annotations){
                if(annotation instanceof Table || annotation instanceof TableName || annotation instanceof TableTemplate){
                    return true;
                }
            }
            return false;
        }
    }

}
