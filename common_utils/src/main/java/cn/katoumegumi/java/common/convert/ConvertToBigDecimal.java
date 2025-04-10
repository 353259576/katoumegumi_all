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
public class ConvertToBigDecimal implements ConvertBean<BigDecimal> {

    public BigDecimal convertBean(Integer bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Short bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Byte bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Float bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Double bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Long bean) {
        return new BigDecimal(bean);
    }


    public BigDecimal convertBean(BigInteger bean) {
        return new BigDecimal(bean);
    }

    public BigDecimal convertBean(Date date) {
        return new BigDecimal(date.getTime());
    }

    public BigDecimal convertBean(java.sql.Date date) {
        return new BigDecimal(date.getTime());
    }

    public BigDecimal convertBean(LocalDate date) {
        return new BigDecimal(ConvertUtils.convert(date, Date.class).getTime());
    }

    public BigDecimal convertBean(LocalDateTime date) {
        return new BigDecimal(ConvertUtils.convert(date, Date.class).getTime());
    }

    public BigDecimal convertBean(String string){
        return WsStringUtils.isEmpty(string) ? null : new BigDecimal(string);
    }

    public BigDecimal convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.isEmpty(s) ? null : new BigDecimal(s);
    }

    @Override
    public BigDecimal convert(Object bean) {
        Class<?> tClass = bean.getClass();
        if (tClass == String.class) {
            return convertBean((String)bean);
        }else if (tClass == Integer.class){
            return convertBean((Integer) bean);
        } else if (tClass == Long.class){
            return convertBean((Long) bean);
        } else if (tClass == Double.class){
            return convertBean((Double) bean);
        } else if (tClass == Short.class){
            return convertBean((Short) bean);
        } else if (tClass == Byte.class){
            return convertBean((Byte) bean);
        } else if (tClass == Float.class){
            return convertBean((Float) bean);
        } else if (tClass == BigInteger.class){
            return convertBean((BigInteger) bean);
        } else if (tClass == Date.class){
            return convertBean((Date) bean);
        } else if (tClass == java.sql.Date.class){
            return convertBean((java.sql.Date) bean);
        } else if (tClass == LocalDate.class){
            return convertBean((LocalDate) bean);
        } else if (tClass == LocalDateTime.class){
            return convertBean((LocalDateTime) bean);
        }else {
            return convertBean(bean);
        }
    }
}
