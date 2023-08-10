package cn.katoumegumi.java.sql.mapper.factory.strategys;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.mapper.model.PropertyObjectColumnJoinRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

import java.util.Optional;

public interface FieldColumnRelationMapperHandleStrategy {


    /**
     * 是否能够使用
     *
     * @return
     */
    boolean canUse();

    /**
     * 判断是否能够处理
     *
     * @param clazz
     * @return
     */
    boolean canHandle(Class<?> clazz);

    /**
     * 获取表名称
     *
     * @param clazz
     * @return
     */
    Optional<PropertyColumnRelationMapper> getTableName(Class<?> clazz);

    /**
     * 是否需要过滤
     *
     * @param beanProperty
     * @return
     */
    boolean isIgnoreField(BeanPropertyModel beanProperty);

    /**
     * 获取列名称
     *
     * @param mainMapper
     * @param beanProperty
     * @return
     */
    Optional<PropertyBaseColumnRelation> getColumnName(PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty);

    /**
     * 获取关联列对应关系
     *
     * @param mainMapper
     * @param joinMapper
     * @param beanProperty
     * @return key为主表列表 value为关联表列
     */
    Optional<PropertyObjectColumnJoinRelation> getJoinRelation(PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty);

//    /**
//     * 处理当前数据
//     * @param clazz
//     * @param allowIncomplete
//     * @return
//     */
//    PropertyColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete);


}
