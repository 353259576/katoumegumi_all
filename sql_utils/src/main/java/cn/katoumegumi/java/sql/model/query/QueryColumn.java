package cn.katoumegumi.java.sql.model.query;

public class QueryColumn {

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
        return new QueryColumn(path, name);
    }

    public static QueryColumn of(String name) {
        return new QueryColumn(null, name);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
