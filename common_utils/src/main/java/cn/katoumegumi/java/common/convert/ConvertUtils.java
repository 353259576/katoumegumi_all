package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.BaseTypeCommon;
import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.model.WsRun;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConvertUtils {

    private static Class<?> getClass(Class<?> c){
        String className = c.getGenericInterfaces()[0].getTypeName();
        int start = className.indexOf("<");
        int end = className.lastIndexOf(">");
        if(start > -1 && end > -1){
            try {
                return Class.forName(className.substring(start + 1, end));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static final Map<Class,ConvertBean> CLASS_CONVERT_BEAN_MAP = new HashMap<>();

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
        ConvertToLocalDate convertToLocalDate = new ConvertToLocalDate();
        ConvertToLocalDateTime convertToLocalDateTime = new ConvertToLocalDateTime();

        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToString.getClass()),convertToString);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToBoolean.getClass()),convertToBoolean);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToCharacter.getClass()),convertToCharacter);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToByte.getClass()),convertToByte);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToShort.getClass()),convertToShort);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToInteger.getClass()),convertToInteger);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToLong.getClass()),convertToLong);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToFloat.getClass()),convertToFloat);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToDouble.getClass()),convertToDouble);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToBigInteger.getClass()),convertToBigInteger);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToBigDecimal.getClass()),convertToBigDecimal);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToDate.getClass()),convertToDate);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToSqlDate.getClass()),convertToSqlDate);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToLocalDate.getClass()),convertToLocalDate);
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertToLocalDateTime.getClass()),convertToLocalDateTime);
    }

    public static <T> T convert(Object o,Class<T> tClass){
        if(o == null){
            return null;
        }
        Class<?> c = null;
        if(tClass.isPrimitive()){
            c = BaseTypeCommon.getWrapperClass(tClass);
        }else {
            c = tClass;
        }
        if(o.getClass().equals(tClass)){
            return (T)o;
        }
        ConvertBean<T> convertBean = CLASS_CONVERT_BEAN_MAP.get(c);
        if(convertBean == null){
            return WsBeanUtils.convertBean(o,tClass);
        }else {
            return (T)convertBean.convert(o);
        }
    }

    /**
     * 增加转换规则
     *
     * @param convertBean
     * @param <T>
     */
    public static <T> void addConvertBean(ConvertBean<T> convertBean){
        CLASS_CONVERT_BEAN_MAP.put(getClass(convertBean.getClass()),convertBean);
    }

}
