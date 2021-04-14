package cn.katoumegumi.java.common;


import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
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
            return objectToT(o, tClass);
        }
        Field[] sourceFields = WsFieldUtils.getFieldAll(o.getClass());
        Field[] targetFields = WsFieldUtils.getFieldAll(tClass);
        if (WsListUtils.isEmpty(sourceFields) || WsListUtils.isEmpty(targetFields)) {
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
                sourceValue = WsFieldUtils.getValue(o, sourceField);
                if (sourceValue != null) {
                    WsFieldUtils.setValue(target, WsBeanUtils.objectToT(sourceValue, targetField.getType()), targetField);
                }
            } else if (isArray(sourceField.getType()) && isArray(targetField.getType())) {
                Object value = WsFieldUtils.getValue(o, sourceField);
                if (value != null) {
                    if (targetField.getType().isArray()) {
                        Object setValue = convertToArray(value, targetField.getType().getComponentType());
                        if (setValue != null) {
                            WsFieldUtils.setValue(target, setValue, targetField);
                        }
                    } else {
                        Class<?> targetClass = WsFieldUtils.getClassTypeof(targetField);
                        Object setValue = null;
                        Object collection = null;
                        if (targetField.getType().equals(List.class) || targetField.getType().equals(Collection.class)) {
                            collection = new ArrayList<>();
                        } else if (targetField.getType().equals(Set.class)) {
                            collection = new HashSet<>();
                        }
                        if (targetClass == null) {
                            //setValue = value;
                            targetClass = Object.class;
                        }
                        setValue = convertToList(value, (Collection) collection, targetClass);
                        if (setValue != null) {
                            WsFieldUtils.setValue(target, setValue, targetField);
                        }
                    }

                }
            } else if (isArray(sourceField.getType())) {
                continue;
            } else if (isArray(targetField.getType())) {
                continue;
            } else if (WsFieldUtils.classCompare(sourceField.getType(), targetField.getType())) {
                sourceValue = WsFieldUtils.getValue(o, sourceField);
                if (sourceValue != null) {
                    WsFieldUtils.setValue(target, objectToT(sourceValue, targetField.getType()), targetField);
                }
            } else {
                continue;
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
    public static <T extends Serializable> T cloneBean(T object) {
        try {
            byte[] bytes = serializeObject(object);
            return (T) deSerializeObject(bytes);
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


    public static <T> T objectToT(Object object, Class<T> tClass) {
        try {
            if (tClass.equals(object.getClass())) {
                return (T) object;
            }
            if (tClass.equals(Object.class)) {
                return (T) object;
            }
            if (tClass == int.class) {
                if (object.getClass() == Integer.class) {
                    return (T) Integer.valueOf((int) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Integer.valueOf(((Number) object).intValue());
                    }
                    return (T) Integer.valueOf(String.valueOf(object));
                }
            } else if (tClass == Integer.class) {
                if (object.getClass() == Integer.class) {
                    return (T) object;
                } else {
                    if (object instanceof Number) {
                        return (T) Integer.valueOf(((Number) object).intValue());
                    }
                    return (T) Integer.valueOf(String.valueOf(object));
                }
            } else if (tClass == short.class) {
                if (object.getClass() == Short.class) {
                    return (T) ((Object) (Short) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Short.valueOf(((Number) object).shortValue());
                    }
                    return (T) Short.valueOf(String.valueOf(object));
                }
            } else if (tClass == Short.class) {
                if (object.getClass() == Short.class) {
                    return (T) object;
                } else {
                    if (object instanceof Number) {
                        return (T) Short.valueOf(((Number) object).shortValue());
                    }
                    return (T) Short.valueOf(String.valueOf(object));
                }
            } else if (tClass == byte.class) {
                if (object.getClass() == Byte.class) {
                    return (T) ((Object) (Byte) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Byte.valueOf(((Number) object).byteValue());
                    }
                    return (T) ((Object) Byte.parseByte(String.valueOf(object)));
                }
            } else if (tClass == Byte.class) {
                if (object.getClass() == Byte.class) {
                    return (T) object;
                } else {
                    return (T) ((Object) Byte.valueOf(String.valueOf(object)));
                }
            } else if (tClass == float.class) {
                if (object.getClass() == Float.class) {
                    return (T) ((Object) (Float) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Float.valueOf(((Number) object).floatValue());
                    }
                    return (T) ((Object) Float.parseFloat(String.valueOf(object)));
                }
            } else if (tClass == Float.class) {
                if (object.getClass() == Float.class) {
                    return (T) object;
                } else {
                    if (object instanceof Number) {
                        return (T) Float.valueOf(((Number) object).floatValue());
                    }
                    return (T) ((Object) Float.valueOf(String.valueOf(object)));
                }
            } else if (tClass == double.class) {
                if (object.getClass() == Double.class) {
                    return (T) ((Object) (Double) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Double.valueOf(((Number) object).doubleValue());
                    }
                    return (T) ((Object) Double.parseDouble(String.valueOf(object)));
                }
            } else if (tClass == Double.class) {
                if (object.getClass() == Double.class) {
                    return (T) object;
                } else {
                    if (object instanceof Number) {
                        return (T) Double.valueOf(((Number) object).doubleValue());
                    }
                    return (T) ((Object) Double.valueOf(String.valueOf(object)));
                }
            } else if (tClass == long.class) {
                if (object.getClass() == Long.class) {
                    return (T) ((Object) (Long) object);
                } else {
                    if (object instanceof Number) {
                        return (T) Long.valueOf(((Number) object).longValue());
                    }
                    return (T) Long.valueOf(String.valueOf(object));
                }
            } else if (tClass == Long.class) {
                if (object.getClass() == Long.class) {
                    return (T) object;
                } else {
                    if (object instanceof Number) {
                        return (T) Long.valueOf(((Number) object).longValue());
                    }
                    return (T) Long.valueOf(String.valueOf(object));
                }
            } else if (tClass == char.class) {
                if (object.getClass() == Character.class) {
                    return (T) ((Object) (Character) object);
                } else {
                    return (T) (Object) String.valueOf(object).charAt(0);
                }

            } else if (tClass == Character.class) {
                if (object.getClass() == Character.class) {
                    return (T) object;
                } else {
                    return (T) (Object) String.valueOf(object).charAt(0);
                }
            } else if (tClass == boolean.class) {
                if (object.getClass() == Boolean.class) {
                    return (T) ((Object) (Boolean) object);
                } else {
                    return (T) ((Object) Boolean.parseBoolean(String.valueOf(object)));
                }
            } else if (tClass == Boolean.class) {
                if (object.getClass() == Boolean.class) {
                    return (T) object;
                } else {
                    return (T) ((Object) Boolean.valueOf(String.valueOf(object)));
                }
            } else if (tClass == BigInteger.class) {
                if (object.getClass() == BigInteger.class) {
                    return (T) object;
                } else {
                    return (T) new BigInteger(String.valueOf(object));
                }
            } else if (tClass == BigDecimal.class) {
                if (object.getClass() == BigDecimal.class) {
                    return (T) object;
                } else {
                    return (T) new BigDecimal(String.valueOf(object));
                }
            } else if (tClass == String.class) {
                if (object.getClass() == String.class) {
                    return (T) object;
                } else {
                    return (T) WsStringUtils.anyToString(object);
                }
            } else if (tClass == Date.class || tClass == LocalDateTime.class || tClass == LocalDate.class || tClass == java.sql.Date.class) {
                if (object.getClass() == Date.class) {
                    if (tClass == LocalDate.class) {
                        Date date = (Date) object;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        return (T) LocalDate.ofYearDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
                    }
                    if (tClass == LocalDateTime.class) {
                        return (T) LocalDateTime.ofInstant(((Date) object).toInstant(), ZoneId.systemDefault());
                    }
                    if (tClass == java.sql.Date.class) {
                        return (T) new java.sql.Date(((Date) object).getTime());
                    }
                    return (T) object;
                } else {
                    Date date = WsDateUtils.objectToDate(object);
                    if (date == null) {
                        return null;
                    } else {
                        if (tClass == LocalDate.class) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            return (T) LocalDate.ofYearDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
                        }
                        if (tClass == LocalDateTime.class) {
                            return (T) LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        }
                        if (tClass == java.sql.Date.class) {
                            return (T) new java.sql.Date(date.getTime());
                        }
                        return (T) date;
                    }

                }
            } else {
                return convertBean(object, tClass);
                //return null;

            }
        } catch (Exception e) {
            return null;
        }

    }


    public static boolean isBaseType(Class<?> clazz) {
        return BaseTypeCommon.verify(clazz);
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray() || WsFieldUtils.classCompare(clazz, Collection.class);
    }


    public static <T> T createObject(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

    }


    public static String objectGetMethodName(Field field) {
        String fieldName = field.getName();
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String objectSetMethodName(Field field) {
        String fieldName = field.getName();
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }


    public static <T> Collection convertToList(Object o, Collection collection, Class<T> tClass) {
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
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rInts[i] = (int) objectToT(ints[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rShorts[i] = (short) objectToT(ints[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rLongs[i] = (long) objectToT(ints[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rFloats[i] = (float) objectToT(ints[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rDoubles[i] = (double) objectToT(ints[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rBytes[i] = (byte) objectToT(ints[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rChars[i] = (char) objectToT(ints[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, ints.length);
                                    for (int i = 0; i < ints.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(ints[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, ints.length);
                            for (int i = 0; i < ints.length; i++) {
                                objects[i] = objectToT(ints[i], tClass);
                            }
                            return objects;
                        }
                    case "short":
                        short[] shorts = (short[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rInts[i] = (int) objectToT(shorts[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rShorts[i] = (short) objectToT(shorts[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rLongs[i] = (long) objectToT(shorts[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rFloats[i] = (float) objectToT(shorts[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rDoubles[i] = (double) objectToT(shorts[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rBytes[i] = (byte) objectToT(shorts[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rChars[i] = (char) objectToT(shorts[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, shorts.length);
                                    for (int i = 0; i < shorts.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(shorts[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, shorts.length);
                            for (int i = 0; i < shorts.length; i++) {
                                objects[i] = objectToT(shorts[i], tClass);
                            }
                            return objects;
                        }
                    case "long":
                        long[] longs = (long[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rInts[i] = (int) objectToT(longs[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rShorts[i] = (short) objectToT(longs[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rLongs[i] = (long) objectToT(longs[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rFloats[i] = (float) objectToT(longs[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rDoubles[i] = (double) objectToT(longs[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rBytes[i] = (byte) objectToT(longs[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rChars[i] = (char) objectToT(longs[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, longs.length);
                                    for (int i = 0; i < longs.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(longs[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, longs.length);
                            for (int i = 0; i < longs.length; i++) {
                                objects[i] = objectToT(longs[i], tClass);
                            }
                            return objects;
                        }
                    case "float":
                        float[] floats = (float[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rInts[i] = (int) objectToT(floats[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rShorts[i] = (short) objectToT(floats[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rLongs[i] = (long) objectToT(floats[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rFloats[i] = (float) objectToT(floats[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rDoubles[i] = (double) objectToT(floats[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rBytes[i] = (byte) objectToT(floats[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rChars[i] = (char) objectToT(floats[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, floats.length);
                                    for (int i = 0; i < floats.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(floats[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, floats.length);
                            for (int i = 0; i < floats.length; i++) {
                                objects[i] = objectToT(floats[i], tClass);
                            }
                            return objects;
                        }
                    case "double":
                        double[] doubles = (double[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rInts[i] = (int) objectToT(doubles[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rShorts[i] = (short) objectToT(doubles[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rLongs[i] = (long) objectToT(doubles[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rFloats[i] = (float) objectToT(doubles[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rDoubles[i] = (double) objectToT(doubles[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rBytes[i] = (byte) objectToT(doubles[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rChars[i] = (char) objectToT(doubles[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, doubles.length);
                                    for (int i = 0; i < doubles.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(doubles[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, doubles.length);
                            for (int i = 0; i < doubles.length; i++) {
                                objects[i] = objectToT(doubles[i], tClass);
                            }
                            return objects;
                        }
                    case "byte":
                        byte[] bytes = (byte[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rInts[i] = (int) objectToT(bytes[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rShorts[i] = (short) objectToT(bytes[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rLongs[i] = (long) objectToT(bytes[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rFloats[i] = (float) objectToT(bytes[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rDoubles[i] = (double) objectToT(bytes[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rBytes[i] = (byte) objectToT(bytes[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rChars[i] = (char) objectToT(bytes[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, bytes.length);
                                    for (int i = 0; i < bytes.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(bytes[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, bytes.length);
                            for (int i = 0; i < bytes.length; i++) {
                                objects[i] = objectToT(bytes[i], tClass);
                            }
                            return objects;
                        }
                    case "char":
                        char[] chars = (char[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rInts[i] = (int) objectToT(chars[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rShorts[i] = (short) objectToT(chars[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rLongs[i] = (long) objectToT(chars[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rFloats[i] = (float) objectToT(chars[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rDoubles[i] = (double) objectToT(chars[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rBytes[i] = (byte) objectToT(chars[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rChars[i] = (char) objectToT(chars[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, chars.length);
                                    for (int i = 0; i < chars.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(chars[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, chars.length);
                            for (int i = 0; i < chars.length; i++) {
                                objects[i] = objectToT(chars[i], tClass);
                            }
                            return objects;
                        }
                    case "boolean":
                        boolean[] booleans = (boolean[]) o;
                        if (tClass.isPrimitive()) {
                            switch (tClass.getName()) {
                                case "int":
                                    int[] rInts = (int[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rInts[i] = (int) objectToT(booleans[i], tClass);
                                    }
                                    return rInts;
                                case "short":
                                    short[] rShorts = (short[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rShorts[i] = (short) objectToT(booleans[i], tClass);
                                    }
                                    return rShorts;
                                case "long":
                                    long[] rLongs = (long[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rLongs[i] = (long) objectToT(booleans[i], tClass);
                                    }
                                    return rLongs;
                                case "float":
                                    float[] rFloats = (float[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rFloats[i] = (float) objectToT(booleans[i], tClass);
                                    }
                                    return rFloats;
                                case "double":
                                    double[] rDoubles = (double[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rDoubles[i] = (double) objectToT(booleans[i], tClass);
                                    }
                                    return rDoubles;
                                case "byte":
                                    byte[] rBytes = (byte[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rBytes[i] = (byte) objectToT(booleans[i], tClass);
                                    }
                                    return rBytes;
                                case "char":
                                    char[] rChars = (char[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rChars[i] = (char) objectToT(booleans[i], tClass);
                                    }
                                    return rChars;
                                case "boolean":
                                    boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, booleans.length);
                                    for (int i = 0; i < booleans.length; i++) {
                                        rBooleans[i] = (boolean) objectToT(booleans[i], tClass);
                                    }
                                    return rBooleans;
                                default:
                                    throw new RuntimeException("不支持的类型");
                            }
                        } else {
                            objects = (T[]) Array.newInstance(tClass, booleans.length);
                            for (int i = 0; i < booleans.length; i++) {
                                objects[i] = objectToT(booleans[i], tClass);
                            }
                            return objects;
                        }
                    default:
                        throw new RuntimeException("非基本类型");
                }
            } else {
                if (o.getClass().equals(tClass)) {
                    return (T[]) o;
                } else {
                    Object[] objects = (Object[]) o;
                    if (tClass.isPrimitive()) {
                        switch (tClass.getName()) {
                            case "int":
                                int[] rInts = (int[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rInts[i] = (int) objectToT(objects[i], tClass);
                                }
                                return rInts;
                            case "short":
                                short[] rShorts = (short[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rShorts[i] = (short) objectToT(objects[i], tClass);
                                }
                                return rShorts;
                            case "long":
                                long[] rLongs = (long[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rLongs[i] = (long) objectToT(objects[i], tClass);
                                }
                                return rLongs;
                            case "float":
                                float[] rFloats = (float[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rFloats[i] = (float) objectToT(objects[i], tClass);
                                }
                                return rFloats;
                            case "double":
                                double[] rDoubles = (double[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rDoubles[i] = (double) objectToT(objects[i], tClass);
                                }
                                return rDoubles;
                            case "byte":
                                byte[] rBytes = (byte[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rBytes[i] = (byte) objectToT(objects[i], tClass);
                                }
                                return rBytes;
                            case "char":
                                char[] rChars = (char[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rChars[i] = (char) objectToT(objects[i], tClass);
                                }
                                return rChars;
                            case "boolean":
                                boolean[] rBooleans = (boolean[]) Array.newInstance(tClass, objects.length);
                                for (int i = 0; i < objects.length; i++) {
                                    rBooleans[i] = (boolean) objectToT(objects[i], tClass);
                                }
                                return rBooleans;
                            default:
                                throw new RuntimeException("不支持的类型");
                        }
                    } else {
                        T[] ts = (T[]) Array.newInstance(tClass, objects.length);
                        for (int i = 0; i < objects.length; i++) {
                            ts[i] = objectToT(objects[i], tClass);
                        }
                        return ts;
                    }
                }
            }
        } else if (o instanceof Collection) {
            Collection<?> collection = (Collection<?>) o;
            Object[] objects = collection.toArray();
            T[] ts = (T[]) Array.newInstance(tClass, objects.length);
            for (int i = 0; i < objects.length; i++) {
                ts[i] = objectToT(objects[i], tClass);
            }
            return ts;
        }
        return null;
    }

}
