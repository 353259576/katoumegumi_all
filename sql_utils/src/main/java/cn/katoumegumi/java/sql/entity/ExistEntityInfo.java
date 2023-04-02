package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.model.KeyValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 已经存在的实体信息
 */
public class ExistEntityInfo {

    private final List<ExistEntityInfo> next;

    private final Map<ReturnEntityId, KeyValue<Object, Object[]>> existMap;

    public ExistEntityInfo(int length) {
        this.next = new ArrayList<>(length);
        this.existMap = new HashMap<>();
    }

    public ExistEntityInfo getNext(int index) {
        return next.get(index);
    }

    public ExistEntityInfo addNext(ExistEntityInfo existEntityInfo) {
        next.add(existEntityInfo);
        return this;
    }

    public int getNextSize() {
        return next.size();
    }

    public Map<ReturnEntityId, KeyValue<Object, Object[]>> getExistMap() {
        return existMap;
    }

}
