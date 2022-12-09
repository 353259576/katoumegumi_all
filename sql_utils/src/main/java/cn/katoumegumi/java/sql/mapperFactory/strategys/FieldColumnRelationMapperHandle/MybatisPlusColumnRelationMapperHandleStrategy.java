package cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandleStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Field;
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
    public Optional<FieldColumnRelationMapper> getTableName(Class<?> clazz) {
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
        return Optional.of(new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz));
    }

    @Override
    public boolean isIgnoreField(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField == null) {
            return false;
        }
        return !tableField.exist();
    }

    @Override
    public Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper, Field field) {
        TableId tableId = field.getAnnotation(TableId.class);
        String columnName;
        if (tableId != null) {
            columnName = tableId.value();
        } else {
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField == null) {
                return Optional.empty();
            }
            columnName = tableField.value();
        }
        if (WsStringUtils.isBlank(columnName)) {
            columnName = fieldColumnRelationMapperFactory.getChangeColumnName(field.getName());
        }
        return Optional.of(new FieldColumnRelation(tableId != null, field.getName(), field, columnName, field.getType()));

    }

    @Override
    public Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, Field field) {
        return Optional.empty();
    }
}
