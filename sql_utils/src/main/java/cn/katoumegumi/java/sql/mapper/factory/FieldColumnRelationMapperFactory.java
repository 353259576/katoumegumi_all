package cn.katoumegumi.java.sql.mapper.factory;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsCollectionUtils;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandle.*;
import cn.katoumegumi.java.sql.mapper.factory.strategys.FieldColumnRelationMapperHandleStrategy;
import cn.katoumegumi.java.sql.mapper.model.ObjectPropertyJoinRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * 用于生成FieldColumnRelationMapper
 *
 * @author 星梦苍天
 */
public class FieldColumnRelationMapperFactory {

    private static final Logger log = Logger.getLogger(FieldColumnRelationMapperFactory.class.getName());

    /**
     * 缓存实体对应的对象属性与列名的关联
     */
    private static final Map<Class<?>, PropertyColumnRelationMapper> MAPPER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class<?>, PropertyColumnRelationMapper> INCOMPLETE_MAPPER_MAP = new ConcurrentHashMap<>();

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
    private static final FieldColumnRelationMapperHandleStrategy defaultFieldColumnRelationMapperHandleStrategy;

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

    public static PropertyColumnRelationMapper analysisClassRelation(Class<?> clazz) {
        return analysisClassRelation(clazz, false);
    }

    /**
     * 解析实体对象
     *
     * @param clazz           class
     * @param allowIncomplete 允许不完整的
     * @return 表与实例映射关系
     */
    public static PropertyColumnRelationMapper analysisClassRelation(Class<?> clazz, boolean allowIncomplete) {
        PropertyColumnRelationMapper propertyColumnRelationMapper = MAPPER_MAP.get(clazz);
        if (propertyColumnRelationMapper != null) {
            return propertyColumnRelationMapper;
        }
        if (allowIncomplete) {
            propertyColumnRelationMapper = INCOMPLETE_MAPPER_MAP.get(clazz);
            if (propertyColumnRelationMapper != null) {
                return propertyColumnRelationMapper;
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
                propertyColumnRelationMapper = MAPPER_MAP.get(clazz);
                if (propertyColumnRelationMapper == null) {
                    throw new RuntimeException("解析失败,无法解析：" + clazz);
                }
                return propertyColumnRelationMapper;
            } else {
                throw new RuntimeException("解析超时,无法解析：" + clazz);
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

    public PropertyColumnRelationMapper getTableName(int startIndex, Class<?> clazz) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getTableName(clazz)).orElse(null);
    }

    public boolean isIgnoreField(int startIndex, BeanPropertyModel beanProperty) {
        return getStrategyAndHandle(startIndex, strategy -> {
            if (strategy.isIgnoreField(beanProperty)) {
                return Optional.of(Boolean.TRUE);
            } else {
                return Optional.empty();
            }
        }).orElse(false);
    }

    public PropertyColumnRelation getColumnName(int startIndex, PropertyColumnRelationMapper mainMapper, BeanPropertyModel beanProperty) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getColumnName(mainMapper, beanProperty)).orElse(null);
    }

    public ObjectPropertyJoinRelation getJoinRelation(int startIndex, PropertyColumnRelationMapper mainMapper, PropertyColumnRelationMapper joinMapper, BeanPropertyModel beanProperty) {
        return getStrategyAndHandle(startIndex, strategy -> strategy.getJoinRelation(mainMapper, joinMapper, beanProperty)).orElse(null);
    }


    public static PropertyColumnRelationMapper putMapper(Class<?> clazz, PropertyColumnRelationMapper propertyColumnRelationMapper) {
        return MAPPER_MAP.put(clazz, propertyColumnRelationMapper);
    }

    public static PropertyColumnRelationMapper putIncompleteMapper(Class<?> clazz, PropertyColumnRelationMapper propertyColumnRelationMapper) {
        return INCOMPLETE_MAPPER_MAP.put(clazz, propertyColumnRelationMapper);
    }

    public static PropertyColumnRelationMapper removeIncompleteMapper(Class<?> clazz) {
        return INCOMPLETE_MAPPER_MAP.remove(clazz);
    }

    public static boolean addFieldColumnRelationMapperHandleStrategy(FieldColumnRelationMapperHandleStrategy strategy) {
        if (strategy.canUse()) {
            FIELD_COLUMN_RELATION_MAPPER_HANDLE_STRATEGY_LIST.add(strategy);
            return true;
        }
        return false;
    }


    public PropertyColumnRelationMapper createFieldColumnRelationMapper(Class<?> clazz, boolean allowIncomplete) {
        int startIndex = getCanHandleFieldColumnRelationMapperStrategyIndex(clazz);
        if (startIndex == -1) {
            return null;
        }
        PropertyColumnRelationMapper propertyColumnRelationMapper = getTableName(startIndex, clazz);

        //Field[] fields = WsReflectUtils.getFieldAll(clazz);

        BeanModel beanModel = WsReflectUtils.createBeanModel(clazz);

        List<BeanPropertyModel> baseTypeFieldList = new ArrayList<>();
        List<BeanPropertyModel> joinClassFieldList = new ArrayList<>();

        for (Map.Entry<String, BeanPropertyModel> entry : beanModel.getPropertyModelMap().entrySet()) {
            BeanPropertyModel beanPropertyModel = entry.getValue();
            if (isIgnoreField(startIndex, beanPropertyModel)) {
                continue;
            }
            if (WsBeanUtils.isBaseType(beanPropertyModel.getPropertyClass())) {
                baseTypeFieldList.add(beanPropertyModel);
            } else {
                joinClassFieldList.add(beanPropertyModel);
            }
        }
        if (WsCollectionUtils.isNotEmpty(baseTypeFieldList)) {
            for (BeanPropertyModel propertyModel : baseTypeFieldList) {
                PropertyColumnRelation propertyColumnRelation = getColumnName(startIndex, propertyColumnRelationMapper.getBaseTemplateMapper() == null ? propertyColumnRelationMapper : propertyColumnRelationMapper.getBaseTemplateMapper(), propertyModel);
                if (propertyColumnRelation == null) {
                    continue;
                }
                propertyColumnRelationMapper.putFieldColumnRelationMap(propertyModel.getPropertyName(), propertyColumnRelation);
                if (propertyColumnRelation.isId()) {
                    propertyColumnRelationMapper.getIds().add(propertyColumnRelation);
                } else {
                    propertyColumnRelationMapper.getFieldColumnRelations().add(propertyColumnRelation);
                }
            }
        }

        putIncompleteMapper(clazz, propertyColumnRelationMapper);

        if (WsCollectionUtils.isNotEmpty(joinClassFieldList)) {
            for (BeanPropertyModel propertyModel : joinClassFieldList) {
                Class<?> joinClass;
                if (WsReflectUtils.isArrayType(propertyModel.getPropertyClass())){
                    joinClass = propertyModel.getGenericClass();
                }else {
                    joinClass = propertyModel.getPropertyClass();
                }
                if (joinClass == null){
                    continue;
                }
                try {
                    PropertyColumnRelationMapper joinMapper = analysisClassRelation(joinClass, true);
                    ObjectPropertyJoinRelation objectPropertyJoinRelation = getJoinRelation(startIndex, propertyColumnRelationMapper, joinMapper, propertyModel);
                    if (objectPropertyJoinRelation == null) {
                        continue;
                    }
                    propertyColumnRelationMapper.getFieldJoinClasses().add(objectPropertyJoinRelation);
                }catch (RuntimeException e){
                    log.info("解析"+ joinClass +"失败:"+e.getMessage());
                }
            }
        }
        propertyColumnRelationMapper.markSignLocation();
        putMapper(clazz, propertyColumnRelationMapper);
        removeIncompleteMapper(clazz);
        return propertyColumnRelationMapper;
    }


}
