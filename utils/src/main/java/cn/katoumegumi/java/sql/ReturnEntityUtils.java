package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.entity.ReturnEntity;
import cn.katoumegumi.java.sql.entity.ReturnEntityId;

import java.lang.reflect.Field;
import java.util.*;

public class ReturnEntityUtils {



    public static ReturnEntity getReturnEntity(Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap, Map<String, ReturnEntity> returnEntityMap, ReturnEntity returnEntity,String tableName){
        ReturnEntity entity = getReturnEntity(idReturnEntityMap,returnEntity);
        packageReturnEntity(idReturnEntityMap,returnEntityMap,entity,tableName);
        return entity;
    }


    /**
     * 获取id对应的returnEntity
     *
     * @param idReturnEntityMap
     * @param returnEntityMap
     * @param returnEntity
     * @return
     */
    private static ReturnEntity getReturnEntity(Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap, ReturnEntity returnEntity) {

        FieldColumnRelationMapper fieldColumnRelationMapper = returnEntity.getFieldColumnRelationMapper();
        if (WsListUtils.isNotEmpty(fieldColumnRelationMapper.getIds())) {
            ReturnEntityId returnEntityId = new ReturnEntityId(fieldColumnRelationMapper.getIds(), returnEntity);
            returnEntity.setReturnEntityId(returnEntityId);
            Map<ReturnEntityId, ReturnEntity> map = idReturnEntityMap.computeIfAbsent(fieldColumnRelationMapper.getClazz(), c -> new HashMap<>());
            return map.computeIfAbsent(returnEntityId, id -> {
                returnEntity.setValue(returnEntityToObject(returnEntity));
                return returnEntity;
            });
        } else {
            returnEntity.setValue(returnEntityToObject(returnEntity));
            return returnEntity;
        }
    }


    /**
     * 把returnEntity转换为相应的对象
     *
     * @param returnEntity
     * @return
     */
    private static Object returnEntityToObject(ReturnEntity returnEntity) {
        FieldColumnRelationMapper mapper = returnEntity.getFieldColumnRelationMapper();
        Object o = WsBeanUtils.createObject(mapper.getClazz());
        List<FieldColumnRelation> list = mapper.getIds();
        Object[] values = null;
        boolean haveValue = false;


        values = returnEntity.getIdValueList();
        if(values != null) {
            int length = values.length;
            for (int i = 0; i < length; ++i) {

                Object value = values[i];
                if (value != null) {

                    haveValue = true;

                    FieldColumnRelation fieldColumnRelation = list.get(i);
                    try {
                        if (value instanceof byte[]) {
                            value = new String((byte[]) value);
                        }
                        fieldColumnRelation.getField().set(o, WsBeanUtils.objectToT(value, fieldColumnRelation.getFieldClass()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }



        list = mapper.getFieldColumnRelations();
        values = returnEntity.getColumnValueList();
        if(values != null) {
            int length = values.length;
            for (int i = 0; i < length; ++i) {

                Object value = values[i];
                if (value != null) {

                    haveValue = true;

                    FieldColumnRelation fieldColumnRelation = list.get(i);
                    try {
                        if (value instanceof byte[]) {
                            value = new String((byte[]) value);
                        }
                        fieldColumnRelation.getField().set(o, WsBeanUtils.objectToT(value, fieldColumnRelation.getFieldClass()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (haveValue) {
            return o;
        } else {
            return null;
        }

    }



    /**
     * 包装returnEntity
     *
     * @param idReturnEntityMap
     * @param returnEntityMap
     * @param returnEntity
     * @param tableName
     * @return
     */
    private static ReturnEntity packageReturnEntity(Map<Class<?>, Map<ReturnEntityId, ReturnEntity>> idReturnEntityMap, Map<String, ReturnEntity> returnEntityMap, ReturnEntity returnEntity, String tableName) {
        FieldColumnRelationMapper mapper = returnEntity.getFieldColumnRelationMapper();
        if (WsListUtils.isNotEmpty(mapper.getFieldJoinClasses())) {
            List<FieldJoinClass> fieldJoinClassList = mapper.getFieldJoinClasses();
            //int length = fieldJoinClassList.size();
            Object o = returnEntity.getValue();
            for (FieldJoinClass fieldJoinClass : fieldJoinClassList) {
                String nextTableName = tableName + "." + fieldJoinClass.getNickName();
                ReturnEntity nextEntity = returnEntityMap.get(nextTableName);
                if (nextEntity != null) {
                    nextEntity.setParentReturnEntity(returnEntity);
                    ReturnEntity entity = getReturnEntity(idReturnEntityMap, nextEntity);
                    if (entity.getValue() == null) {
                        continue;
                    }
                    if (fieldJoinClass.isArray()) {
                        if (nextEntity == entity) {
                            Field field = fieldJoinClass.getField();
                            try {
                                Object value = field.get(o);
                                if (value == null) {
                                    List list = new ArrayList();
                                    list.add(entity.getValue());
                                    field.set(o, list);

                                } else {
                                    if (value instanceof Collection) {
                                        ((Collection) value).add(entity.getValue());
                                    }
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (nextEntity == entity) {
                            Field field = fieldJoinClass.getField();
                            try {
                                field.set(o, entity.getValue());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    packageReturnEntity(idReturnEntityMap, returnEntityMap, entity, nextTableName);
                }
            }
        }
        return returnEntity;
    }


}
