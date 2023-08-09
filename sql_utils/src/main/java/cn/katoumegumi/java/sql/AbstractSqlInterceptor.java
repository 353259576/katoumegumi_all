package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

/**
 * sql拦截器
 *
 * @author ws
 */
public interface AbstractSqlInterceptor {

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
     * 插入语句自动填充
     *
     * @return
     */
    default Object insertFill() {
        return null;
    }

    /**
     * 修改语句自动填充
     *
     * @return
     */
    default Object updateFill() {
        return null;
    }

    /**
     * 查询语句自动填充
     *
     * @return
     */
    default Object selectFill() {
        return null;
    }

    /**
     * 需要自动注入的属性名称
     *
     * @return
     */
    String fieldName();


}
