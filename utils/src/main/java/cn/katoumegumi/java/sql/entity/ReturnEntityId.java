package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.util.List;

public class ReturnEntityId {

    private int hashCode;

    public ReturnEntityId(List<FieldColumnRelation> idList,Object[] valueList){
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
        hashCode = stringBuilder.toString().hashCode();
    }

    public ReturnEntityId(ReturnEntity returnEntity){
        this.hashCode = returnEntity.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ReturnEntityId)){
            return false;
        }
        return obj.hashCode() == this.hashCode();
    }
}
