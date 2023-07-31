package cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandleStrategy;

import java.lang.reflect.Field;
import java.util.Optional;

public class TableTemplateFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    private final FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory;

    public TableTemplateFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory) {
        this.fieldColumnRelationMapperFactory = fieldColumnRelationMapperFactory;
    }

    @Override
    public boolean canUse() {
        try {
            Class.forName("cn.katoumegumi.java.sql.annotation.TableTemplate");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
        return tableTemplate != null;
    }

    @Override
    public Optional<FieldColumnRelationMapper> getTableName(Class<?> clazz) {
        TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
        Class<?> templateClass = tableTemplate.value();
        FieldColumnRelationMapper baseMapper = FieldColumnRelationMapperFactory.analysisClassRelation(templateClass, true);
        FieldColumnRelationMapper mapper = new FieldColumnRelationMapper(baseMapper.getNickName(), baseMapper.getTableName(), clazz, baseMapper);
        return Optional.of(mapper);
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        return false;
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        FieldColumnRelation templateRelation = mainMapper.containsFieldColumnRelationByFieldName(beanProperty.getPropertyName());
        if (templateRelation == null) {
            return Optional.empty();
        }
        FieldColumnRelation relation = new FieldColumnRelation(templateRelation.isId(), templateRelation.getColumnName(),beanProperty);
        return Optional.of(relation);
    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        return Optional.empty();
    }
}
