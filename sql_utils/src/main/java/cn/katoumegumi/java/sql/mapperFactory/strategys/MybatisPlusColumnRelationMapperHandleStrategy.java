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
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MybatisPlusColumnRelationMapperHandleStrategy implements FieldColumnRelationMapperHandleStrategy{

    @Override
    public boolean canHandle(Class<?> clazz) {
        TableName table = clazz.getAnnotation(TableName.class);
        return table != null;
    }

    @Override
    public FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        TableName table = clazz.getAnnotation(TableName.class);
        String tableName;
        if (table == null) {
            tableName = FieldColumnRelationMapperFactory.getChangeColumnName(clazz.getSimpleName());
        } else {
            if (WsStringUtils.isBlank(table.value())) {
                tableName = FieldColumnRelationMapperFactory.getChangeColumnName(clazz.getSimpleName());
            } else {
                tableName = table.value();
            }
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
                if(fieldColumnRelation.isId()){
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                }else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
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
                    if(annotation instanceof TableField){
                        columnName = ((TableField) annotation).value();
                        getColumn = true;
                    }else if(annotation instanceof TableId){
                        columnName = ((TableId) annotation).value();
                        getColumn = true;
                    } else if(annotation instanceof Column){
                        columnName = ((Column) annotation).name();
                        getColumn = true;
                    }
                }
                if(!getId){
                    if(annotation instanceof TableId){
                        isId = true;
                        getId = true;
                    }else if (annotation instanceof Id) {
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
        boolean isArray = false;
        Class<?> joinClass = field.getType();
        if (WsFieldUtils.classCompare(field.getType(), Collection.class)) {
            String className = field.getGenericType().getTypeName();
            className = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">"));
            try {
                joinClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("不存在的类");
            }
            isArray = true;
        } else if (field.getType().isArray()) {
            String className = field.getGenericType().getTypeName();
            className = className.substring(0, className.length() - 2);
            try {
                joinClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("不存在的类");
            }
            isArray = true;
        }
        analysisClassRelation(joinClass, true);
        FieldJoinClass fieldJoinClass = new FieldJoinClass(isArray, joinClass, field);
        fieldJoinClass.setNickName(field.getName());
        fieldJoinClass.setJoinType(TableJoinType.LEFT_JOIN);
        return fieldJoinClass;
    }

    public boolean isIgnoreField(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        if(tableField != null && !tableField.exist()){
            return false;
        }
        Transient aTransient = field.getAnnotation(Transient.class);
        if (aTransient != null) {
            return true;
        }
        jakarta.persistence.Transient jTransient = field.getAnnotation(jakarta.persistence.Transient.class);
        if(jTransient != null){
            return true;
        }
        return false;
    }
}
