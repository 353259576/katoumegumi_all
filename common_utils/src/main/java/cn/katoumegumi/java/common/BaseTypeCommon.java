package cn.katoumegumi.java.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 基本类型
 *
 * @author ws
 */
public class BaseTypeCommon {

    private static final Set<Class<?>> CLASS_SET = new HashSet<>();

    private static final Set<Class<?>> ARRAY_CLASS_SET = new HashSet<>();

    private static final Map<Class<?>,Class<?>> BASE_AND_WRAPPER_TYPE_MAP = new HashMap<>();

    static {
        CLASS_SET.add(byte.class);
        CLASS_SET.add(Byte.class);
        CLASS_SET.add(char.class);
        CLASS_SET.add(Character.class);
        CLASS_SET.add(boolean.class);
        CLASS_SET.add(Boolean.class);
        CLASS_SET.add(short.class);
        CLASS_SET.add(Short.class);
        CLASS_SET.add(float.class);
        CLASS_SET.add(Float.class);
        CLASS_SET.add(int.class);
        CLASS_SET.add(Integer.class);
        CLASS_SET.add(double.class);
        CLASS_SET.add(Double.class);
        CLASS_SET.add(long.class);
        CLASS_SET.add(Long.class);
        CLASS_SET.add(String.class);
        CLASS_SET.add(Date.class);
        CLASS_SET.add(java.sql.Date.class);
        CLASS_SET.add(LocalDate.class);
        CLASS_SET.add(LocalDateTime.class);
        CLASS_SET.add(BigDecimal.class);
        CLASS_SET.add(BigInteger.class);


        ARRAY_CLASS_SET.add(int[].class);
        ARRAY_CLASS_SET.add(byte[].class);
        ARRAY_CLASS_SET.add(char[].class);
        ARRAY_CLASS_SET.add(short[].class);
        ARRAY_CLASS_SET.add(long[].class);
        ARRAY_CLASS_SET.add(float[].class);
        ARRAY_CLASS_SET.add(double[].class);
        ARRAY_CLASS_SET.add(boolean[].class);

        BASE_AND_WRAPPER_TYPE_MAP.put(int.class,Integer.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(long.class,Long.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(short.class,Short.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(boolean.class,Boolean.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(double.class,Double.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(float.class,Float.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(char.class,Character.class);
        BASE_AND_WRAPPER_TYPE_MAP.put(byte.class,Byte.class);
    }

    public static boolean isBaseType(Class<?> tClass) {
        return tClass != null && CLASS_SET.contains(tClass);
    }

    public static boolean isBaseType(Object o) {
        return o != null && isBaseType(o.getClass());
    }

    public static boolean isBaseTypeArray(Class<?> tClass) {
        return tClass != null && ARRAY_CLASS_SET.contains(tClass);
    }

    /**
     * 获取基本类型的包装类
     *
     * @param clazz
     * @return
     */
    public static Class<?> getWrapperClass(Class<?> clazz) {
        Class<?> aClass = BASE_AND_WRAPPER_TYPE_MAP.get(clazz);
        if (aClass == null) {
            throw new IllegalArgumentException("not find " + clazz.getSimpleName() + " wrapper class");
        }
        return aClass;
    }


}
