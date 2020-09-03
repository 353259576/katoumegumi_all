package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.util.List;

public class ReturnEntityId {

    private String idSign;

    public ReturnEntityId(List<FieldColumnRelation> idList,ReturnEntity returnEntity){
        Object[] valueList = returnEntity.getIdValueList();
        StringBuilder stringBuilder = new StringBuilder();
        int length = idList.size();
        for(int i = 0; i < length; i++){
            stringBuilder.append(idList.get(i).hashCode());
            stringBuilder.append(":");
            if(valueList[i] != null){
                stringBuilder.append(valueList[i]);
            }else {
                stringBuilder.append("`");
            }
            stringBuilder.append("_");
        }
        ReturnEntity entity = returnEntity.getParentReturnEntity();
        if (entity != null){
            stringBuilder.append(";");
            stringBuilder.append(entity.getReturnEntityId().getIdSign());
        }
        idSign = stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return idSign.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ReturnEntityId)){
            return false;
        }
        if(obj.hashCode() != this.hashCode()){
            return false;
        }
        return ((ReturnEntityId) obj).getIdSign().equals(this.getIdSign());
    }

    public String getIdSign() {
        return idSign;
    }

    public void setIdSign(String idSign) {
        this.idSign = idSign;
    }
}
