package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsListUtils;

import java.util.*;

/**
 * @author ws
 * 对象数据库列名关系集合
 */
public class FieldColumnRelationMapper {

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
    private final List<FieldColumnRelation> ids = new ArrayList<>();

    /**
     * 非Id
     */
    private final List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();

    /**
     * 关联对象
     */
    private final List<FieldJoinClass> fieldJoinClasses = new ArrayList<>();
    private final Map<String, FieldColumnRelation> fieldColumnRelationMap = new HashMap<>();

    /**
     * 位置
     */
    private final Map<Object, Integer> locationMap = new HashMap<>();

    /**
     * 当使用tableTemplate时，此对象不为null且为该模板的基类
     */
    private final FieldColumnRelationMapper baseTemplateMapper;

    public FieldColumnRelationMapper(String nickName, String tableName, Class<?> clazz) {
        this.nickName = nickName;
        this.tableName = tableName;
        this.clazz = clazz;
        this.baseTemplateMapper = null;
    }

    public FieldColumnRelationMapper(String nickName, String tableName, Class<?> clazz, FieldColumnRelationMapper mapper) {
        this.nickName = nickName;
        this.tableName = tableName;
        this.clazz = clazz;
        this.baseTemplateMapper = mapper;
    }


    public FieldColumnRelation getFieldColumnRelationByColumn(String column) {
        for (FieldColumnRelation fieldColumnRelation : ids) {
            if (fieldColumnRelation.getColumnName().equals(column)) {
                return fieldColumnRelation;
            }
        }
        for (FieldColumnRelation fieldColumnRelation : fieldColumnRelations) {
            if (fieldColumnRelation.getColumnName().equals(column)) {
                return fieldColumnRelation;
            }
        }
        return null;
    }

    public FieldColumnRelation getFieldColumnRelationByFieldName(String fieldName) {
        FieldColumnRelation fieldColumnRelation = fieldColumnRelationMap.get(fieldName);
        if (fieldColumnRelation == null) {
            throw new IllegalArgumentException("未发现对象含有属性：" + fieldName);
        }
        return fieldColumnRelation;
    }

    public FieldColumnRelation containsFieldColumnRelationByFieldName(String fieldName) {
        return fieldColumnRelationMap.get(fieldName);
    }


    public FieldJoinClass getFieldJoinClassByColumn(String column) {
        for (FieldJoinClass fieldJoinClass : fieldJoinClasses) {
            if (fieldJoinClass.getAnotherJoinColumn().equals(column)) {
                return fieldJoinClass;
            }
        }
        return null;
    }

    public FieldJoinClass getFieldJoinClassByFieldName(String fieldName) {
        for (FieldJoinClass fieldJoinClass : fieldJoinClasses) {
            if (fieldJoinClass.getNickName().equals(fieldName)) {
                return fieldJoinClass;
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

    public List<FieldColumnRelation> getIds() {
        return ids;
    }


    public List<FieldColumnRelation> getFieldColumnRelations() {
        return fieldColumnRelations;
    }


    public List<FieldJoinClass> getFieldJoinClasses() {
        return fieldJoinClasses;
    }


    /*public Map<String, FieldColumnRelation> getFieldColumnRelationMap() {
        return fieldColumnRelationMap;
    }*/

    public FieldColumnRelation putFieldColumnRelationMap(String key, FieldColumnRelation fieldColumnRelation) {
        return this.fieldColumnRelationMap.put(key, fieldColumnRelation);
    }


    public void markSignLocation() {
        for (int i = 0; i < ids.size(); i++) {
            locationMap.put(ids.get(i), i);
        }
        for (int i = 0; i < fieldColumnRelations.size(); i++) {
            locationMap.put(fieldColumnRelations.get(i), i);
        }
        if (WsListUtils.isNotEmpty(fieldJoinClasses)) {
            for (int i = 0; i < fieldJoinClasses.size(); i++) {
                locationMap.put(fieldJoinClasses.get(i), i);
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
        if (!(o instanceof FieldColumnRelationMapper)) {
            return false;
        }
        FieldColumnRelationMapper that = (FieldColumnRelationMapper) o;
        return Objects.equals(nickName, that.nickName) && Objects.equals(tableName, that.tableName) && Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, tableName, clazz);
    }

    public FieldColumnRelationMapper getBaseTemplateMapper() {
        return baseTemplateMapper;
    }
}
