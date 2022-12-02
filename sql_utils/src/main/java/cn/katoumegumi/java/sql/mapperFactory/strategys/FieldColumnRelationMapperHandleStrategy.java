package cn.katoumegumi.java.sql.mapperFactory.strategys;

import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;

import java.lang.reflect.Field;

public interface FieldColumnRelationMapperHandleStrategy {

    /**
     * 判断是否能够处理
     * @param clazz
     * @return
     */
    boolean canHandle(Class<?> clazz);

    /**
     * 处理当前数据
     * @param clazz
     * @param allowIncomplete
     * @return
     */
    FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete);


}
