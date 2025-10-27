package cn.katoumegumi.java.sql.interceptor;

public interface BaseInsertSqlInterceptor extends BaseSqlInterceptor{

    /**
     * 插入语句自动填充
     *
     * @return
     */
    default Object insertFill() {
        return null;
    }

}
