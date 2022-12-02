package cn.katoumegumi.java.sql.mapperFactory;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.annotation.TableTemplate;
import cn.katoumegumi.java.sql.common.TableJoinType;
import cn.katoumegumi.java.sql.mapperFactory.strategys.*;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 用于生成FieldColumnRelationMapper
 *
 * @author 星梦苍天
 */
public class FieldColumnRelationMapperFactory {

    /**
     * 缓存实体对应的对象属性与列名的关联
     */
    private static final Map<Class<?>, FieldColumnRelationMapper> MAPPER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, FieldColumnRelationMapper> INCOMPLETE_MAPPER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, CountDownLatch> CLASS_COUNT_DOWN_LATCH_MAP = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(0, 200, 0L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("sqlUtils mapper生成线程");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });

    private static final List<FieldColumnRelationMapperHandleStrategy> fieldColumnRelationMapperHandleStrategyList = new ArrayList<>();

    static {
        fieldColumnRelationMapperHandleStrategyList.add(
                new TableTemplateFieldColumnRelationMapperHandleStrategy()
        );
        fieldColumnRelationMapperHandleStrategyList.add(
                new JakartaFieldColumnRelationMapperHandleStrategy()
        );
        fieldColumnRelationMapperHandleStrategyList.add(
                new HibernateFieldColumnRelationMapperHandleStrategy()
        );
        fieldColumnRelationMapperHandleStrategyList.add(
                new MybatisPlusColumnRelationMapperHandleStrategy()
        );
    }

    /**
     * 是否转换列名
     */
    public static boolean fieldNameChange = true;

    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz) {
        return analysisClassRelation(clazz, false);
    }

    /**
     * 解析实体对象
     *
     * @param clazz class
     * @param allowIncomplete 允许不完整的
     * @return 表与实例映射关系
     */
    public static FieldColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        FieldColumnRelationMapper fieldColumnRelationMapper = MAPPER_MAP.get(clazz);
        if (fieldColumnRelationMapper != null) {
            return fieldColumnRelationMapper;
        }
        if (allowIncomplete) {
            fieldColumnRelationMapper = INCOMPLETE_MAPPER_MAP.get(clazz);
            if (fieldColumnRelationMapper != null) {
                return fieldColumnRelationMapper;
            }
        }
        CountDownLatch countDownLatch = CLASS_COUNT_DOWN_LATCH_MAP.computeIfAbsent(clazz, c -> {
            CountDownLatch cdl = new CountDownLatch(1);
            EXECUTOR_SERVICE.execute(() -> {
                try {

                    FieldColumnRelationMapper mapper = null;
                    for (FieldColumnRelationMapperHandleStrategy fieldColumnRelationMapperHandleStrategy : fieldColumnRelationMapperHandleStrategyList) {
                        if(fieldColumnRelationMapperHandleStrategy.canHandle(clazz)){
                            mapper = fieldColumnRelationMapperHandleStrategy.analysisClassRelation(clazz,allowIncomplete);
                            break;
                        }
                    }
                    if(mapper == null){
                        putMapper(clazz,null);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    CLASS_COUNT_DOWN_LATCH_MAP.remove(c);
                    cdl.countDown();
                }


            });

            return cdl;
        });
        try {
            boolean k = countDownLatch.await(3, TimeUnit.MINUTES);
            if (k) {
                fieldColumnRelationMapper = MAPPER_MAP.get(clazz);
                if (fieldColumnRelationMapper == null) {
                    throw new RuntimeException("解析失败");
                }
                return fieldColumnRelationMapper;
            } else {
                throw new RuntimeException("解析超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("程序异常中断");
        }
    }


    private static boolean ignoreField(Field field) {
        jakarta.persistence.Transient aTransient = field.getAnnotation(jakarta.persistence.Transient.class);
        if (aTransient != null) {
            return true;
        }
        javax.persistence.Transient jTransient = field.getAnnotation(javax.persistence.Transient.class);
        if(jTransient != null){
            return true;
        }
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null && !tableField.exist();
    }

    /**
     * 对没有指定名称的列名自动驼峰更改
     *
     * @param fieldName 对象字段名称
     * @return 返回表列名
     */
    public static String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
    }


    public static FieldColumnRelationMapper putMapper(Class<?> clazz,FieldColumnRelationMapper fieldColumnRelationMapper){
        return MAPPER_MAP.put(clazz,fieldColumnRelationMapper);
    }

    public static FieldColumnRelationMapper putIncompleteMapper(Class<?> clazz,FieldColumnRelationMapper fieldColumnRelationMapper){
        return INCOMPLETE_MAPPER_MAP.put(clazz,fieldColumnRelationMapper);
    }

    public static FieldColumnRelationMapper removeIncompleteMapper(Class<?> clazz){
        return INCOMPLETE_MAPPER_MAP.remove(clazz);
    }




}
