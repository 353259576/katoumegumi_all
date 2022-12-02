package cn.katoumegumi.java.sql.mapperFactory.strategys;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapperFactory.FieldColumnRelationMapperFactory;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HibernateFieldColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy{

    @Override
    public boolean canHandle(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return table != null;
    }

    @Override
    public FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName;
        if (WsStringUtils.isBlank(table.name())) {
            tableName = FieldColumnRelationMapperFactory.getChangeColumnName(table.name());
        } else {
            tableName = table.name();
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = new FieldColumnRelationMapper(clazz.getSimpleName(), tableName, clazz);
        Field[] fields = WsFieldUtils.getFieldAll(clazz);

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
                FieldColumnRelation fieldColumnRelation = createFieldColumnRelation(field);
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
                if (fieldColumnRelation.isId()) {
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            }
        }

        FieldColumnRelationMapperFactory.putIncompleteMapper(clazz, fieldColumnRelationMapper);

        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
                fieldColumnRelationMapper.getFieldJoinClasses().add(createFieldJoinClass(fieldColumnRelationMapper,field));
            }
        }
        fieldColumnRelationMapper.markSignLocation();
        FieldColumnRelationMapperFactory.putMapper(clazz, fieldColumnRelationMapper);
        FieldColumnRelationMapperFactory.removeIncompleteMapper(clazz);
        return fieldColumnRelationMapper;
    }

    public FieldColumnRelation createFieldColumnRelation(Field field) {
        Annotation[] annotations = field.getAnnotations();
        boolean isId = false;
        String columnName = null;
        boolean getId = false;
        boolean getColumn = false;
        if (WsListUtils.isNotEmpty(annotations)) {
            for (Annotation annotation : annotations) {
                if(!getColumn){
                    if(annotation instanceof Column){
                        columnName = ((Column) annotation).name();
                        getColumn = true;
                    }else if(annotation instanceof TableField){
                        columnName = ((TableField) annotation).value();
                        getColumn = true;
                    }else if(annotation instanceof TableId){
                        columnName = ((TableId) annotation).value();
                        getColumn = true;
                    }
                }
                if(!getId){
                    if (annotation instanceof Id) {
                        isId = true;
                        getId = true;
                    } else if(annotation instanceof TableId){
                        isId = true;
                        getId = true;
                    }
                }
            }
        }
        if (WsStringUtils.isBlank(columnName)) {
            columnName = FieldColumnRelationMapperFactory.getChangeColumnName(field.getName());
        }
        return new FieldColumnRelation(isId, field.getName(), field, columnName, field.getType());
    }

    public FieldJoinClass createFieldJoinClass(FieldColumnRelationMapper fieldColumnRelationMapper,Field field) {
        boolean isArray = WsFieldUtils.isArrayType(field);
        Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
        FieldColumnRelationMapper mapper = FieldColumnRelationMapperFactory.analysisClassRelation(joinClass, true);
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinClass, field);
        fieldJoinClass.setNickName(field.getName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        //field.setAccessible(true);
        if (joinColumn != null) {
            String name = joinColumn.name();
            if (WsStringUtils.isBlank(name)) {
                name = fieldColumnRelationMapper.getIds().get(0).getColumnName();
            }
            String referenced = joinColumn.referencedColumnName();
            if (WsStringUtils.isBlank(referenced)) {
                referenced = mapper.getIds().get(0).getColumnName();
            }
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            if (oneToMany == null) {
                fieldJoinClass.setAnotherJoinColumn(referenced);
                fieldJoinClass.setJoinColumn(name);
            } else {
                fieldJoinClass.setAnotherJoinColumn(name);
                fieldJoinClass.setJoinColumn(referenced);
            }
        }
        return fieldJoinClass;
    }

    public boolean isIgnoreField(Field field) {
        Transient aTransient = field.getAnnotation(Transient.class);
        if (aTransient != null) {
            return true;
        }
        jakarta.persistence.Transient jTransient = field.getAnnotation(jakarta.persistence.Transient.class);
        if(jTransient != null){
            return true;
        }
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null && !tableField.exist();
    }
}
