package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.ObjectPropertyJoinRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

import javax.persistence.*;
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
    public Optional<PropertyColumnRelationMapper> getTableName(Class<?> clazz) {
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

        return Optional.of(new PropertyColumnRelationMapper(clazz.getSimpleName(), tableName, clazz));
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        Transient t = beanPropertyModel.getAnnotation(Transient.class);
        return t != null;
    }

    @Override
    public Optional<PropertyColumnRelation> getColumnName(PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        Id id = beanProperty.getAnnotation(Id.class);
        Column column = beanProperty.getAnnotation(Column.class);
        if (id == null && column == null) {
            return Optional.empty();
        }
        String columnName;
        if (column == null || WsStringUtils.isBlank(column.name())) {
            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(beanProperty.getPropertyName());
        } else {
            columnName = column.name();
        }
        return Optional.of(new PropertyColumnRelation(id != null, columnName, beanProperty));
    }

    @Override
    public Optional<ObjectPropertyJoinRelation> getJoinRelation(PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        JoinColumn joinColumn = beanProperty.getAnnotation(JoinColumn.class);
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
        OneToMany oneToMany = beanProperty.getAnnotation(OneToMany.class);
        boolean isArray = WsReflectUtils.isArrayType(beanProperty.getPropertyClass());
        ObjectPropertyJoinRelation objectPropertyJoinRelation = new ObjectPropertyJoinRelation(isArray, joinMapper.getClazz(), beanProperty);
        objectPropertyJoinRelation.setNickName(beanProperty.getPropertyName());
        objectPropertyJoinRelation.setJoinType(TableJoinType.LEFT_JOIN);
        if (oneToMany == null) {
            objectPropertyJoinRelation.setJoinColumn(name);
            objectPropertyJoinRelation.setAnotherJoinColumn(referenced);
            return Optional.of(objectPropertyJoinRelation);
        } else {
            objectPropertyJoinRelation.setJoinColumn(referenced);
            objectPropertyJoinRelation.setAnotherJoinColumn(name);
            return Optional.of(objectPropertyJoinRelation);
        }
    }

    //@Override
//    public PropertyColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
//        Table table = clazz.getAnnotation(Table.class);
//        String tableName;
//        if (WsStringUtils.isBlank(table.name())) {
//            tableName = fieldColumnRelationMapperFactory.getChangeColumnName(table.name());
//        } else {
//            tableName = table.name();
//        }
//        PropertyColumnRelationMapper fieldColumnRelationMapper = new PropertyColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
//        Field[] fields = WsReflectUtils.getFieldAll(clazz);
//
//        List<Field> baseTypeFieldList = new ArrayList<>();
//        List<Field> joinClassFieldList = new ArrayList<>();
//
//        for (Field field : fields) {
//            if (isIgnoreField(field)) {
//                continue;
//            }
//            if (WsBeanUtils.isBaseType(field.getType())) {
//                baseTypeFieldList.add(field);
//            } else {
//                joinClassFieldList.add(field);
//            }
//        }
//        if (WsCollectionUtils.isNotEmpty(baseTypeFieldList)) {
//            for (Field field : baseTypeFieldList) {
//                PropertyColumnRelation fieldColumnRelation = createFieldColumnRelation(field);
//                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
//                if (fieldColumnRelation.isId()) {
//                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
//                } else {
//                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
//                }
//            }
//        }
//
//        FieldColumnRelationMapperFactory.putIncompleteMapper(clazz, fieldColumnRelationMapper);
//
//        if (WsCollectionUtils.isNotEmpty(joinClassFieldList)) {
//            for (Field field : joinClassFieldList) {
//                fieldColumnRelationMapper.getFieldJoinClasses().add(createFieldJoinClass(fieldColumnRelationMapper, field));
//            }
//        }
//        fieldColumnRelationMapper.markSignLocation();
//        FieldColumnRelationMapperFactory.putMapper(clazz, fieldColumnRelationMapper);
//        FieldColumnRelationMapperFactory.removeIncompleteMapper(clazz);
//        return fieldColumnRelationMapper;
//    }

//    public PropertyColumnRelation createFieldColumnRelation(Field field) {
//        Annotation[] annotations = field.getAnnotations();
//        boolean isId = false;
//        String columnName = null;
//        boolean getId = false;
//        boolean getColumn = false;
//        if (WsCollectionUtils.isNotEmpty(annotations)) {
//            for (Annotation annotation : annotations) {
//                if (!getColumn) {
//                    if (annotation instanceof Column) {
//                        columnName = ((Column) annotation).name();
//                        getColumn = true;
//                    } else if (annotation instanceof TableField) {
//                        columnName = ((TableField) annotation).value();
//                        getColumn = true;
//                    } else if (annotation instanceof TableId) {
//                        columnName = ((TableId) annotation).value();
//                        getColumn = true;
//                    }
//                }
//                if (!getId) {
//                    if (annotation instanceof Id) {
//                        isId = true;
//                        getId = true;
//                    } else if (annotation instanceof TableId) {
//                        isId = true;
//                        getId = true;
//                    }
//                }
//            }
//        }
//        if (WsStringUtils.isBlank(columnName)) {
//            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(field.getName());
//        }
//        return new PropertyColumnRelation(isId, field.getName(), field, columnName, field.getType());
//    }
//
//    public ObjectPropertyJoinRelation createFieldJoinClass(PropertyColumnRelationMapper fieldColumnRelationMapper, Field field) {
//        boolean isArray = WsReflectUtils.isArrayType(field);
//        Class<?> joinClass = WsReflectUtils.getClassTypeof(field);
//        PropertyColumnRelationMapper mapper = FieldColumnRelationMapperFactory.analysisClassRelation(joinClass, true);
//        ObjectPropertyJoinRelation fieldJoinClass = new ObjectPropertyJoinRelation(isArray, joinClass, field);
//        fieldJoinClass.setNickName(field.getName());
//        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
//        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
//        //field.setAccessible(true);
//        if (joinColumn != null) {
//            String name = joinColumn.name();
//            if (WsStringUtils.isBlank(name)) {
//                name = fieldColumnRelationMapper.getIds().get(0).getColumnName();
//            }
//            String referenced = joinColumn.referencedColumnName();
//            if (WsStringUtils.isBlank(referenced)) {
//                referenced = mapper.getIds().get(0).getColumnName();
//            }
//            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
//            if (oneToMany == null) {
//                fieldJoinClass.setAnotherJoinColumn(referenced);
//                fieldJoinClass.setJoinColumn(name);
//            } else {
//                fieldJoinClass.setAnotherJoinColumn(name);
//                fieldJoinClass.setJoinColumn(referenced);
//            }
//        }
//        return fieldJoinClass;
//    }
}
