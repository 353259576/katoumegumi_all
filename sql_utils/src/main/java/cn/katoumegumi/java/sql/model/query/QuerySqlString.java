package cn.katoumegumi.java.sql.model.query;

import java.util.Objects;

public class QuerySqlString implements QueryElement{

    private String sql;

    private QuerySqlString(String sql){

    }

    public static QuerySqlString of(String sql){
        return new QuerySqlString(sql);
    }

    public String getSql() {
        return sql;
    }

    public QuerySqlString setSql(String sql) {
        this.sql = sql;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuerySqlString)) return false;

        QuerySqlString that = (QuerySqlString) o;
        return Objects.equals(sql, that.sql);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sql);
    }
}
