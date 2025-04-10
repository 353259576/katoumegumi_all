package cn.katoumegumi.java.sql.mapper.model;

import cn.katoumegumi.java.common.WsCollectionUtils;

import java.util.*;

/**
 * 属性与列的关系
 * @author ws
 */
public class PropertyColumnRelationMapper {

    /**
     * 实体名称
     */
    private final String entityName;

    /**
     * 表名
     */
    private final String tableName;

    /**
     * 表对应实体的class
     */
    private final Class<?> entityClass;

    /**
     * id
     */
    private final List<PropertyBaseColumnRelation> ids = new ArrayList<>();

    /**
     * 非Id
     */
    private final List<PropertyBaseColumnRelation> propertyBaseColumnRelations = new ArrayList<>();

    /**
     * 关联对象
     */
    private final List<PropertyObjectColumnJoinRelation> propertyObjectColumnJoinRelations = new ArrayList<>();

    private final Map<String, PropertyBaseColumnRelation> propertyColumnRelationMap = new HashMap<>();

    /**
     * 位置
     */
    private final Map<Object, Integer> locationMap = new HashMap<>();

    /**
     * 当使用tableTemplate时，此对象不为null且为该模板的基类
     */
    private final PropertyColumnRelationMapper baseTemplateMapper;

    public PropertyColumnRelationMapper(String entityName, String tableName, Class<?> entityClass) {
        this.entityName = entityName;
        this.tableName = tableName;
        this.entityClass = entityClass;
        this.baseTemplateMapper = null;
    }

    public PropertyColumnRelationMapper(String entityName, String tableName, Class<?> entityClass, PropertyColumnRelationMapper mapper) {
        this.entityName = entityName;
        this.tableName = tableName;
        this.entityClass = entityClass;
        this.baseTemplateMapper = mapper;
    }


    public PropertyBaseColumnRelation getFieldColumnRelationByColumn(String column) {
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : ids) {
            if (propertyBaseColumnRelation.getColumnName().equals(column)) {
                return propertyBaseColumnRelation;
            }
        }
        for (PropertyBaseColumnRelation propertyBaseColumnRelation : propertyBaseColumnRelations) {
            if (propertyBaseColumnRelation.getColumnName().equals(column)) {
                return propertyBaseColumnRelation;
            }
        }
        return null;
    }

    public PropertyBaseColumnRelation getFieldColumnRelationByFieldName(String fieldName) {
        PropertyBaseColumnRelation propertyBaseColumnRelation = propertyColumnRelationMap.get(fieldName);
        if (propertyBaseColumnRelation == null) {
            throw new IllegalArgumentException("未发现对象含有属性：" + fieldName);
        }
        return propertyBaseColumnRelation;
    }

    public PropertyBaseColumnRelation containsFieldColumnRelationByFieldName(String fieldName) {
        return propertyColumnRelationMap.get(fieldName);
    }


    public PropertyObjectColumnJoinRelation getFieldJoinClassByColumn(String column) {
        for (PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation : propertyObjectColumnJoinRelations) {
            if (propertyObjectColumnJoinRelation.getJoinTableColumnName().equals(column)) {
                return propertyObjectColumnJoinRelation;
            }
        }
        return null;
    }

    public PropertyObjectColumnJoinRelation getFieldJoinClassByFieldName(String fieldName) {
        for (PropertyObjectColumnJoinRelation propertyObjectColumnJoinRelation : propertyObjectColumnJoinRelations) {
            if (propertyObjectColumnJoinRelation.getJoinEntityPropertyName().equals(fieldName)) {
                return propertyObjectColumnJoinRelation;
            }
        }
        return null;
    }


    public String getEntityName() {
        return entityName;
    }

    public String getTableName() {
        return tableName;
    }


    public Class<?> getClazz() {
        return entityClass;
    }

    public List<PropertyBaseColumnRelation> getIds() {
        return ids;
    }


    public List<PropertyBaseColumnRelation> getFieldColumnRelations() {
        return propertyBaseColumnRelations;
    }


    public List<PropertyObjectColumnJoinRelation> getFieldJoinClasses() {
        return propertyObjectColumnJoinRelations;
    }


    /*public Map<String, PropertyBaseColumnRelation> getFieldColumnRelationMap() {
        return fieldColumnRelationMap;
    }*/

    public PropertyBaseColumnRelation putFieldColumnRelationMap(String key, PropertyBaseColumnRelation propertyBaseColumnRelation) {
        return this.propertyColumnRelationMap.put(key, propertyBaseColumnRelation);
    }


    public void markSignLocation() {
        for (int i = 0; i < ids.size(); i++) {
            locationMap.put(ids.get(i), i);
        }
        for (int i = 0; i < propertyBaseColumnRelations.size(); i++) {
            locationMap.put(propertyBaseColumnRelations.get(i), i);
        }
        if (WsCollectionUtils.isNotEmpty(propertyObjectColumnJoinRelations)) {
            for (int i = 0; i < propertyObjectColumnJoinRelations.size(); i++) {
                locationMap.put(propertyObjectColumnJoinRelations.get(i), i);
            }
        }

    }

    public Integer getLocation(Object o) {
        return locationMap.get(o);
    }

    @Override
    public String toString() {
        return entityName + "_" + tableName;
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
        return Objects.equals(entityName, that.entityName)
                && Objects.equals(tableName, that.tableName)
                && Objects.equals(entityClass, that.entityClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName, tableName, entityClass);
    }

    public PropertyColumnRelationMapper getBaseTemplateMapper() {
        return baseTemplateMapper;
    }
}
