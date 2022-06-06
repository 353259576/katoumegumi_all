package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.common.WsBeanUtils;

import java.util.Collection;

/**
 * sql语句
 */
public class SqlStringModel {

    private final String sql;

    private final Object value;

    //o 空 1 基本类型 2 集合类型 3 数组
    private final int valueType;

    public SqlStringModel(String sql, Object value) {
        this.sql = sql;
        this.value = value;
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
    }

    public String getSql() {
        return sql;
    }

    public Object getValue() {
        return value;
    }

    public int getValueType() {
        return valueType;
    }
}
