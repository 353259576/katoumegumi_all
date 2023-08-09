package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.ObjectPropertyJoinRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

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
    public Optional<PropertyColumnRelationMapper> getTableName(Class<?> clazz) {
        TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
        Class<?> templateClass = tableTemplate.value();
        PropertyColumnRelationMapper baseMapper = FieldColumnRelationMapperFactory.analysisClassRelation(templateClass, true);
        PropertyColumnRelationMapper mapper = new PropertyColumnRelationMapper(baseMapper.getNickName(), baseMapper.getTableName(), clazz, baseMapper);
        return Optional.of(mapper);
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        return false;
    }

    @Override
    public Optional<PropertyColumnRelation> getColumnName(PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        PropertyColumnRelation templateRelation = mainMapper.containsFieldColumnRelationByFieldName(beanProperty.getPropertyName());
        if (templateRelation == null) {
            return Optional.empty();
        }
        PropertyColumnRelation relation = new PropertyColumnRelation(templateRelation.isId(), templateRelation.getColumnName(),beanProperty);
        return Optional.of(relation);
    }

    @Override
    public Optional<ObjectPropertyJoinRelation> getJoinRelation(PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        return Optional.empty();
    }
}
