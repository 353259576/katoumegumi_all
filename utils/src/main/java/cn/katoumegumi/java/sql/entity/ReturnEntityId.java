package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.util.Arrays;
import java.util.List;

/**
 * 返回实体的id对象
 * @author ws
 */
public class ReturnEntityId {

    //private String idSign;

    private final Object[] idSigns;

    public ReturnEntityId(List<FieldColumnRelation> idList,ReturnEntity returnEntity){
        int length = idList.size();
        Object[] valueList = returnEntity.getIdValueList();
        ReturnEntity parentEntity = returnEntity.getParentReturnEntity();
        if(parentEntity == null){
            idSigns = new Object[length];
            System.arraycopy(valueList,0,idSigns,0,length);
        }else {
            idSigns = new Object[parentEntity.getReturnEntityId().getIdSigns().length + length];
            System.arraycopy(parentEntity.getReturnEntityId().getIdSigns(),0,idSigns,0,parentEntity.getReturnEntityId().getIdSigns().length);
            System.arraycopy(valueList,0,idSigns,parentEntity.getReturnEntityId().getIdSigns().length,length);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(idSigns);
        //return idSign.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ReturnEntityId)){
            return false;
        }
        if(obj.hashCode() != this.hashCode()){
            return false;
        }
        ReturnEntityId entityId = (ReturnEntityId) obj;
        if(entityId.getIdSigns().length != this.idSigns.length){
            return false;
        }
        Object[] o1 = entityId.getIdSigns();
        int length = idSigns.length;
        for(int i = 0; i < length; i++){
            if(o1[i] != idSigns[i]){
                if(o1[i] == null || idSigns[i] == null){
                    return false;
                }
                if(!o1[i].equals(idSigns[i])){
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
