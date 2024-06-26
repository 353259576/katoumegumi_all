package cn.katoumegumi.java.common;

import cn.katoumegumi.java.common.convert.ConvertUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * bean工具类
 *
 * @author ws
 */
@SuppressWarnings("unchecked")
public class WsBeanUtils {

    /**
     * 转换bean
     *
     * @param o
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T convertBean(Object o, Class<T> tClass) {
        if (isArray(o.getClass())) {
            throw new IllegalArgumentException("不支持直接转数组");
        }
        if (tClass.isInterface()) {
            throw new IllegalArgumentException("不支持转换为接口");
        }
        if (isBaseType(o.getClass())) {
            return baseTypeConvert(o, tClass);
        }
        Field[] sourceFields = WsReflectUtils.getFieldAll(o.getClass());
        Field[] targetFields = WsReflectUtils.getFieldAll(tClass);
        if (WsCollectionUtils.isEmpty(sourceFields) || WsCollectionUtils.isEmpty(targetFields)) {
            return null;
        }
        Map<String, Field> targetNameAndFieldMap = new HashMap<>(targetFields.length);
        for (Field field : targetFields) {
            field.setAccessible(true);
            targetNameAndFieldMap.put(field.getName(), field);
        }
        Field[] orderlyTargetFields = new Field[sourceFields.length];
        Field targetField;
        Field sourceField;
        for (int i = 0; i < sourceFields.length; i++) {
            sourceField = sourceFields[i];
            targetField = targetNameAndFieldMap.get(sourceField.getName());
            orderlyTargetFields[i] = targetField;
        }
        T target = createObject(tClass);
        Object sourceValue;
        for (int i = 0; i < sourceFields.length; i++) {
            targetField = orderlyTargetFields[i];
            if (targetField == null) {
                continue;
            }
            sourceField = sourceFields[i];
            if (isBaseType(sourceField.getType()) && isBaseType(targetField.getType())) {
                sourceValue = WsReflectUtils.getValue(o, sourceField);
                if (sourceValue != null) {
                    WsReflectUtils.setValue(
                            target, WsBeanUtils.baseTypeConvert(sourceValue, targetField.getType()), targetField);
                }
            } else if (isArray(sourceField.getType()) && isArray(targetField.getType())) {
                Object value = WsReflectUtils.getValue(o, sourceField);
                if (value != null) {
                    if (targetField.getType().isArray()) {
                        Object setValue = convertToArray(value, targetField.getType().getComponentType());
                        if (setValue != null) {
                            WsReflectUtils.setValue(target, setValue, targetField);
                        }
                    } else {
                        Class<?> targetClass = WsReflectUtils.getClassTypeof(targetField);
                        Object setValue;
                        Collection<Object> collection = null;
                        if (targetField.getType().equals(List.class)
                                || targetField.getType().equals(Collection.class)) {
                            collection = new ArrayList<>();
                        } else if (targetField.getType().equals(Set.class)) {
                            collection = new HashSet<>();
                        }
                        if (targetClass == null) {
                            // setValue = value;
                            targetClass = Object.class;
                        }
                        setValue = convertToList(value, collection, targetClass);
                        if (setValue != null) {
                            WsReflectUtils.setValue(target, setValue, targetField);
                        }
                    }
                }
                continue;
            }

            if (isArray(sourceField.getType()) || isArray(targetField.getType())) {
                continue;
            }

            if (WsReflectUtils.classCompare(sourceField.getType(), targetField.getType())) {
                sourceValue = WsReflectUtils.getValue(o, sourceField);
                if (sourceValue != null) {
                    WsReflectUtils.setValue(target, baseTypeConvert(sourceValue, targetField.getType()), targetField);
                }
            }
        }
        return target;
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
                o = 0;
            } else if (tClass == short.class) {
                o = 0;
            } else if (tClass == char.class) {
                o = 0;
            }
        }
        return (T) o;
    }


//    private static <T> T convertToT(Object object, Class<T> tClass) {
//        try {
//            if (tClass.isPrimitive()) {
//                tClass = (Class<T>) BaseTypeCommon.getWrapperClass(tClass);
//            }
//            if (tClass.equals(object.getClass())) {
//                return (T) object;
//            }
//            if (tClass.equals(Object.class)) {
//                return (T) object;
//            }
//            if (tClass == Integer.class) {
//                if (object instanceof Number) {
//                    return (T) Integer.valueOf(((Number) object).intValue());
//                }
//                return (T) Integer.valueOf(String.valueOf(object));
//            } else if (tClass == Short.class) {
//                if (object instanceof Number) {
//                    return (T) Short.valueOf(((Number) object).shortValue());
//                }
//                return (T) Short.valueOf(String.valueOf(object));
//            } else if (tClass == Byte.class) {
//                return (T) Byte.valueOf(String.valueOf(object));
//            } else if (tClass == Float.class) {
//                if (object instanceof Number) {
//                    return (T) Float.valueOf(((Number) object).floatValue());
//                }
//                return (T) Float.valueOf(String.valueOf(object));
//            } else if (tClass == Double.class) {
//                if (object instanceof Number) {
//                    return (T) Double.valueOf(((Number) object).doubleValue());
//                }
//                return (T) Double.valueOf(String.valueOf(object));
//            } else if (tClass == Long.class) {
//                if (object instanceof Number) {
//                    return (T) Long.valueOf(((Number) object).longValue());
//                }
//                return (T) Long.valueOf(String.valueOf(object));
//            } else if (tClass == Character.class) {
//                return (T) (Object) String.valueOf(object).charAt(0);
//            } else if (tClass == Boolean.class) {
//                return (T) Boolean.valueOf(String.valueOf(object));
//            } else if (tClass == BigInteger.class) {
//                return (T) new BigInteger(String.valueOf(object));
//            } else if (tClass == BigDecimal.class) {
//                return (T) new BigDecimal(String.valueOf(object));
//            } else if (tClass == String.class) {
//                return (T) WsStringUtils.anyToString(object);
//            } else if (tClass == Date.class
//                    || tClass == LocalDateTime.class
//                    || tClass == LocalDate.class
//                    || tClass == java.sql.Date.class) {
//                if (object.getClass() == Date.class) {
//                    if (tClass == LocalDate.class) {
//                        Date date = (Date) object;
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTime(date);
//                        return (T)
//                                LocalDate.ofYearDay(
//                                        calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
//                    }
//                    if (tClass == LocalDateTime.class) {
//                        return (T) LocalDateTime.ofInstant(((Date) object).toInstant(), ZoneId.systemDefault());
//                    }
//                    return (T) new java.sql.Date(((Date) object).getTime());
//                } else {
//                    Date date = WsDateUtils.objectToDate(object);
//                    if (date == null) {
//                        return null;
//                    } else {
//                        if (tClass == LocalDate.class) {
//                            Calendar calendar = Calendar.getInstance();
//                            calendar.setTime(date);
//                            return (T)
//                                    LocalDate.ofYearDay(
//                                            calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
//                        }
//                        if (tClass == LocalDateTime.class) {
//                            return (T) LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
//                        }
//                        if (tClass == java.sql.Date.class) {
//                            return (T) new java.sql.Date(date.getTime());
//                        }
//                        return (T) date;
//                    }
//                }
//            } else {
//                return convertBean(object, tClass);
//            }
//        } catch (Exception e) {
//            return null;
//        }
//    }

    public static boolean isBaseType(Class<?> clazz) {
        return BaseTypeCommon.verify(clazz);
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray() || WsReflectUtils.classCompare(clazz, Collection.class);
    }

    public static <T> T createObject(Class<T> clazz) {
        return (T) WsUnsafeUtils.allocateInstance(clazz);
    }

    public static <T> Collection<Object> convertToList(Object o, Collection<Object> collection, Class<T> tClass) {
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
            Collection<?> collection = (Collection<?>) o;
            Object[] objects = collection.toArray();
            T[] ts = (T[]) Array.newInstance(tClass, objects.length);
            for (int i = 0; i < objects.length; i++) {
                ts[i] = baseTypeConvert(objects[i], tClass);
            }
            return ts;
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
            Object[] objects = (Object[]) o;
            List<Object> list = new ArrayList<>(objects.length);
            for (Object value : objects) {
                if (WsBeanUtils.isBaseType(value.getClass())) {
                    list.add(value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    list.add(arrayToList(value));
                } else if (value instanceof Map) {
                    list.add(o);
                } else {
                    list.add(objectToMap(o));
                }
            }
            return list;
        }else if (o instanceof Collection){
            Collection<Object> collection = (Collection<Object>) o;
            List<Object> list = new ArrayList<>(collection.size());
            for (Object value : collection) {
                if (WsBeanUtils.isBaseType(value.getClass())) {
                    list.add(value);
                } else if (WsBeanUtils.isArray(value.getClass())) {
                    list.add(arrayToList(value));
                } else if (value instanceof Map) {
                    list.add(o);
                } else {
                    list.add(objectToMap(o));
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
