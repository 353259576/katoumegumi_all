package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;

public class OrderByModel {

    private final ColumnBaseEntity column;

    private final String type;

    public OrderByModel(ColumnBaseEntity column, String type) {
        this.column = column;
        this.type = type;
    }

    public ColumnBaseEntity getColumn() {
        return column;
    }

    public String getType() {
        return type;
    }
}
