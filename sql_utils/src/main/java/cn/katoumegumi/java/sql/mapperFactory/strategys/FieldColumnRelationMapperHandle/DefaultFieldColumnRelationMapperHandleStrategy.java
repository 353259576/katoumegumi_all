package cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandleStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
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
        boolean isArray = WsFieldUtils.isArrayType(beanProperty.getPropertyClass());
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinMapper.getClazz(), beanProperty);
        fieldJoinClass.setNickName(beanProperty.getPropertyName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        return Optional.of(fieldJoinClass);
    }
}
