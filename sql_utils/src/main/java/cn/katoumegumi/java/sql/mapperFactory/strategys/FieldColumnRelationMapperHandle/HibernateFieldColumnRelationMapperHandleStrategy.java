package cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandleStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HibernateFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    private final FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory;

    public HibernateFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory) {
        this.fieldColumnRelationMapperFactory = fieldColumnRelationMapperFactory;
    }

    @Override
    public boolean canUse() {
        try {
            Class.forName("javax.persistence.Table");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return table != null;
    }

    @Override
    public Optional<FieldColumnRelationMapper> getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            return Optional.empty();
        }
        String tableName;
        if (WsStringUtils.isBlank(table.name())) {
            tableName = fieldColumnRelationMapperFactory.getChangeColumnName(clazz.getSimpleName());
        } else {
            tableName = table.name();
        }

        return Optional.of(new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz));
    }

    @Override
    public boolean isIgnoreField(Field field) {
        Transient t = field.getAnnotation(Transient.class);
        return t != null;
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, Field field) {
        Id id = field.getAnnotation(Id.class);
        Column column = field.getAnnotation(Column.class);
        if (id == null && column == null) {
            return Optional.empty();
        }
        String columnName;
        if (column == null || WsStringUtils.isBlank(column.name())) {
            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(field.getName());
        } else {
            columnName = column.name();
        }
        return Optional.of(new FieldColumnRelation(id != null, field.getName(), field, columnName, field.getType()));
    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, Field field) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn == null) {
            return Optional.empty();
        }
        String name = joinColumn.name();
        if (WsStringUtils.isBlank(name)) {
            name = mainMapper.getIds().get(0).getColumnName();
        }
        String referenced = joinColumn.referencedColumnName();
        if (WsStringUtils.isBlank(referenced)) {
            referenced = joinMapper.getIds().get(0).getColumnName();
        }
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        boolean isArray = WsFieldUtils.isArrayType(field);
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinMapper.getClazz(), field);
        fieldJoinClass.setNickName(field.getName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        if (oneToMany == null) {
            fieldJoinClass.setJoinColumn(name);
            fieldJoinClass.setAnotherJoinColumn(referenced);
            return Optional.of(fieldJoinClass);
        } else {
            fieldJoinClass.setJoinColumn(referenced);
            fieldJoinClass.setAnotherJoinColumn(name);
            return Optional.of(fieldJoinClass);
        }
    }

    //@Override
    public FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName;
        if (WsStringUtils.isBlank(table.name())) {
            tableName = fieldColumnRelationMapperFactory.getChangeColumnName(table.name());
        } else {
            tableName = table.name();
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);

        List<Field> baseTypeFieldList = new ArrayList<>();
        List<Field> joinClassFieldList = new ArrayList<>();

        for (Field field : fields) {
            if (isIgnoreField(field)) {
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

        fieldColumnRelationMapperFactory.putIncompleteMapper(clazz, fieldColumnRelationMapper);

        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
                fieldColumnRelationMapper.getFieldJoinClasses().add(createFieldJoinClass(fieldColumnRelationMapper, field));
            }
        }
        fieldColumnRelationMapper.markSignLocation();
        fieldColumnRelationMapperFactory.putMapper(clazz, fieldColumnRelationMapper);
        fieldColumnRelationMapperFactory.removeIncompleteMapper(clazz);
        return fieldColumnRelationMapper;
    }

    public FieldColumnRelation createFieldColumnRelation(Field field) {
        Annotation[] annotations = field.getAnnotations();
        boolean isId = false;
        String columnName = null;
        boolean getId = false;
        boolean getColumn = false;
        if (WsListUtils.isNotEmpty(annotations)) {
            for (Annotation annotation : annotations) {
                if (!getColumn) {
                    if (annotation instanceof Column) {
                        columnName = ((Column) annotation).name();
                        getColumn = true;
                    } else if (annotation instanceof TableField) {
                        columnName = ((TableField) annotation).value();
                        getColumn = true;
                    } else if (annotation instanceof TableId) {
                        columnName = ((TableId) annotation).value();
                        getColumn = true;
                    }
                }
                if (!getId) {
                    if (annotation instanceof Id) {
                        isId = true;
                        getId = true;
                    } else if (annotation instanceof TableId) {
                        isId = true;
                        getId = true;
                    }
                }
            }
        }
        if (WsStringUtils.isBlank(columnName)) {
            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(field.getName());
        }
        return new FieldColumnRelation(isId, field.getName(), field, columnName, field.getType());
    }

    public FieldJoinClass createFieldJoinClass(FieldColumnRelationMapper fieldColumnRelationMapper, Field field) {
        boolean isArray = WsFieldUtils.isArrayType(field);
        Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
        FieldColumnRelationMapper mapper = fieldColumnRelationMapperFactory.analysisClassRelation(joinClass, true);
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
        return fieldJoinClass;
    }
}