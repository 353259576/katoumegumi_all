package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.ResultMapIds;

/**
 * 返回数据的单个数据
 * @author ws
 */
public class ReturnColumnEntity {

    private String name;

    private Object value;

    private boolean listType;

    private boolean baseType;


    public ReturnColumnEntity(String name,Object value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ReturnColumnEntity setName(String name) {
        this.name = name;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ReturnColumnEntity setValue(Object value) {
        this.value = value;
        return this;
    }

    public boolean isListType() {
        return listType;
    }

    public ReturnColumnEntity setListType(boolean listType) {
        this.listType = listType;
        return this;
    }

    public boolean isBaseType() {
        return baseType;
    }

    public ReturnColumnEntity setBaseType(boolean baseType) {
        this.baseType = baseType;
        return this;
    }
}
