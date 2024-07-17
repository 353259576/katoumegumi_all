package cn.katoumegumi.java.sql.model.query;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;

import java.util.Objects;

public class QueryColumn implements QueryElement {

    /**
     * 路径
     */
    private final String path;

    /**
     * 名称
     */
    private final String name;

    private QueryColumn(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public static QueryColumn of(String path, String name) {
        if (WsStringUtils.isBlank(name)){
            throw new IllegalArgumentException("columnName is null or empty");
        }
        if (WsStringUtils.isBlank(path)){
            return new QueryColumn(null,name);
        }
        return new QueryColumn(path, name);
    }

    public static QueryColumn of(String path, SFunction<?, ?> columnName) {
        if (columnName==null){
            throw new IllegalArgumentException("columnName is null");
        }
        return new QueryColumn(path, WsReflectUtils.getFieldName(columnName));
    }

    public static QueryColumn of(String name) {
        if (WsStringUtils.isBlank(name)){
            throw new IllegalArgumentException("columnName is null or empty");
        }
        return new QueryColumn(null, name);
    }

    public static QueryColumn of(SFunction<?, ?> columnName) {
        if (columnName==null){
            throw new IllegalArgumentException("columnName is null");
        }
        return new QueryColumn(null, WsReflectUtils.getFieldName(columnName));
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "QueryColumn{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryColumn)) return false;
        QueryColumn that = (QueryColumn) o;
        return Objects.equals(name, that.name) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(path);
        result = 31 * result + Objects.hashCode(name);
        return result;
    }
}
