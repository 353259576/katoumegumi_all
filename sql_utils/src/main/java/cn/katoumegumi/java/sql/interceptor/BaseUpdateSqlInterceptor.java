package cn.katoumegumi.java.sql.interceptor;

public interface BaseUpdateSqlInterceptor extends BaseSqlInterceptor{

    /**
     * 修改语句自动填充
     *
     * @return
     */
    default Object updateFill() {
        return null;
    }


}
