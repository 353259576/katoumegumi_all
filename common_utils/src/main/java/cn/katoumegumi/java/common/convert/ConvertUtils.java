package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsReflectUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertUtils {


    private static final Map<Class<?>, BaseTypeConvert<?>> BASE_TYPE_CONVERT_MAP = new HashMap<>();

    private static Class<?> resolveConvertTargetClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        Type type = null;
        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface.getTypeName().startsWith(BaseTypeConvert.class.getTypeName())){
                type = genericInterface;
                break;
            }
        }
        List<Class<?>> genericsTypes = WsReflectUtils.getGenericClass(type);
        if (genericsTypes.isEmpty()) {
            throw new IllegalArgumentException("convert target class " + clazz + " has no generics type");
        }
        return genericsTypes.get(0);
    }

    static {
        ToStringConvert convertToString = new ToStringConvert();
        ToBooleanConvert convertToBoolean = new ToBooleanConvert();
        ToCharacterConvert convertToCharacter = new ToCharacterConvert();
        ToByteConvert convertToByte = new ToByteConvert();
        ToShortConvert convertToShort = new ToShortConvert();
        ToIntegerConvert convertToInteger = new ToIntegerConvert();
        ToLongConvert convertToLong = new ToLongConvert();
        ToFloatConvert convertToFloat = new ToFloatConvert();
        ToDoubleConvert convertToDouble = new ToDoubleConvert();
        ToBigIntegerConvert convertToBigInteger = new ToBigIntegerConvert();
        ToBigDecimalConvert convertToBigDecimal = new ToBigDecimalConvert();
        ToDateConvert convertToDate = new ToDateConvert();
        ToSqlDateConvert convertToSqlDate = new ToSqlDateConvert();
        ToSqlTimestampConvert convertToSqlTimestamp = new ToSqlTimestampConvert();
        ToLocalDateConvert convertToLocalDate = new ToLocalDateConvert();
        ToLocalDateTimeConvert convertToLocalDateTime = new ToLocalDateTimeConvert();

        register(convertToString);
        register(convertToBoolean);
        register(convertToCharacter);
        register(convertToByte);
        register(convertToShort);
        register(convertToInteger);
        register(convertToLong);
        register(convertToFloat);
        register(convertToDouble);
        register(convertToBigInteger);
        register(convertToBigDecimal);
        register(convertToDate);
        register(convertToSqlDate);
        register(convertToLocalDate);
        register(convertToLocalDateTime);
        register(convertToSqlTimestamp);
    }

    private static void register(BaseTypeConvert<?> convert) {
        BASE_TYPE_CONVERT_MAP.put(resolveConvertTargetClass(convert.getClass()), convert);
    }

    public static <T> T convert(Object o, Class<T> targetClass) {
        if (o == null) {
            return null;
        } else if (targetClass == null) {
            throw new NullPointerException("convert target class is null");
        } else if (o.getClass() == targetClass) {
            return (T) o;
        }

        Class<?> c;
        if (targetClass.isPrimitive()) {
            c = BaseType.getWrapperClass(targetClass);
        } else {
            c = targetClass;
        }
        if (c.isInstance(o)) {
            return (T) o;
        }
        BaseTypeConvert<T> baseTypeConvert = (BaseTypeConvert<T>) BASE_TYPE_CONVERT_MAP.get(c);
        if (baseTypeConvert == null) {
            // 防止 base→bean 无限递归：baseTypeConvert 对 base 源会再次走 baseTypeConvert
            // → ConvertUtils.convert → baseTypeConvert …。若源是基本类型且目标无注册转换器，
            // 说明无法转换，直接返回 null 而不再回调 baseTypeConvert。
            if (BaseType.isBaseType(o.getClass())) {
                return null;
            }
            return WsBeanUtils.convertBean(o, targetClass);
        } else {
            return baseTypeConvert.convert(o);
        }
    }

    /**
     * 增加转换规则
     *
     * @param baseTypeConvert
     * @param <T>
     */
    public synchronized static <T> void addBaseTypeConvert(BaseTypeConvert<T> baseTypeConvert) {
        BASE_TYPE_CONVERT_MAP.put(resolveConvertTargetClass(baseTypeConvert.getClass()), baseTypeConvert);
    }

    public static <T> BaseTypeConvert<T> getBaseTypeConvert(Class<T> c) {
        return (BaseTypeConvert<T>) BASE_TYPE_CONVERT_MAP.get(c);
    }

}
