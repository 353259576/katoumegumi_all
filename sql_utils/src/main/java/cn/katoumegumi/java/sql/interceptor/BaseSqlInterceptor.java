package cn.katoumegumi.java.sql.interceptor;

import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

public interface BaseSqlInterceptor {

    /**
     * 使用条件
     *
     * @param propertyColumnRelationMapper
     * @return
     */
    default boolean useCondition(PropertyColumnRelationMapper propertyColumnRelationMapper) {
        return true;
    }

    /**
     * 需要自动注入的属性名称
     *
     * @return
     */
    String fieldName();
}
