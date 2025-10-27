package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.interceptor.BaseInsertSqlInterceptor;
import cn.katoumegumi.java.sql.interceptor.BaseSelectSqlInterceptor;
import cn.katoumegumi.java.sql.interceptor.BaseSqlInterceptor;
import cn.katoumegumi.java.sql.interceptor.BaseUpdateSqlInterceptor;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

/**
 * sql拦截器
 *
 * @author ws
 */
public interface AbstractSqlInterceptor extends BaseInsertSqlInterceptor, BaseUpdateSqlInterceptor, BaseSelectSqlInterceptor {

    /**
     * 是否在查询语句中起作用
     *
     * @return
     */
    default boolean isSelect() {
        return false;
    }

    /**
     * 是否在修改语句中起作用
     *
     * @return
     */
    default boolean isInsert() {
        return false;
    }

    /**
     * 是否在修改语句中起作用
     *
     * @return
     */
    default boolean isUpdate() {
        return false;
    }


}
