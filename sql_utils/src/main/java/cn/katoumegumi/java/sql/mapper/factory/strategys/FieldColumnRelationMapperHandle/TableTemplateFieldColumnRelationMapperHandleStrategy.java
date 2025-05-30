package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyObjectColumnJoinRelation;
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
        PropertyColumnRelationMapper mapper = new PropertyColumnRelationMapper(baseMapper.getEntityName(), baseMapper.getTableName(), clazz, baseMapper);
        return Optional.of(mapper);
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        return false;
    }

    @Override
    public Optional<PropertyBaseColumnRelation> getColumnName(PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty,int abbreviation) {
        PropertyBaseColumnRelation templateRelation = mainMapper.containsFieldColumnRelationByFieldName(beanProperty.getPropertyName());
        if (templateRelation == null) {
            return Optional.empty();
        }
        PropertyBaseColumnRelation relation = new PropertyBaseColumnRelation(templateRelation.isId(), templateRelation.getColumnName(),beanProperty,abbreviation);
        return Optional.of(relation);
    }

    @Override
    public Optional<PropertyObjectColumnJoinRelation> getJoinRelation(PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty,int abbreviation) {
        return Optional.empty();
    }
}
