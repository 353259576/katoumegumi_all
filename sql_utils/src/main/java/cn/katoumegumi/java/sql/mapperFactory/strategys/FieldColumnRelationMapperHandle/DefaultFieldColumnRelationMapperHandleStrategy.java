package cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsFieldUtils;
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
    public boolean isIgnoreField(Field field) {
        return DEFAULT_IGNORE_FIELD.contains(field.getType());
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, Field field) {
        return Optional.of(new FieldColumnRelation(false, field.getName(), field, fieldColumnRelationMapperFactory.getChangeColumnName(field.getName()), field.getType()));
    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, Field field) {
        boolean isArray = WsFieldUtils.isArrayType(field);
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinMapper.getClazz(), field);
        fieldJoinClass.setNickName(field.getName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        return Optional.of(fieldJoinClass);
    }
}
