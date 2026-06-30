package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.convert.ConvertUtils;
import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * bean工具类
 *
 * @author ws
 */
@SuppressWarnings("unchecked")
public class WsBeanUtils {

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
    }

    /**
     * 将源属性值按目标属性类型转换为可写入的值。
     */
    private static Object convertValue(Object value, BeanPropertyModel targetPm) {
        Class<?> targetType = targetPm.getPropertyClass();
        if (targetType == null) {
            return null;
        }
        Class<?> valueClass = value.getClass();
        boolean valueIsArray = isArray(valueClass);

        // 基础类型走 ConvertUtils
        if (isBaseType(targetType)) {
            return baseTypeConvert(value, targetType);
        }
        if (targetType == Object.class) {
            return value;
        }

        // 目标为数组
        if (targetType.isArray()) {
            if (!valueIsArray) {
                return null;
            }
            return convertToArray(value, targetType.getComponentType());
        }

        // 目标为 Collection
        if (WsReflectUtils.classCompare(targetType, Collection.class)) {
            Collection<Object> collection = newCollectionInstance(targetType);
            Class<?> elementClass = targetPm.getGenericClass();
            if (elementClass == null) {
                elementClass = Object.class;
            }
            return convertToList(value, collection, elementClass);
        }

        // 目标为 Map
        if (WsReflectUtils.classCompare(targetType, Map.class)) {
            if (!(value instanceof Map) && !isArray(valueClass)) {
                return null;
            }
            return convertToMap(value, targetPm);
        }

        // 普通 JavaBean，递归
        if (valueIsArray) {
            return null;
        }
        if (targetType.isAssignableFrom(valueClass) && targetType == valueClass) {
            return value;
        }
        return convertBean(value, targetType);
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
        if (target == null) {
            return null;
        }
        Class<?> keyClass = targetPm.getKeyGenericClass();
        Class<?> valueClass = targetPm.getValueGenericClass();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Object dstKey = convertMapElement(entry.getKey(), keyClass);
            if (dstKey == null && entry.getKey() != null) {
                continue;
            }
            Object dstVal = convertMapElement(entry.getValue(), valueClass);
            if (dstVal == null && entry.getValue() != null) {
                continue;
            }
            target.put(dstKey, dstVal);
        }
        return target;
    }

    /**
     * 将单个 Map 元素（key 或 value）转换为指定类型。
     */
    private static Object convertMapElement(Object element, Class<?> targetClass) {
        if (element == null) {
            return null;
        }
        if (targetClass == null || targetClass == Object.class) {
            return element;
        }
        if (isBaseType(targetClass)) {
            return baseTypeConvert(element, targetClass);
        }
        if (targetClass.isArray()) {
            return isArray(element.getClass()) ? convertToArray(element, targetClass.getComponentType()) : null;
        }
        if (WsReflectUtils.classCompare(targetClass, Collection.class)) {
            Collection<Object> collection = newCollectionInstance(targetClass);
            // 元素类型未知，退化为 Object
            return convertToList(element, collection, Object.class);
        }
        if (WsReflectUtils.classCompare(targetClass, Map.class)) {
            Map<Object, Object> nested = newMapInstance(targetClass);
            if (nested == null) {
                return null;
            }
            Map<?, ?> src;
            try {
                src = (Map<?, ?>) element;
            } catch (ClassCastException e) {
                return null;
            }
            for (Map.Entry<?, ?> entry : src.entrySet()) {
                nested.put(entry.getKey(), entry.getValue());
            }
            return nested;
        }
        // 普通 bean
        if (isArray(element.getClass())) {
            return null;
        }
        if (targetClass.isAssignableFrom(element.getClass()) && targetClass == element.getClass()) {
            return element;
        }
        return convertBean(element, targetClass);
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> toMap(Object value) {
        if (value instanceof Map) {
            return (Map<Object, Object>) value;
        }
        // 仅支持 Map 源，数组/集合不直接转 Map
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T newTargetInstance(Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Object instance = WsUnsafeUtils.allocateInstance(tClass);
            return (T) instance;
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollectionInstance(Class<?> collectionType) {
        if (collectionType.equals(List.class)
                || collectionType.equals(Collection.class)
                || collectionType.equals(ArrayList.class)) {
            return new ArrayList<>();
        }
        if (collectionType.equals(Set.class)
                || collectionType.equals(HashSet.class)) {
            return new HashSet<>();
        }
        try {
            return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
        } catch (Exception ignore) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> newMapInstance(Class<?> mapType) {
        if (mapType.equals(Map.class)
                || mapType.equals(HashMap.class)) {
            return new HashMap<>();
        }
        if (mapType.equals(java.util.LinkedHashMap.class)) {
            return new java.util.LinkedHashMap<>();
        }
        if (mapType.equals(java.util.TreeMap.class)) {
            return new java.util.TreeMap<>();
        }
        try {
            return (Map<Object, Object>) mapType.getDeclaredConstructor().newInstance();
        } catch (Exception ignore) {
            return new HashMap<>();
        }
    }

    /**
     * 为 Map 值类型构造一个用于元素类型推导的合成 PropertyModel。
     */
    private static BeanPropertyModel buildSyntheticPropertyModel(Class<?> targetClass) {
        // 借用一个目标类的可写属性作为载体，仅为拿到 genericClass/valueGenericClass；
        // 当目标类型无属性（基础类型/集合等）已由上游处理，这里只对 bean 走常规路径。
        BeanModel model = WsReflectUtils.createBeanModel(targetClass);
        Map<String, BeanPropertyModel> pmMap = model.getPropertyModelMap();
        if (pmMap.isEmpty()) {
            return null;
        }
        return pmMap.values().iterator().next();
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
        boolean isPrimitive = tClass.isPrimitive();
        if (isPrimitive && o == null) {
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
     * 按字段类型分派 setValue，避免对 primitive 字段走 putObject 抛 IllegalArgumentException/写脏值。
     * 当 value 为 null 时不写入（保持自然语义）。
     */
    private static void setFieldValue(Object target, Object value, Field field) {
        if (value == null) {
            return;
        }
        Class<?> ft = field.getType();
        if (ft == int.class) {
            WsReflectUtils.setValue(target, ((Number) value).intValue(), field);
        } else if (ft == long.class) {
            WsReflectUtils.setValue(target, ((Number) value).longValue(), field);
        } else if (ft == short.class) {
            WsReflectUtils.setValue(target, ((Number) value).shortValue(), field);
        } else if (ft == byte.class) {
            WsReflectUtils.setValue(target, ((Number) value).byteValue(), field);
        } else if (ft == float.class) {
            WsReflectUtils.setValue(target, ((Number) value).floatValue(), field);
        } else if (ft == double.class) {
            WsReflectUtils.setValue(target, ((Number) value).doubleValue(), field);
        } else if (ft == boolean.class) {
            WsReflectUtils.setValue(target, (Boolean) value, field);
        } else if (ft == char.class) {
            if (value instanceof Character) {
                WsReflectUtils.setValue(target, (Character) value, field);
            } else if (value instanceof String && ((String) value).length() == 1) {
                WsReflectUtils.setValue(target, ((String) value).charAt(0), field);
            } else {
                WsReflectUtils.setValue(target, ((Number) value).intValue(), field);
            }
        } else {
            WsReflectUtils.setValue(target, value, field);
        }
    }

    public static <T> Collection<Object> convertToList(Object o, Collection<Object> collection, Class<T> tClass) {
        if (collection == null) {
            collection = new ArrayList<>();
        }
        Object object = convertToArray(o, tClass);
        if (object == null) {
            return null;
        }

        if (object.getClass().getComponentType().isPrimitive()) {
            switch (object.getClass().getComponentType().getName()) {
                case "int":
                    int[] ints = (int[]) object;
                    for (int i : ints) {
                        collection.add(i);
                    }
                    return collection;
                case "short":
                    short[] shorts = (short[]) object;
                    for (short i : shorts) {
                        collection.add(i);
                    }
                    return collection;
                case "long":
                    long[] longs = (long[]) object;
                    for (long i : longs) {
                        collection.add(i);
                    }
                    return collection;
                case "float":
                    float[] floats = (float[]) object;
                    for (float i : floats) {
                        collection.add(i);
                    }
                    return collection;
                case "double":
                    double[] doubles = (double[]) object;
                    for (double i : doubles) {
                        collection.add(i);
                    }
                    return collection;
                case "byte":
                    byte[] bytes = (byte[]) object;
                    for (byte i : bytes) {
                        collection.add(i);
                    }
                    return collection;
                case "char":
                    char[] chars = (char[]) object;
                    for (char i : chars) {
                        collection.add(i);
                    }
                    return collection;
                case "boolean":
                    boolean[] booleans = (boolean[]) object;
                    for (boolean i : booleans) {
                        collection.add(i);
                    }
                    return collection;
                default:
                    throw new RuntimeException("不支持的类型");
            }

        } else {
            Object[] objects = (Object[]) object;
            Collections.addAll(collection, objects);
            return collection;
        }
    }

    /**
     * 把数组或者list对象转换成数组
     *
     * @param o      数组或者list
     * @param tClass 需要转换成的对象
     * @param <T>
     * @return
     */
    public static <T> Object convertToArray(Object o, Class<T> tClass) {
        if (o.getClass().isArray()) {
            if (o.getClass().getComponentType().equals(tClass)) {
                return o;
            }
            if (o.getClass().getComponentType().isPrimitive()) {
                T[] objects;
                switch (o.getClass().getComponentType().getName()) {
                    case "int":
                        int[] ints = (int[]) o;
                        return intArrayToTArray(tClass, ints);
                    case "short":
                        short[] shorts = (short[]) o;
                        return shortArrayToTArray(tClass, shorts);
                    case "long":
                        long[] longs = (long[]) o;
                        return longArrayToTArray(tClass, longs);
                    case "float":
                        float[] floats = (float[]) o;
                        return floatArrayToTArray(tClass, floats);
                    case "double":
                        double[] doubles = (double[]) o;
                        return doubleArrayToTArray(tClass, doubles);
                    case "byte":
                        byte[] bytes = (byte[]) o;
                        return byteArrayToTArray(tClass, bytes);
                    case "char":
                        char[] chars = (char[]) o;
                        return charArrayToTArray(tClass, chars);
                    case "boolean":
                        boolean[] booleans = (boolean[]) o;
                        return booleanArrayToTArray(tClass, booleans);
                    default:
                        throw new RuntimeException("非基本类型");
                }
            } else {
                if (o.getClass().equals(tClass)) {
                    return o;
                } else {
                    Object[] objects = (Object[]) o;
                    return objectArrayToTArray(tClass, objects);
                }
            }
        } else if (o instanceof Collection) {
            return objectArrayToTArray(tClass, ((Collection<?>) o).toArray());
        }
        return null;
    }

    private static <T> Object objectArrayToTArray(Class<T> tClass, Object[] objects) {
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(objects[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(objects[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(objects[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(objects[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(objects[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(objects[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(objects[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, objects.length);
                    for (int i = 0; i < objects.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(objects[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            T[] ts = (T[]) Array.newInstance(tClass, objects.length);
            for (int i = 0; i < objects.length; i++) {
                ts[i] = baseTypeConvert(objects[i], tClass);
            }
            return ts;
        }
    }

    private static <T> Object booleanArrayToTArray(Class<T> tClass, boolean[] booleans) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(booleans[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(booleans[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(booleans[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(booleans[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(booleans[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(booleans[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(booleans[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, booleans.length);
                    for (int i = 0; i < booleans.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(booleans[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, booleans.length);
            for (int i = 0; i < booleans.length; i++) {
                objects[i] = baseTypeConvert(booleans[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object charArrayToTArray(Class<T> tClass, char[] chars) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(chars[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(chars[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(chars[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(chars[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(chars[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(chars[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(chars[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, chars.length);
                    for (int i = 0; i < chars.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(chars[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, chars.length);
            for (int i = 0; i < chars.length; i++) {
                objects[i] = baseTypeConvert(chars[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object byteArrayToTArray(Class<T> tClass, byte[] bytes) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(bytes[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(bytes[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(bytes[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(bytes[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(bytes[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(bytes[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(bytes[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, bytes.length);
                    for (int i = 0; i < bytes.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(bytes[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                objects[i] = baseTypeConvert(bytes[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object doubleArrayToTArray(Class<T> tClass, double[] doubles) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(doubles[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(doubles[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(doubles[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(doubles[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(doubles[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(doubles[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(doubles[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, doubles.length);
                    for (int i = 0; i < doubles.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(doubles[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, doubles.length);
            for (int i = 0; i < doubles.length; i++) {
                objects[i] = baseTypeConvert(doubles[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object floatArrayToTArray(Class<T> tClass, float[] floats) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(floats[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(floats[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(floats[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(floats[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(floats[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(floats[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(floats[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, floats.length);
                    for (int i = 0; i < floats.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(floats[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, floats.length);
            for (int i = 0; i < floats.length; i++) {
                objects[i] = baseTypeConvert(floats[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object longArrayToTArray(Class<T> tClass, long[] longs) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(longs[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(longs[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(longs[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(longs[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(longs[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(longs[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(longs[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, longs.length);
                    for (int i = 0; i < longs.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(longs[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, longs.length);
            for (int i = 0; i < longs.length; i++) {
                objects[i] = baseTypeConvert(longs[i], tClass);
            }
            return objects;
        }
    }

    private static <T> Object shortArrayToTArray(Class<T> tClass, short[] shorts) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(shorts[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(shorts[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(shorts[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(shorts[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(shorts[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(shorts[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(shorts[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, shorts.length);
                    for (int i = 0; i < shorts.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(shorts[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, shorts.length);
            for (int i = 0; i < shorts.length; i++) {
                objects[i] = baseTypeConvert(shorts[i], tClass);
            }
            return objects;
        }
    }

    /**
     * int数组转换
     *
     * @param tClass
     * @param ints
     * @param <T>
     * @return
     */
    private static <T> Object intArrayToTArray(Class<T> tClass, int[] ints) {
        T[] objects;
        if (tClass.isPrimitive()) {
            switch (tClass.getName()) {
                case "int":
                    int[] rInts = (int[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rInts[i] = (Integer) baseTypeConvert(ints[i], tClass);
                    }
                    return rInts;
                case "short":
                    short[] rShorts = (short[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rShorts[i] = (Short) baseTypeConvert(ints[i], tClass);
                    }
                    return rShorts;
                case "long":
                    long[] rLongs = (long[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rLongs[i] = (Long) baseTypeConvert(ints[i], tClass);
                    }
                    return rLongs;
                case "float":
                    float[] rFloats = (float[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rFloats[i] = (Float) baseTypeConvert(ints[i], tClass);
                    }
                    return rFloats;
                case "double":
                    double[] rDoubles = (double[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rDoubles[i] = (Double) baseTypeConvert(ints[i], tClass);
                    }
                    return rDoubles;
                case "byte":
                    byte[] rBytes = (byte[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rBytes[i] = (Byte) baseTypeConvert(ints[i], tClass);
                    }
                    return rBytes;
                case "char":
                    char[] rChars = (char[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rChars[i] = (Character) baseTypeConvert(ints[i], tClass);
                    }
                    return rChars;
                case "boolean":
                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, ints.length);
                    for (int i = 0; i < ints.length; i++) {
                        rBooleans[i] = (Boolean) baseTypeConvert(ints[i], tClass);
                    }
                    return rBooleans;
                default:
                    throw new RuntimeException("不支持的类型");
            }
        } else {
            objects = (T[]) Array.newInstance(tClass, ints.length);
            for (int i = 0; i < ints.length; i++) {
                objects[i] = baseTypeConvert(ints[i], tClass);
            }
            return objects;
        }
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
