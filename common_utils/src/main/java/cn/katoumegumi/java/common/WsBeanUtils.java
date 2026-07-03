package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.convert.ConvertUtils;
import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bean工具类
 *
 * @author ws
 */
@SuppressWarnings("unchecked")
public class WsBeanUtils {

    /**
     * 无参构造缓存。value 为对应类的无参 {@link Constructor}；{@link #NO_CTOR_MARKER}
     * 使用 {@link ConcurrentHashMap} 稳定持有，避免热路径反复反射查询。
     */
    private static final ConcurrentHashMap<Class<?>, Constructor<?>> NO_ARG_CTOR_CACHE = new ConcurrentHashMap<>();

    /** 命中失败的标记值，避免对同一无构造类反复执行 getDeclaredConstructor */
    private static final Constructor<?> NO_CTOR_MARKER;

    /**
     * convertBean 递归调用栈（基于对象 identity）。用于检测 A→B→A 循环引用，
     * 命中时直接返回 null 断开环，避免 StackOverflowError。
     * 使用 {@link IdentityHashMap} 以引用相等而非 equals 判定，避免重写了
     * equals/hashCode 的 bean 误判。
     */
    private static final ThreadLocal<Set<Object>> CONVERT_BEAN_STACK = ThreadLocal.withInitial(
            () -> Collections.newSetFromMap(new IdentityHashMap<>()));

    static {
        try {
            NO_CTOR_MARKER = Void.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            // Void 一定有无参 private 构造，理论上不会到这里
            throw new IllegalStateException(e);
        }
    }

    private static Constructor<?> findNoArgConstructor(Class<?> clazz) {
        Constructor<?> cached = NO_ARG_CTOR_CACHE.get(clazz);
        if (cached != null) {
            return cached;
        }
        Constructor<?> resolved;
        try {
            resolved = clazz.getDeclaredConstructor();
            resolved.setAccessible(true);
        } catch (NoSuchMethodException e) {
            resolved = NO_CTOR_MARKER;
        }
        Constructor<?> prev = NO_ARG_CTOR_CACHE.putIfAbsent(clazz, resolved);
        if (prev != null) {
            return prev;
        }
        return resolved;
    }

    /**
     * 基于 {@link WsReflectUtils#createBeanModel} 转换 JavaBean。
     * <p>规则：
     * <ol>
     *   <li>源与目标属性名一致即尝试赋值；</li>
     *   <li>目标是基本类型（含包装类、String、日期等）走 {@link ConvertUtils#convert(Object, Class)}；</li>
     *   <li>目标是数组/集合走 {@link #convertToArray}/{@link #convertToList}，元素类型取自目标属性泛型；</li>
     *   <li>目标是 {@link Map} 时按 K/V 泛型逐项转换；</li>
     *   <li>目标为普通 JavaBean 时递归调用本方法。</li>
     * </ol>
     *
     * @param o      源对象
     * @param tClass 目标类型
     * @param <T>    目标泛型
     * @return 转换后的实例，无法转换返回 null
     */
    public static <T> T convertBean(Object o, Class<T> tClass) {
        if (o == null) {
            return null;
        }
        if (isArray(o.getClass())) {
            throw new IllegalArgumentException("不支持直接转数组");
        }
        if (tClass == null || tClass.isInterface()) {
            throw new IllegalArgumentException("不支持转换为接口");
        }
        if (isBaseType(o.getClass())) {
            return baseTypeConvert(o, tClass);
        }
        if (o instanceof Map) {
            // Map 不支持作为源进行 bean 转换
            return null;
        }

        // 循环引用检测：若当前对象已在转换栈中，说明出现 A→B→A 环，直接断开
        Set<Object> stack = CONVERT_BEAN_STACK.get();
        if (!stack.add(o)) {
            return null;
        }
        try {
            BeanModel sourceModel = WsReflectUtils.createBeanModel(o.getClass());
            BeanModel targetModel = WsReflectUtils.createBeanModel(tClass);
            Map<String, BeanPropertyModel> sourcePmMap = sourceModel.getPropertyModelMap();
            Map<String, BeanPropertyModel> targetPmMap = targetModel.getPropertyModelMap();
            if (sourcePmMap.isEmpty() || targetPmMap.isEmpty()) {
                return null;
            }

            T target = newTargetInstance(tClass);
            if (target == null) {
                return null;
            }

            for (Map.Entry<String, BeanPropertyModel> sourceEntry : sourcePmMap.entrySet()) {
                BeanPropertyModel sourcePm = sourceEntry.getValue();
                BeanPropertyModel targetPm = targetPmMap.get(sourceEntry.getKey());
                if (targetPm == null) {
                    continue;
                }
                Object value = sourcePm.getValue(o);
                if (value == null) {
                    continue;
                }
                Object converted = convertValue(value, targetPm);
                if (converted == null) {
                    continue;
                }
                targetPm.setValue(target, converted);
            }
            return target;
        } finally {
            stack.remove(o);
            if (stack.isEmpty()) {
                CONVERT_BEAN_STACK.remove();
            }
        }
    }

    /**
     * 将源属性值按目标属性类型转换为可写入的值。
     * 走预计算的 {@link BeanPropertyModel.PropertyKind} 快速分派，
     * 避免热路径上的多次 isAssignableFrom/isBaseType 调用。
     */
    private static Object convertValue(Object value, BeanPropertyModel targetPm) {
        Class<?> targetType = targetPm.getPropertyClass();
        if (targetType == null) {
            return null;
        }
        Class<?> valueClass = value.getClass();

        switch (targetPm.getPropertyKind()) {
            case BASE:
                return baseTypeConvert(value, targetType);
            case OBJECT:
                return value;
            case ARRAY:
                if (!isArray(valueClass)) {
                    return null;
                }
                return convertToArray(value, targetType.getComponentType());
            case COLLECTION: {
                Collection<Object> collection = newCollectionInstance(targetType);
                List<Class<?>> list = targetPm.getGenericClass();
                Class<?> tClass;
                if (WsCollectionUtils.isEmpty(list)) {
                    tClass = Object.class;
                }else {
                    tClass = list.get(0);
                }
                return convertToList(value, collection, tClass);
            }
            case MAP: {
                if (!(value instanceof Map)) {
                    return null;
                }
                List<Class<?>> list = targetPm.getGenericClass();
                if (WsCollectionUtils.isEmpty(list)) {
                    return value;
                }
                Class<?> keyClass = list.get(0);
                Class<?> valClass = list.get(1);
                // 同类型 + 无具体泛型约束时直接复用，跳过逐项转换（最高频短路）
                if ((keyClass == null || keyClass == Object.class)
                        && (valClass == null || valClass == Object.class)
                        && targetType.isInstance(value)) {
                    return value;
                }
                return convertToMap(value, targetPm);
            }
            case BEAN:
            default:
                if (isArray(valueClass)) {
                    return null;
                }
                // 同类型或子类型直接复用引用，与原版 baseTypeConvert→ConvertUtils 的
                // c.isInstance(o) 短路语义一致，避免无意义的递归深拷贝
                if (targetType.isAssignableFrom(valueClass)) {
                    return value;
                }
                return convertBean(value, targetType);
        }
    }

    /**
     * 将源 Map 转换为指定键值泛型的目标 Map。
     * <p>仅取目标声明上的第一层泛型（如 {@code Map<String, Bar>}），嵌套 Map 的内层
     * 泛型在运行时不可用，将退化为 {@code Object} 处理。
     */
    @SuppressWarnings("unchecked")
    private static Map<Object, Object> convertToMap(Object value, BeanPropertyModel targetPm) {
        Class<?> targetType = targetPm.getPropertyClass();
        if (!(value instanceof Map)) {
            return null;
        }
        Map<Object, Object> source = (Map<Object, Object>) value;
        Map<Object, Object> target = newMapInstance(targetType);
        List<Class<?>> list = targetPm.getGenericClass();
        Class<?> keyClass = list.get(0);
        Class<?> valueClass = list.get(1);
        // 预计算 K/V 的元素分类，避免每个 entry 重复执行 isBaseType / classCompare
        BeanPropertyModel.PropertyKind keyKind = classifyElementKind(keyClass);
        BeanPropertyModel.PropertyKind valKind = classifyElementKind(valueClass);
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Object srcKey = entry.getKey();
            Object srcVal = entry.getValue();
            Object dstKey = convertMapElement(srcKey, keyClass, keyKind);
            if (dstKey == null && srcKey != null) {
                continue;
            }
            Object dstVal = convertMapElement(srcVal, valueClass, valKind);
            if (dstVal == null && srcVal != null) {
                continue;
            }
            target.put(dstKey, dstVal);
        }
        return target;
    }

//    /** Map 元素的目标类型分类，构造时一次性确定 */
//    private enum ElementKind {
//        /** null 或 Object.class：直接透传 */
//        OBJECT,
//        /** 基本类型/包装类/String/日期等 */
//        BASE,
//        /** Java 数组 */
//        ARRAY,
//        /** Collection 子类型 */
//        COLLECTION,
//        /** Map 子类型 */
//        MAP,
//        /** 普通 JavaBean */
//        BEAN
//    }

    private static BeanPropertyModel.PropertyKind classifyElementKind(Class<?> c) {
        if (c == null || c == Object.class) {
            return BeanPropertyModel.PropertyKind.OBJECT;
        }
        if (isBaseType(c)) {
            return BeanPropertyModel.PropertyKind.BASE;
        }
        if (c.isArray()) {
            return BeanPropertyModel.PropertyKind.ARRAY;
        }
        if (WsReflectUtils.classCompare(c, Collection.class)) {
            return BeanPropertyModel.PropertyKind.COLLECTION;
        }
        if (WsReflectUtils.classCompare(c, Map.class)) {
            return BeanPropertyModel.PropertyKind.MAP;
        }
        return BeanPropertyModel.PropertyKind.BEAN;
    }

    /**
     * 将单个 Map 元素（key 或 value）按预计算的 {@link BeanPropertyModel.PropertyKind} 分派转换。
     */
    private static Object convertMapElement(Object element, Class<?> targetClass, BeanPropertyModel.PropertyKind kind) {
        if (element == null) {
            return null;
        }
        switch (kind) {
            case OBJECT:
                return element;
            case BASE:
                return baseTypeConvert(element, targetClass);
            case ARRAY:
                return isArray(element.getClass())
                        ? convertToArray(element, targetClass.getComponentType())
                        : null;
            case COLLECTION: {
                Collection<Object> coll = newCollectionInstance(targetClass);
                // 元素类型未知，退化为 Object
                return convertToList(element, coll, Object.class);
            }
            case MAP: {
                if (!(element instanceof Map)) {
                    return null;
                }
                Map<Object, Object> nested = newMapInstance(targetClass);
                nested.putAll((Map<?, ?>) element);
                return nested;
            }
            case BEAN:
            default:
                if (isArray(element.getClass())) {
                    return null;
                }
                if (targetClass.isAssignableFrom(element.getClass())) {
                    return element;
                }
                return convertBean(element, targetClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T newTargetInstance(Class<T> tClass) {
        Constructor<?> ctor = findNoArgConstructor(tClass);
        if (ctor == NO_CTOR_MARKER) {
            return (T) WsUnsafeUtils.allocateInstance(tClass);
        }
        try {
            return (T) ctor.newInstance();
        } catch (Exception e) {
            return (T) WsUnsafeUtils.allocateInstance(tClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollectionInstance(Class<?> collectionType) {
        if (collectionType == List.class
                || collectionType == Collection.class
                || collectionType == ArrayList.class) {
            return new ArrayList<>();
        }
        if (collectionType == Set.class
                || collectionType == HashSet.class) {
            return new HashSet<>();
        }
        Constructor<?> ctor = findNoArgConstructor(collectionType);
        if (ctor == NO_CTOR_MARKER) {
            return new ArrayList<>();
        }
        try {
            return (Collection<Object>) ctor.newInstance();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> newMapInstance(Class<?> mapType) {
        if (mapType == Map.class
                || mapType == HashMap.class) {
            return new HashMap<>();
        }
        if (mapType == java.util.LinkedHashMap.class) {
            return new java.util.LinkedHashMap<>();
        }
        if (mapType == java.util.TreeMap.class) {
            return new java.util.TreeMap<>();
        }
        Constructor<?> ctor = findNoArgConstructor(mapType);
        if (ctor == NO_CTOR_MARKER) {
            return new HashMap<>();
        }
        try {
            return (Map<Object, Object>) ctor.newInstance();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 通过序列化的方式克隆对象
     *
     * @param object
     * @param <T>
     * @return
     */
    public static <T extends Serializable> T cloneBeanBySerialize(T object) {
        try {
            byte[] bytes = serializeObject(object);
            return deSerializeObject(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 序列化object
     *
     * @param object
     * @return
     */
    public static byte[] serializeObject(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 反序列化Object
     *
     * @param bytes
     * @param <T>
     * @return
     */
    public static <T> T deSerializeObject(byte[] bytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
            return (T) object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 常见数据格式相互转换
     * @param object
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T baseTypeConvert(Object object, Class<T> tClass) {
        Object o = ConvertUtils.convert(object, tClass);
        if (o == null && tClass.isPrimitive()) {
            if (tClass == int.class) {
                o = 0;
            } else if (tClass == long.class) {
                o = 0L;
            } else if (tClass == double.class) {
                o = 0D;
            } else if (tClass == float.class) {
                o = 0F;
            } else if (tClass == boolean.class) {
                o = false;
            } else if (tClass == byte.class) {
                o = (byte) 0;
            } else if (tClass == short.class) {
                o = (short) 0;
            } else if (tClass == char.class) {
                o = (char) 0;
            }
        }
        return (T) o;
    }


    public static boolean isBaseType(Class<?> clazz) {
        return BaseTypeCommon.isBaseType(clazz);
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray() || WsReflectUtils.classCompare(clazz, Collection.class);
    }

    public static <T> T createObject(Class<T> clazz) {
        return (T) WsUnsafeUtils.allocateInstance(clazz);
    }

    /**
     * 将数组或集合元素收集到指定 {@link Collection}。
     * 先统一经过 {@link #convertToArray} 归一化为数组，再用 {@link Array#get} 装箱填充，
     * 避免基本类型数组的逐类型 switch 模板代码。
     *
     * @param o        数组或集合
     * @param collection 目标容器；为 null 时使用 {@link ArrayList}
     * @param tClass   目标元素类型
     * @param <T>
     * @return 填充后的集合；无法转换返回 null
     */
    public static <T> Collection<Object> convertToList(Object o, Collection<Object> collection, Class<T> tClass) {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        Object array = convertToArray(o, tClass);
        if (array == null) {
            return null;
        }
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            // Array.get 对基本类型元素自动装箱，符合集合元素必须是 Object 的语义
            collection.add(Array.get(array, i));
        }
        return collection;
    }

    /**
     * 把数组或者 list 对象转换成数组。
     * 命中常见 same-type 路径时直接返回源引用（无需拷贝），其余情况委托
     * {@link #convertArray} 按元素 {@link #baseTypeConvert} 统一处理，
     * 兼顾 primitive→primitive、primitive→wrapper、wrapper→primitive、wrapper→wrapper。
     *
     * @param o      数组或者list
     * @param tClass 需要转换成的对象
     * @param <T>
     * @return
     */
    public static <T> Object convertToArray(Object o, Class<T> tClass) {
        if (o == null) {
            return null;
        }
        Class<?> srcComp = o.getClass().getComponentType();
        if (srcComp != null) {
            return convertArray(o, srcComp, tClass);
        }
        if (o instanceof Collection) {
            Object[] src = ((Collection<?>) o).toArray();
            if (tClass == Object.class) {
                return src;
            }
            return convertArray(src, Object.class, tClass);
        }
        return null;
    }

    /**
     * 统一的数组元素转换。命中完全同类型直接返回源数组引用；其余情况按元素
     * {@link #baseTypeConvert} 处理，{@link Array#set} 会在目标为 primitive 数组时
     * 自动拆箱。这替代了原先 8 份 primitive 源 × primitive/对象目标的模板代码。
     *
     * @param srcArr   源数组（primitive 或对象数组均可）
     * @param srcComp  源数组的 component 类型
     * @param tgtComp  目标 component 类型
     * @return 目标类型数组；若 srcComp == tgtComp 则直接返回源数组引用
     */
    private static Object convertArray(Object srcArr, Class<?> srcComp, Class<?> tgtComp) {
        if (srcComp == tgtComp) {
            return srcArr;
        }
        int len = Array.getLength(srcArr);
        Object dst = Array.newInstance(tgtComp, len);
        for (int i = 0; i < len; i++) {
            Object srcVal = Array.get(srcArr, i);
            Object conv = baseTypeConvert(srcVal, tgtComp);
            Array.set(dst, i, conv);
        }
        return dst;
    }


    /**
     * 对象转换成map
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> Map<Object, Object> convertToMap(T t) {
        if (WsBeanUtils.isBaseType(t.getClass()) || WsBeanUtils.isArray(t.getClass())) {
            throw new RuntimeException("不支持的格式");
        }
        if (t instanceof Map) {
            return (Map<Object, Object>) t;
        }
        Field[] fields = WsReflectUtils.getFieldAll(t.getClass());

        Map<Object, Object> map = new HashMap<>();
        for (Field field : fields) {
            Object value = WsReflectUtils.getValue(t, field);
            if (value != null) {
                String name = field.getName();

                if (WsBeanUtils.isBaseType(value.getClass())) {
                    map.put(name, value);
                } else if (value instanceof Map) {
                    map.put(name, value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    map.put(name, arrayToList(value));
                } else {
                    map.put(name, objectToMap(value));
                }
            }
        }
        return map;
    }

    /**
     * 把数组里的对象转换成map
     *
     * @param o
     * @return
     */
    private static List<Object> arrayToList(Object o) {
        if (o == null){
            throw new NullPointerException("待转换数组为空");
        }
        if (o.getClass().isArray()){
            List<Object> list;
            if (o.getClass().getComponentType().isPrimitive()) {
                list = new ArrayList<>(Array.getLength(o));
                if (o instanceof int[]) {
                    for (int v : (int[]) o) { list.add(v); }
                } else if (o instanceof short[]) {
                    for (short v : (short[]) o) { list.add(v); }
                } else if (o instanceof long[]) {
                    for (long v : (long[]) o) { list.add(v); }
                } else if (o instanceof float[]) {
                    for (float v : (float[]) o) { list.add(v); }
                } else if (o instanceof double[]) {
                    for (double v : (double[]) o) { list.add(v); }
                } else if (o instanceof byte[]) {
                    for (byte v : (byte[]) o) { list.add(v); }
                } else if (o instanceof char[]) {
                    for (char v : (char[]) o) { list.add(v); }
                } else if (o instanceof boolean[]) {
                    for (boolean v : (boolean[]) o) { list.add(v); }
                }
                return list;
            }
            Object[] objects = (Object[]) o;
            list = new ArrayList<>(objects.length);
            for (Object value : objects) {
                if (value == null) {
                    list.add(null);
                } else if (WsBeanUtils.isBaseType(value.getClass())) {
                    list.add(value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    list.add(arrayToList(value));
                } else if (value instanceof Map) {
                    list.add(value);
                } else {
                    list.add(objectToMap(value));
                }
            }
            return list;
        }else if (o instanceof Collection){
            Collection<Object> collection = (Collection<Object>) o;
            List<Object> list = new ArrayList<>(collection.size());
            for (Object value : collection) {
                if (value == null) {
                    list.add(null);
                } else if (WsBeanUtils.isBaseType(value.getClass())) {
                    list.add(value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    list.add(arrayToList(value));
                } else if (value instanceof Map) {
                    list.add(value);
                } else {
                    list.add(objectToMap(value));
                }
            }
            return list;
        }
        throw new IllegalArgumentException("不支持的类型");
    }


    /**
     * 对象转换成map
     *
     * @param o
     * @return
     */
    private static Map<String,Object> objectToMap(Object o) {
        if (WsBeanUtils.isBaseType(o.getClass()) || WsBeanUtils.isArray(o.getClass())) {
            throw new RuntimeException("格式错误");
        }

        Field[] fields = WsReflectUtils.getFieldAll(o.getClass());

        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            Object value = WsReflectUtils.getValue(o, field);
            if (value != null) {
                String name = field.getName();
                if (WsBeanUtils.isBaseType(value.getClass())) {
                    map.put(name, value);
                } else if (value instanceof Map) {
                    map.put(name, value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    map.put(name, arrayToList(value));
                } else {
                    map.put(name, objectToMap(value));
                }
            }
        }
        return map;
    }

}
