package cn.katoumegumi.java.sql.mapper.model;

import cn.katoumegumi.java.common.WsCollectionUtils;

import java.util.*;

/**
 * @author ws
 * 对象数据库列名关系集合
 */
public class PropertyColumnRelationMapper {

    /**
     * 实体名称
     */
    private final String nickName;

    /**
     * 表名
     */
    private final String tableName;

    /**
     * 表对应实体的class
     */
    private final Class<?> clazz;

    /**
     * id
     */
    private final List<PropertyColumnRelation> ids = new ArrayList<>();

    /**
     * 非Id
     */
    private final List<PropertyColumnRelation> propertyColumnRelations = new ArrayList<>();

    /**
     * 关联对象
     */
    private final List<ObjectPropertyJoinRelation> objectPropertyJoinRelations = new ArrayList<>();
    private final Map<String, PropertyColumnRelation> propertyColumnRelationMap = new HashMap<>();

    /**
     * 位置
     */
    private final Map<Object, Integer> locationMap = new HashMap<>();

    /**
     * 当使用tableTemplate时，此对象不为null且为该模板的基类
     */
    private final PropertyColumnRelationMapper baseTemplateMapper;

    public PropertyColumnRelationMapper(String nickName, String tableName, Class<?> clazz) {
        this.nickName = nickName;
        this.tableName = tableName;
        this.clazz = clazz;
        this.baseTemplateMapper = null;
    }

    public PropertyColumnRelationMapper(String nickName, String tableName, Class<?> clazz, PropertyColumnRelationMapper mapper) {
        this.nickName = nickName;
        this.tableName = tableName;
        this.clazz = clazz;
        this.baseTemplateMapper = mapper;
    }


    public PropertyColumnRelation getFieldColumnRelationByColumn(String column) {
        for (PropertyColumnRelation propertyColumnRelation : ids) {
            if (propertyColumnRelation.getColumnName().equals(column)) {
                return propertyColumnRelation;
            }
        }
        for (PropertyColumnRelation propertyColumnRelation : propertyColumnRelations) {
            if (propertyColumnRelation.getColumnName().equals(column)) {
                return propertyColumnRelation;
            }
        }
        return null;
    }

    public PropertyColumnRelation getFieldColumnRelationByFieldName(String fieldName) {
        PropertyColumnRelation propertyColumnRelation = propertyColumnRelationMap.get(fieldName);
        if (propertyColumnRelation == null) {
            throw new IllegalArgumentException("未发现对象含有属性：" + fieldName);
        }
        return propertyColumnRelation;
    }

    public PropertyColumnRelation containsFieldColumnRelationByFieldName(String fieldName) {
        return propertyColumnRelationMap.get(fieldName);
    }


    public ObjectPropertyJoinRelation getFieldJoinClassByColumn(String column) {
        for (ObjectPropertyJoinRelation objectPropertyJoinRelation : objectPropertyJoinRelations) {
            if (objectPropertyJoinRelation.getAnotherJoinColumn().equals(column)) {
                return objectPropertyJoinRelation;
            }
        }
        return null;
    }

    public ObjectPropertyJoinRelation getFieldJoinClassByFieldName(String fieldName) {
        for (ObjectPropertyJoinRelation objectPropertyJoinRelation : objectPropertyJoinRelations) {
            if (objectPropertyJoinRelation.getNickName().equals(fieldName)) {
                return objectPropertyJoinRelation;
            }
        }
        return null;
    }


    public String getNickName() {
        return nickName;
    }

    public String getTableName() {
        return tableName;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public List<PropertyColumnRelation> getIds() {
        return ids;
    }


    public List<PropertyColumnRelation> getFieldColumnRelations() {
        return propertyColumnRelations;
    }


    public List<ObjectPropertyJoinRelation> getFieldJoinClasses() {
        return objectPropertyJoinRelations;
    }


    /*public Map<String, PropertyColumnRelation> getFieldColumnRelationMap() {
        return fieldColumnRelationMap;
    }*/

    public PropertyColumnRelation putFieldColumnRelationMap(String key, PropertyColumnRelation propertyColumnRelation) {
        return this.propertyColumnRelationMap.put(key, propertyColumnRelation);
    }


    public void markSignLocation() {
        for (int i = 0; i < ids.size(); i++) {
            locationMap.put(ids.get(i), i);
        }
        for (int i = 0; i < propertyColumnRelations.size(); i++) {
            locationMap.put(propertyColumnRelations.get(i), i);
        }
        if (WsCollectionUtils.isNotEmpty(objectPropertyJoinRelations)) {
            for (int i = 0; i < objectPropertyJoinRelations.size(); i++) {
                locationMap.put(objectPropertyJoinRelations.get(i), i);
            }
        }

    }

    public Integer getLocation(Object o) {
        return locationMap.get(o);
    }

    @Override
    public String toString() {
        return nickName + "_" + tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertyColumnRelationMapper)) {
            return false;
        }
        PropertyColumnRelationMapper that = (PropertyColumnRelationMapper) o;
        return Objects.equals(nickName, that.nickName) && Objects.equals(tableName, that.tableName) && Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, tableName, clazz);
    }

    public PropertyColumnRelationMapper getBaseTemplateMapper() {
        return baseTemplateMapper;
    }
}
