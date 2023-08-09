package cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.mapper.factory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;
import cn.katoumegumi.java.sql.mapper.model.ObjectPropertyJoinRelation;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Optional;

public class MybatisPlusColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    private final FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory;

    public MybatisPlusColumnRelationMapperHandleStrategy(FieldColumnRelationMapperFactory fieldColumnRelationMapperFactory) {
        this.fieldColumnRelationMapperFactory = fieldColumnRelationMapperFactory;
    }

    @Override
    public boolean canUse() {
        try {
            Class.forName("com.baomidou.mybatisplus.annotation.TableName");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        return table != null;
    }

    @Override
    public Optional<PropertyColumnRelationMapper> getTableName(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        if (table == null) {
            return Optional.empty();
        }
        String tableName;
        if (WsStringUtils.isBlank(table.value())) {
            tableName = fieldColumnRelationMapperFactory.getChangeColumnName(clazz.getSimpleName());
        } else {
            tableName = table.value();
        }
        return Optional.of(new PropertyColumnRelationMapper(clazz.getSimpleName(), tableName, clazz));
    }

    @Override
    public boolean isIgnoreField(BeanPropertyModel beanPropertyModel) {
        TableField tableField = beanPropertyModel.getAnnotation(TableField.class);
        if (tableField == null) {
            return false;
        }
        return !tableField.exist();
    }

    @Override
    public Optional<PropertyColumnRelation> getColumnName(PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        TableId tableId = beanProperty.getAnnotation(TableId.class);
        String columnName;
        if (tableId != null) {
            columnName = tableId.value();
        } else {
            TableField tableField = beanProperty.getAnnotation(TableField.class);
            if (tableField == null) {
                return Optional.empty();
            }
            columnName = tableField.value();
        }
        if (WsStringUtils.isBlank(columnName)) {
            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(beanProperty.getPropertyName());
        }
        return Optional.of(new PropertyColumnRelation(tableId != null, columnName, beanProperty));

    }

    @Override
    public Optional<ObjectPropertyJoinRelation> getJoinRelation(PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        return Optional.empty();
    }
}
