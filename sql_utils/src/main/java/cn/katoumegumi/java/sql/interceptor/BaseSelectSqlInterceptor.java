package cn.katoumegumi.java.sql.interceptor;

public interface BaseSelectSqlInterceptor extends BaseSqlInterceptor {

    /**
     * 查询语句自动填充
     *
     * @return
     */
    default Object selectFill() {
        return null;
    }

}
