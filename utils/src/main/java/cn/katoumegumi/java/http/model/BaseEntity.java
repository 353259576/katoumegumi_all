package cn.katoumegumi.java.http.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 基础参数实体
 * @author ws
 */
public abstract class BaseEntity implements Serializable,Comparable<BaseEntity> {

    protected String name;


    public String getName() {
        return name;
    }

    public BaseEntity setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(name == null){
            return false;
        }
        if(obj == null){
            return false;
        }
        return name.equals(obj);
    }

    @Override
    public int compareTo(@NotNull BaseEntity o) {
        return name.compareTo(o.getName());
    }
}
