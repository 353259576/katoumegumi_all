package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.common.WsBeanUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlStringAndParameters {

    private final boolean onlyValue;

    private final String sql;

    private final Object value;

    private final int placeholderNum;

    private final int valueType;


    public SqlStringAndParameters(String sql, Object value, int placeholderNum, int valueType) {
        this.sql = sql;
        this.value = value;
        this.placeholderNum = placeholderNum;
        this.valueType = valueType;
        this.onlyValue = this.sql == null;

    }

    public SqlStringAndParameters(String sql, Object value, int placeholderNum) {
        this.sql = sql;
        this.value = value;
        this.placeholderNum = placeholderNum;
        if(value == null){
            this.valueType = 0;
        }else if (WsBeanUtils.isBaseType(value.getClass())){
            this.valueType = 1;
        }else if (value instanceof Collection){
            this.valueType = 2;
        }else if(WsBeanUtils.isArray(value.getClass())){
            this.valueType = 3;
        }else {
            throw new IllegalArgumentException("不支持的类:" + value.getClass());
        }
        this.onlyValue = this.sql == null;
    }

    public SqlStringAndParameters(String sql, Object value) {
        this.sql = sql;
        this.value = value;
        if(value == null){
            this.valueType = 0;
            this.placeholderNum = 0;
        }else if (WsBeanUtils.isBaseType(value.getClass())){
            this.valueType = 1;
            this.placeholderNum = 1;
        }else if (value instanceof Collection){
            this.valueType = 2;
            this.placeholderNum = ((Collection<?>)value).size();
        }else if(WsBeanUtils.isArray(value.getClass())){
            this.valueType = 3;
            this.placeholderNum = ((Object[])value).length;
        }else {
            throw new IllegalArgumentException("不支持的类:" + value.getClass());
        }
        this.onlyValue = this.sql == null;
    }

    public String getSql() {
        return sql;
    }

    public Object getValue() {
        return value;
    }

    public int getPlaceholderNum() {
        return placeholderNum;
    }

    public int getValueType() {
        return valueType;
    }

    public boolean isOnlyValue() {
        return onlyValue;
    }
}
