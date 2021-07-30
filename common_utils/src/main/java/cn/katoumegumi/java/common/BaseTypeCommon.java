package cn.katoumegumi.java.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 基本类型
 *
 * @author ws
 */
public class BaseTypeCommon {

    private static final Set<Class<?>> CLASS_SET = new HashSet<>(18);

    private static final Set<Class<?>> ARRAY_CLASS_SET = new HashSet<>(18);

    private static final Class<?>[] BASE_TYPE_ARRAY = new Class[]{
            boolean.class,
            char.class,
            byte.class,
            short.class,
            int.class,
            float.class,
            long.class,
            double.class
    };

    private static final Class<?>[] WRAPPER_BASE_TYPE_ARRAY = new Class[]{
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Float.class,
            Long.class,
            Double.class
    };

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
    }

    public static boolean verify(Class<?> tClass) {
        return CLASS_SET.contains(tClass);
    }

    public static boolean verify(Object o) {
        return verify(o.getClass());
    }

    public static boolean verifyArray(Class<?> tClass) {
        return ARRAY_CLASS_SET.contains(tClass);
    }

    /**
     * 获取基本类型的包装类
     *
     * @param clazz
     * @return
     */
    public static Class<?> getWrapperClass(Class<?> clazz) {
        for (int i = 0; i < 8; i++) {
            if (clazz.equals(BASE_TYPE_ARRAY[i])) {
                return WRAPPER_BASE_TYPE_ARRAY[i];
            }
        }
        throw new NullPointerException("未发现" + clazz.getSimpleName() + "包装类");
    }


}
