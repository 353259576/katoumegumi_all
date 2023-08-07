package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.mapper.model.FieldJoinClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DefaultFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    private static final Set<Class<?>> DEFAULT_IGNORE_FIELD = new HashSet<>();

    static {
        DEFAULT_IGNORE_FIELD.add(Class.class);
        DEFAULT_IGNORE_FIELD.add(Field.class);
        DEFAULT_IGNORE_FIELD.add(Method.class);
        DEFAULT_IGNORE_FIELD.add(Object.class);
    }

    private final FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory;

    public DefaultFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory) {
        this.fieldColumnRelationMapperFactory = fieldColumnRelationMapperFactory;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return false;
    }

    @Override
    public Optional<FieldColumnRelationMapper> getTableName(Class<?> clazz) {
        return Optional.empty();
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        return DEFAULT_IGNORE_FIELD.contains(beanPropertyModel.getGenericClass()==null?beanPropertyModel.getPropertyClass():beanPropertyModel.getGenericClass());
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        return Optional.of(new FieldColumnRelation(false, fieldColumnRelationMapperFactory.getChangeColumnName(beanProperty.getPropertyName()), beanProperty));
    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        boolean isArray = WsReflectUtils.isArrayType(beanProperty.getPropertyClass());
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinMapper.getClazz(), beanProperty);
        fieldJoinClass.setNickName(beanProperty.getPropertyName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        return Optional.of(fieldJoinClass);
    }
}
