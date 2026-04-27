package cn.katoumegumi.java.sql.interceptor;

import cn.katoumegumi.java.sql.AbstractSqlInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SQlInterceptorRegistry {
    /**
     * 插入参数注入
     */
    private static final Map<String, BaseInsertSqlInterceptor> INSERT_SQL_INTERCEPTOR_MAP = new HashMap<>();
    /**
     * 修改参数注入
     */
    private static final Map<String, BaseUpdateSqlInterceptor> UPDATE_SQL_INTERCEPTOR_MAP = new HashMap<>();
    /**
     * 查询参数注入
     */
    private static final Map<String, BaseSelectSqlInterceptor> SELECT_SQL_INTERCEPTOR_MAP = new HashMap<>();


    /**
     * 添加SQL拦截器
     * @param sqlInterceptor
     * @return
     */
    public synchronized  boolean addSqlInterceptor(BaseSqlInterceptor sqlInterceptor) {
        if (sqlInterceptor == null) {
            return false;
        }
        boolean ans = false;
        if (sqlInterceptor instanceof BaseInsertSqlInterceptor) {
            handleInterceptorRegistration(sqlInterceptor,BaseInsertSqlInterceptor.class, INSERT_SQL_INTERCEPTOR_MAP, AbstractSqlInterceptor::isInsert);
            ans = true;
        }
        if (sqlInterceptor instanceof BaseUpdateSqlInterceptor) {
            handleInterceptorRegistration(sqlInterceptor,BaseUpdateSqlInterceptor.class, UPDATE_SQL_INTERCEPTOR_MAP, AbstractSqlInterceptor::isUpdate);
            ans = true;
        }
        if (sqlInterceptor instanceof BaseSelectSqlInterceptor) {
            handleInterceptorRegistration(sqlInterceptor,BaseSelectSqlInterceptor.class, SELECT_SQL_INTERCEPTOR_MAP, AbstractSqlInterceptor::isSelect);
            ans = true;
        }
        return ans;
    }


    /**
     * 处理拦截器注册的通用方法
     * 
     * @param sqlInterceptor SQL拦截器实例
     * @param interceptorType 拦截器类型Class对象
     * @param map 存储拦截器的Map
     * @param function 用于检查拦截器是否可用的函数
     * @param <T> 拦截器泛型
     */
    private <T> void handleInterceptorRegistration(BaseSqlInterceptor sqlInterceptor,Class<T> interceptorType, Map<String,T> map, Function<AbstractSqlInterceptor,Boolean> function){
        if (sqlInterceptor == null) {
            return;
        }
        // 创建拦截器的代理实例
        T t = (T) Proxy.newProxyInstance(SQlInterceptorRegistry.class.getClassLoader(), new Class[]{interceptorType}, (proxy, method, args)->method.invoke(sqlInterceptor,args));
        // 如果是AbstractSqlInterceptor类型，需要额外检查是否启用
        if (sqlInterceptor instanceof AbstractSqlInterceptor){
            if (!function.apply((AbstractSqlInterceptor) sqlInterceptor)){
                return;
            }
        }
        // 将代理实例存入对应的Map中
        map.put(sqlInterceptor.fieldName(),t);
    }



    /**
     * 根据字段名获取插入SQL拦截器
     *
     * @param fieldName 字段名称
     * @return 对应的插入SQL拦截器，如果不存在则返回null
     */
    public BaseInsertSqlInterceptor getInsertSqlInterceptor(String fieldName) {
        return INSERT_SQL_INTERCEPTOR_MAP.get(fieldName);
    }

    /**
     * 根据字段名获取更新SQL拦截器
     * 
     * @param fieldName 字段名称
     * @return 对应的更新SQL拦截器，如果不存在则返回null
     */
    public BaseUpdateSqlInterceptor getUpdateSqlInterceptor(String fieldName) {
        return UPDATE_SQL_INTERCEPTOR_MAP.get(fieldName);
    }

    /**
     * 根据字段名获取查询SQL拦截器
     *
     * @param fieldName 字段名称
     * @return 对应的查询SQL拦截器，如果不存在则返回null
     */
    public BaseSelectSqlInterceptor getSelectSqlInterceptor(String fieldName) {
        return SELECT_SQL_INTERCEPTOR_MAP.get(fieldName);
    }

}
