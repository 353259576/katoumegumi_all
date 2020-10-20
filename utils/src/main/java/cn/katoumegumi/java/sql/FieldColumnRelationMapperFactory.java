package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import javax.persistence.criteria.JoinType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于生成FieldColumnRelationMapper
 */
public class FieldColumnRelationMapperFactory {

    /**
     * 缓存实体对应的对象属性与列名的关联
     */
    private static final Map<Class<?>, FieldColumnRelationMapper> mapperMap = new ConcurrentHashMap<>();

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
        fieldColumnRelationMapper.markSignLocation();
        mapperMap.put(clazz, fieldColumnRelationMapper);
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
        fieldColumnRelationMapper.setClazz(clazz);
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
                    if (column != null && !column.exist()) {
                        continue;
                    }
                    if (column == null || WsStringUtils.isBlank(column.value())) {
                        fieldColumnRelation.setColumnName(getChangeColumnName(field.getName()));
                    } else {
                        fieldColumnRelation.setColumnName(column.value());
                    }
                    fieldColumnRelation.setId(false);
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                } else {
                    fieldColumnRelation.setId(true);
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
        fieldColumnRelationMapper.markSignLocation();
        mapperMap.put(clazz, fieldColumnRelationMapper);
        return fieldColumnRelationMapper;
    }

    /**
     * 对没有指定名称的列名自动驼峰更改
     * @param fieldName
     * @return
     */
    public static String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
    }

}
