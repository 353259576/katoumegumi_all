package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.TranslateNameUtils;
import cn.katoumegumi.java.sql.common.SqlCommon;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;

public class OrderByModel implements Condition {

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

    /*@Override
    public SqlStringAndParameters toSqlString() {
        String sql = SQLModelUtils.guardKeyword(column.getAlias()) + SqlCommon.SQL_COMMON_DELIMITER + SQLModelUtils.guardKeyword(column.getColumnName()) + SqlCommon.SPACE + type;
        return new SqlStringAndParameters(sql,null);
    }*/
}
