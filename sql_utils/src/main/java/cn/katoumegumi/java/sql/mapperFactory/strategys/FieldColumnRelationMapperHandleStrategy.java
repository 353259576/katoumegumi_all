package cn.katoumegumi.java.sql.mapperFactory.strategys;

import cn.katoumegumi.java.common.model.KeyValue;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;

import java.lang.reflect.Field;
import java.util.Optional;

public interface FieldColumnRelationMapperHandleStrategy {


    /**
     * 是否能够使用
     * @return
     */
    boolean canUse();

    /**
     * 判断是否能够处理
     * @param clazz
     * @return
     */
    boolean canHandle(Class<?> clazz);

    /**
     * 获取表名称
     * @param clazz
     * @return
     */
    Optional<FieldColumnRelationMapper> getTableName(Class<?> clazz);

    /**
     * 是否需要过滤
     * @param field
     * @return
     */
    boolean isIgnoreField(Field field);

    /**
     * 获取列名称
     * @param mainMapper
     * @param field
     * @return
     */
    Optional<FieldColumnRelation> getColumnName(FieldColumnRelationMapper mainMapper,Field field);

    /**
     * 获取关联列对应关系
     * @param mainMapper
     * @param joinMapper
     * @param field
     * @return key为主表列表 value为关联表列
     */
    Optional<FieldJoinClass> getJoinRelation(FieldColumnRelationMapper mainMapper,FieldColumnRelationMapper joinMapper,Field field);

    /**
     * 处理当前数据
     * @param clazz
     * @param allowIncomplete
     * @return
     */
    /*FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete);*/


}
