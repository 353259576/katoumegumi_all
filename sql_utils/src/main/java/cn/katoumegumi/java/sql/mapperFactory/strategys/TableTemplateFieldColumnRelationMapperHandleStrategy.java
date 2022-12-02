package cn.katoumegumi.java.sql.mapperFactory.strategys;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.Transient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TableTemplateFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy {

    @Override
    public boolean canHandle(Class<?> clazz) {
        TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
        return tableTemplate != null;
    }

    @Override
    public FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        TableTemplate tableTemplate = clazz.getAnnotation(TableTemplate.class);
        Class<?> templateClass = tableTemplate.value();
        FieldColumnRelationMapper baseMapper = FieldColumnRelationMapperFactory.analysisClassRelation(templateClass,true);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        if (WsListUtils.isNotEmpty(fields)) {
            return null;
        }
        FieldColumnRelationMapper mapper = new FieldColumnRelationMapper(baseMapper.getNickName(), baseMapper.getTableName(), clazz, baseMapper);

        List<Field> baseTypeFieldList = new ArrayList<>();
        List<Field> joinClassFieldList = new ArrayList<>();

        for (Field field : fields) {
            if (isIgnoreField(field)) {
                continue;
            }
            if (WsBeanUtils.isBaseType(field.getType())) {
                baseTypeFieldList.add(field);
            } else {
                joinClassFieldList.add(field);
            }
        }

        if (WsListUtils.isNotEmpty(baseTypeFieldList)) {
            for (Field field : baseTypeFieldList) {
                FieldColumnRelation templateRelation = baseMapper.containsFieldColumnRelationByFieldName(field.getName());
                if (templateRelation != null) {
                    FieldColumnRelation relation = new FieldColumnRelation(templateRelation.isId(), field.getName(), field, templateRelation.getColumnName(), field.getType());
                    if (relation.isId()) {
                        mapper.getIds().add(relation);
                    } else {
                        mapper.getFieldColumnRelations().add(relation);
                    }
                    mapper.putFieldColumnRelationMap(relation.getFieldName(), relation);
                }
            }
        }
        FieldColumnRelationMapperFactory.putIncompleteMapper(clazz, mapper);
        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
                Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                FieldJoinClass fieldJoinClass = new FieldJoinClass(WsBeanUtils.isArray(field.getType()), joinClass, field);
                fieldJoinClass.setNickName(field.getName());
                fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
                mapper.getFieldJoinClasses().add(fieldJoinClass);
            }
        }
        mapper.markSignLocation();
        FieldColumnRelationMapperFactory.putMapper(clazz, mapper);
        FieldColumnRelationMapperFactory.removeIncompleteMapper(clazz);
        return mapper;

    }

    public boolean isIgnoreField(Field field) {
        Transient aTransient = field.getAnnotation(Transient.class);
        if (aTransient != null) {
            return true;
        }
        javax.persistence.Transient jTransient = field.getAnnotation(javax.persistence.Transient.class);
        if(jTransient != null){
            return true;
        }
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null && !tableField.exist();
    }
}
