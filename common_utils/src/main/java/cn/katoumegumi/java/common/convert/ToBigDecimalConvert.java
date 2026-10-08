package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author 星梦苍天
 */
public class ToBigDecimalConvert implements BaseTypeConvert<BigDecimal> {

    public BigDecimal convertBaseType(Integer source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Short source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Byte source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Float source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Double source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Long source) {
        return new BigDecimal(source);
    }


    public BigDecimal convertBaseType(BigInteger source) {
        return new BigDecimal(source);
    }

    public BigDecimal convertBaseType(Date date) {
        return new BigDecimal(date.getTime());
    }

    public BigDecimal convertBaseType(java.sql.Date date) {
        return new BigDecimal(date.getTime());
    }

    public BigDecimal convertBaseType(LocalDate date) {
        return new BigDecimal(ConvertUtils.convert(date, Date.class).getTime());
    }

    public BigDecimal convertBaseType(LocalDateTime date) {
        return new BigDecimal(ConvertUtils.convert(date, Date.class).getTime());
    }

    public BigDecimal convertBaseType(String string){
        return WsStringUtils.isEmpty(string) ? null : new BigDecimal(string);
    }

    public BigDecimal convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : new BigDecimal(s);
    }

    @Override
    public BigDecimal convert(Object source) {
        Class<?> tClass = source.getClass();
        if (tClass == String.class) {
            return convertBaseType((String)source);
        }else if (tClass == Integer.class){
            return convertBaseType((Integer) source);
        } else if (tClass == Long.class){
            return convertBaseType((Long) source);
        } else if (tClass == Double.class){
            return convertBaseType((Double) source);
        } else if (tClass == Short.class){
            return convertBaseType((Short) source);
        } else if (tClass == Byte.class){
            return convertBaseType((Byte) source);
        } else if (tClass == Float.class){
            return convertBaseType((Float) source);
        } else if (tClass == BigInteger.class){
            return convertBaseType((BigInteger) source);
        } else if (tClass == Date.class){
            return convertBaseType((Date) source);
        } else if (tClass == java.sql.Date.class){
            return convertBaseType((java.sql.Date) source);
        } else if (tClass == LocalDate.class){
            return convertBaseType((LocalDate) source);
        } else if (tClass == LocalDateTime.class){
            return convertBaseType((LocalDateTime) source);
        }else {
            return convertBaseType(source);
        }
    }
}
