package cn.katoumegumi.java.http.model;

import cn.katoumegumi.java.common.WsStringUtils;

/**
 * 通常的基本值
 *
 * @author ws
 */
public class ValueEntity extends BaseEntity {

    private Object value;


    public String getStringValue() {
        return WsStringUtils.anyToString(value);
    }

    public Object getValue() {
        return this.value;
    }

    public ValueEntity setValue(Object value) {
        this.value = value;
        return this;
    }
}
