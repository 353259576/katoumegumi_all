package cn.katoumegumi.java.http.model;

import java.io.Serializable;

/**
 * 基础参数实体
 *
 * @author ws
 */
public abstract class BaseEntity implements Serializable, Comparable<BaseEntity> {

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
        if (obj == null) {
            return false;
        }
        if(!(obj instanceof String)){
            return false;
        }
        if (name == null) {
            return false;
        }

        return name.equals(obj);
    }

    @Override
    public int compareTo(BaseEntity o) {
        return name.compareTo(o.getName());
    }
}
