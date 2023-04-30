package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.util.Arrays;
import java.util.List;

/**
 * 返回实体的id对象
 *
 * @author ws
 */
public class ReturnEntityId {

    private final Object[] idSigns;

    private final int hashCode;

    public ReturnEntityId(Object[] idSigns) {
        this.idSigns = idSigns;
        hashCode = Arrays.hashCode(idSigns);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.hashCode() != this.hashCode()) {
            return false;
        }
        if (!(obj instanceof ReturnEntityId)) {
            return false;
        }
        ReturnEntityId entityId = (ReturnEntityId) obj;
        int length = idSigns.length;
        if (entityId.getIdSigns().length != length) {
            return false;
        }
        Object[] objIdSigns = entityId.getIdSigns();
        for (int i = 0; i < length; i++) {
            if (objIdSigns[i] != idSigns[i]) {
                if (objIdSigns[i] == null || !objIdSigns[i].equals(idSigns[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public Object[] getIdSigns() {
        return idSigns;
    }
}
