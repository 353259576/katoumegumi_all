package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.model.TripleEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 已经存在的实体信息
 */
public class ExistEntityInfo {

    private final Map<ReturnEntityId, TripleEntity<Object, Object[],ExistEntityInfo[]>> existMap;

    public ExistEntityInfo() {
        this.existMap = new HashMap<>();
    }

    public Map<ReturnEntityId, TripleEntity<Object, Object[], ExistEntityInfo[]>> getExistMap() {
        return existMap;
    }
}
