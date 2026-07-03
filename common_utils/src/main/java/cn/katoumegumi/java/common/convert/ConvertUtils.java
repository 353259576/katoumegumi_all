package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.BaseTypeCommon;
import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsReflectUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertUtils {


    private static final Map<Class<?>, ConvertBean<?>> CLASS_CONVERT_BEAN_MAP = new HashMap<>();

    private static Class<?> resolveConvertTargetClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        Type type = null;
        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface.getTypeName().startsWith(ConvertBean.class.getTypeName())){
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
        ConvertToString convertToString = new ConvertToString();
        ConvertToBoolean convertToBoolean = new ConvertToBoolean();
        ConvertToCharacter convertToCharacter = new ConvertToCharacter();
        ConvertToByte convertToByte = new ConvertToByte();
        ConvertToShort convertToShort = new ConvertToShort();
        ConvertToInteger convertToInteger = new ConvertToInteger();
        ConvertToLong convertToLong = new ConvertToLong();
        ConvertToFloat convertToFloat = new ConvertToFloat();
        ConvertToDouble convertToDouble = new ConvertToDouble();
        ConvertToBigInteger convertToBigInteger = new ConvertToBigInteger();
        ConvertToBigDecimal convertToBigDecimal = new ConvertToBigDecimal();
        ConvertToDate convertToDate = new ConvertToDate();
        ConvertToSqlDate convertToSqlDate = new ConvertToSqlDate();
        ConvertToSqlTimestamp convertToSqlTimestamp = new ConvertToSqlTimestamp();
        ConvertToLocalDate convertToLocalDate = new ConvertToLocalDate();
        ConvertToLocalDateTime convertToLocalDateTime = new ConvertToLocalDateTime();

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

    private static void register(ConvertBean<?> bean) {
        CLASS_CONVERT_BEAN_MAP.put(resolveConvertTargetClass(bean.getClass()), bean);
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
            c = BaseTypeCommon.getWrapperClass(targetClass);
        } else {
            c = targetClass;
        }
        if (c.isInstance(o)) {
            return (T) o;
        }
        ConvertBean<T> convertBean = (ConvertBean<T>) CLASS_CONVERT_BEAN_MAP.get(c);
        if (convertBean == null) {
            // 防止 base→bean 无限递归：convertBean 对 base 源会再次走 baseTypeConvert
            // → ConvertUtils.convert → convertBean …。若源是基本类型且目标无注册转换器，
            // 说明无法转换，直接返回 null 而不再回调 convertBean。
            if (BaseTypeCommon.isBaseType(o.getClass())) {
                return null;
            }
            return WsBeanUtils.convertBean(o, targetClass);
        } else {
            return convertBean.convert(o);
        }
    }

    /**
     * 增加转换规则
     *
     * @param convertBean
     * @param <T>
     */
    public synchronized static <T> void addConvertBean(ConvertBean<T> convertBean) {
        CLASS_CONVERT_BEAN_MAP.put(resolveConvertTargetClass(convertBean.getClass()), convertBean);
    }

    public static <T> ConvertBean<T> getConvertBean(Class<T> c) {
        return (ConvertBean<T>) CLASS_CONVERT_BEAN_MAP.get(c);
    }

}
