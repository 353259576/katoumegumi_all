package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.sql.common.NullValue;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;

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
        if (value == null) {
            this.valueType = ValueTypeConstants.NULL_TYPE;
        } else if (WsBeanUtils.isBaseType(value.getClass())) {
            this.valueType = ValueTypeConstants.BASE_VALUE_TYPE;
        } else if (value instanceof Collection) {
            this.valueType = ValueTypeConstants.COLLECTION_TYPE;
        } else if (WsBeanUtils.isArray(value.getClass())) {
            this.valueType = ValueTypeConstants.ARRAY_TYPE;
        } else if (value instanceof NullValue) {
            this.valueType = ValueTypeConstants.NULL_VALUE_MODEL;
        } else {
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
