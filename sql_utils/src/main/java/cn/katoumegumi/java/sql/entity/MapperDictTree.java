package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.mapper.model.ObjectPropertyJoinRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

import java.util.HashMap;
import java.util.Map;

public class MapperDictTree {

    private boolean hasArray;

    private final int depth;

    private FieldColumnRelationMapperName currentMapperName;

    //private PropertyColumnRelationMapper currentMapper;

    private Map<Integer, MapperDictTree> childMap;

    private MapperDictTree[] mapperDictTrees;

    private ObjectPropertyJoinRelation[] objectPropertyJoinRelations;


    public MapperDictTree() {
        this.childMap = new HashMap<>();
        this.depth = -1;
    }

    public MapperDictTree(int depth) {
        this.childMap = new HashMap<>();
        this.depth = depth;
    }

    public void add(FieldColumnRelationMapperName fieldColumnRelationMapperName) {
        MapperDictTree current = this;
        for (int mapperName : fieldColumnRelationMapperName.getCompleteNameSplitSignNameList()) {
            int depth = current.getDepth();
            current = current.getChildMap().computeIfAbsent(mapperName, name -> new MapperDictTree(depth + 1));
        }
        current.setCurrentMapperName(fieldColumnRelationMapperName);
        //current.setCurrentMapper(fieldColumnRelationMapperName.getMapper());
    }

    public int getDepth() {
        return depth;
    }

    public MapperDictTree setCurrentMapperName(FieldColumnRelationMapperName currentMapperName) {
        this.currentMapperName = currentMapperName;
        return this;
    }

    public FieldColumnRelationMapperName getCurrentMapperName() {
        return currentMapperName;
    }


    public Map<Integer, MapperDictTree> getChildMap() {
        return childMap;
    }

    public void setChildMap(Map<Integer, MapperDictTree> childMap) {
        this.childMap = childMap;
    }

    /**
     * 检查是否需要合并数组
     *
     * @return
     */
    public boolean checkNeedMergeAndBuild() {
        boolean hasArray = false;
        if (this.getChildMap().isEmpty()) {
            return false;
        }
        for (Map.Entry<Integer, MapperDictTree> integerMapperDictTreeEntry : this.getChildMap().entrySet()) {
            if (checkNeedMergeAndBuild(integerMapperDictTreeEntry.getValue())) {
                hasArray = true;
            }
        }
        this.hasArray = hasArray;
        return hasArray;
    }

    private boolean checkNeedMergeAndBuild(MapperDictTree mapperDictTree) {
        boolean hasArray = false;
        if (mapperDictTree.getChildMap().isEmpty()) {
            return false;
        }
        PropertyColumnRelationMapper parentMapper = mapperDictTree.getCurrentMapperName().getMapper();
        ObjectPropertyJoinRelation objectPropertyJoinRelation;
        mapperDictTree.objectPropertyJoinRelations = new ObjectPropertyJoinRelation[mapperDictTree.getChildMap().size()];
        mapperDictTree.mapperDictTrees = new MapperDictTree[mapperDictTree.getChildMap().size()];
        int index = 0;
        for (Map.Entry<Integer, MapperDictTree> integerMapperDictTreeEntry : mapperDictTree.getChildMap().entrySet()) {
            objectPropertyJoinRelation = parentMapper.getFieldJoinClassByFieldName(integerMapperDictTreeEntry.getValue().getCurrentMapperName().getCompleteNameSplitNameList()[integerMapperDictTreeEntry.getValue().depth]);
            mapperDictTree.objectPropertyJoinRelations[index] = objectPropertyJoinRelation;
            mapperDictTree.mapperDictTrees[index] = integerMapperDictTreeEntry.getValue();
            index++;
            if (objectPropertyJoinRelation != null && objectPropertyJoinRelation.isArray()) {
                hasArray = true;
            }
            if (checkNeedMergeAndBuild(integerMapperDictTreeEntry.getValue())) {
                hasArray = true;
            }
        }
        mapperDictTree.hasArray = hasArray;
        return hasArray;
    }

    public MapperDictTree[] getMapperDictTrees() {
        return mapperDictTrees;
    }

    public ObjectPropertyJoinRelation[] getFieldJoinClasses() {
        return objectPropertyJoinRelations;
    }

    public boolean isHasArray() {
        return hasArray;
    }
}

