package cn.katoumegumi.java.sql.mapperFactory;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelation;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;
import cn.katoumegumi.java.sql.FieldJoinClass;
import cn.katoumegumi.java.sql.mapperFactory.strategys.*;
import cn.katoumegumi.java.sql.mapperFactory.strategys.FieldColumnRelationMapperHandle.*;
import com.baomidou.mybatisplus.annotation.TableField;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

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

    /**
     * 不同的mapper处理方式
     */
    private static final List<FieldColumnRelationMapperHandleStrategy> FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST = new ArrayList<>();


    private static final FieldColumnRelationMapperFactory FIELD_COLUMN_RELATION_MAPPER_FACTORY = new FieldColumnRelationMapperFactory();

    /**
     * 默认的mapper处理方式
     */
    private static FieldColumnRelationMapperHandleStrategy defaultFieldColumnRelationMapperHandleStrategy;

    static {
        addFieldColumnRelationMapperHandleStrategy(
                new TableTemplateFieldColumnRelationMapperHandleStrategy(FIELD_COLUMN_RELATION_MAPPER_FACTORY)
        );
        addFieldColumnRelationMapperHandleStrategy(
                new JakartaFieldColumnRelationMapperHandleStrategy(FIELD_COLUMN_RELATION_MAPPER_FACTORY)
        );
        addFieldColumnRelationMapperHandleStrategy(
                new HibernateFieldColumnRelationMapperHandleStrategy(FIELD_COLUMN_RELATION_MAPPER_FACTORY)
        );
        addFieldColumnRelationMapperHandleStrategy(
                new MybatisPlusColumnRelationMapperHandleStrategy(FIELD_COLUMN_RELATION_MAPPER_FACTORY)
        );
        defaultFieldColumnRelationMapperHandleStrategy = new DefaultFieldColumnRelationMapperHandleStrategy(FIELD_COLUMN_RELATION_MAPPER_FACTORY);
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
     * @param clazz           class
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
                    FIELD_COLUMN_RELATION_MAPPER_FACTORY.createFieldColumnRelationMapper(c, allowIncomplete);
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


    /**
     * 对没有指定名称的列名自动驼峰更改
     *
     * @param fieldName 对象字段名称
     * @return 返回表列名
     */
    public String getChangeColumnName(String fieldName) {
        return fieldNameChange ? WsStringUtils.camel_case(fieldName) : fieldName;
    }

    /**
     * 获取可以处理当前class的策略
     *
     * @param clazz
     * @return
     */
    public int getCanHandleFieldColumnRelationMapperStrategyIndex(Class<?> clazz) {
        for (int i = 0; i < FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.size(); i++) {
            if (FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.get(i).canHandle(clazz)) {
                return i;
            }
        }
        return -1;
    }


    public <T> Optional<T> getStrategyAndHandle(int startIndex, Function<FieldColumnRelationMapperHandleStrategy, Optional<T>> function) {
        startIndex = startIndex % FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.size();
        int index = startIndex;
        Optional<T> optional;
        do {
            optional = function.apply(FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.get(index));
            if (optional.isPresent()) {
                return optional;
            }
            index = (index + 1) % FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.size();
        } while (index != startIndex);
        if (defaultFieldColumnRelationMapperHandleStrategy != null) {
            return function.apply(defaultFieldColumnRelationMapperHandleStrategy);
        }
        return Optional.empty();
    }

    public FieldColumnRelationMapper getTableName(int startIndex, Class<?> clazz) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getTableName(clazz)).orElse(null);
    }

    public boolean isIgnoreField(int startIndex, Field field) {
        return getStrategyAndHandle(startIndex, strategy -> {
            if (strategy.isIgnoreField(field)) {
                return Optional.of(Boolean.TRUE);
            } else {
                return Optional.empty();
            }
        }).orElse(false);
    }

    public FieldColumnRelation getColumnName(int startIndex, FieldColumnRelationMapper mainMapper, Field field) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getColumnName(mainMapper, field)).orElse(null);
    }

    public FieldJoinClass getJoinRelation(int startIndex, FieldColumnRelationMapper mainMapper, FieldColumnRelationMapper joinMapper, Field field) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getJoinRelation(mainMapper, joinMapper, field)).orElse(null);
    }


    public static FieldColumnRelationMapper putMapper(Class<?> clazz, FieldColumnRelationMapper fieldColumnRelationMapper) {
        return MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
    }

    public static FieldColumnRelationMapper putIncompleteMapper(Class<?> clazz, FieldColumnRelationMapper fieldColumnRelationMapper) {
        return INCOMPLETE_MAPPER_MAP.put(clazz, fieldColumnRelationMapper);
    }

    public static FieldColumnRelationMapper removeIncompleteMapper(Class<?> clazz) {
        return INCOMPLETE_MAPPER_MAP.remove(clazz);
    }

    public static boolean addFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperHandleStrategy strategy) {
        if (strategy.canUse()) {
            FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.add(strategy);
            return true;
        }
        return false;
    }


    public FieldColumnRelationMapper createFieldColumnRelationMapper(Class<?> clazz, boolean allowIncomplete) {
        int startIndex = getCanHandleFieldColumnRelationMapperStrategyIndex(clazz);
        if (startIndex == -1) {
            return null;
        }
        FieldColumnRelationMapper fieldColumnRelationMapper = getTableName(startIndex, clazz);

        Field[] fields = WsFieldUtils.getFieldAll(clazz);
        List<Field> baseTypeFieldList = new ArrayList<>();
        List<Field> joinClassFieldList = new ArrayList<>();

        for (Field field : fields) {
            if (isIgnoreField(startIndex, field)) {
                continue;
            }
            if (WsBeanUtils.isBaseType(field.getType())) {
                baseTypeFieldList.add(field);
            } else {
                joinClassFieldList.add(field);
            }
        }
        if (WsListUtils.isNotEmpty(baseTypeFieldList)) {
            for (Field field : baseTypeFieldList) {
                FieldColumnRelation fieldColumnRelation = getColumnName(startIndex, fieldColumnRelationMapper.getBaseTemplateMapper() == null ? fieldColumnRelationMapper : fieldColumnRelationMapper.getBaseTemplateMapper(), field);
                if (fieldColumnRelation == null) {
                    continue;
                }
                fieldColumnRelationMapper.putFieldColumnRelationMap(field.getName(), fieldColumnRelation);
                if (fieldColumnRelation.isId()) {
                    fieldColumnRelationMapper.getIds().add(fieldColumnRelation);
                } else {
                    fieldColumnRelationMapper.getFieldColumnRelations().add(fieldColumnRelation);
                }
            }
        }

        putIncompleteMapper(clazz, fieldColumnRelationMapper);

        if (WsListUtils.isNotEmpty(joinClassFieldList)) {
            for (Field field : joinClassFieldList) {
                Class<?> joinClass = WsFieldUtils.getClassTypeof(field);
                FieldColumnRelationMapper joinMapper = analysisClassRelation(joinClass, true);
                FieldJoinClass fieldJoinClass = getJoinRelation(startIndex, fieldColumnRelationMapper, joinMapper, field);
                if (fieldJoinClass == null) {
                    continue;
                }
                fieldColumnRelationMapper.getFieldJoinClasses().add(fieldJoinClass);
            }
        }
        fieldColumnRelationMapper.markSignLocation();
        putMapper(clazz, fieldColumnRelationMapper);
        removeIncompleteMapper(clazz);
        return fieldColumnRelationMapper;
    }


}
