package cn.katoumegumi.java.sql;

/**
 * sql拦截器
 *
 * @author ws
 */
public interface AbstractSqlInterceptor {

    //private final Map<Class<?>, FieldColumnRelation> classFieldMap = new ConcurrentHashMap<>();

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
     * @param fieldColumnRelationMapper
     * @return
     */
    default boolean useCondition(FieldColumnRelationMapper fieldColumnRelationMapper) {
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
    public String fieldName();


    /*protected void addClassFieldName(Class<?> tClass, FieldColumnRelation fieldColumnRelation) {
        classFieldMap.put(tClass, fieldColumnRelation);
    }

    public FieldColumnRelation getFieldColumnRelation(Class<?> tClass) {
        return classFieldMap.get(tClass);
    }*/


}
