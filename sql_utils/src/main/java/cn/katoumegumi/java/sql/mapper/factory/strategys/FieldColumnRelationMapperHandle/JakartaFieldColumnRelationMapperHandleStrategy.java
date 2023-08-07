package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.mapper.model.FieldJoinClass;
import jakarta.persistence.*;

import java.util.Optional;

public class JakartaFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    private final FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory;

    public JakartaFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory) {
        this.fieldColumnRelationMapperFactory = fieldColumnRelationMapperFactory;
    }

    @Override
    public boolean canUse() {
        try {
            Class.forName("jakarta.persistence.Table");
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
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        Transient t = beanPropertyModel.getAnnotation(Transient.class);
        return t != null;
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
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
        return Optional.of(new FieldColumnRelation(id != null,  columnName, beanProperty));
    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
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
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinMapper.getClazz(), beanProperty);
        fieldJoinClass.setNickName(beanProperty.getPropertyName());
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
}
