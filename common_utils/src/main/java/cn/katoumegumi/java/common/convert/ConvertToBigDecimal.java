package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author 星梦苍天
 */
public class ConvertToBigDecimal implements ConvertBean<BigDecimal> {

    private final static Class<?>[] classes = new Class[]{
            Integer.class,
            Short.class,
            Byte.class,
            Float.class,
            Double.class,
            Long.class,
            BigInteger.class,
            Date.class,
            java.sql.Date.class,
            LocalDate.class,
            LocalDateTime.class
    };

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


    public BigDecimal convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : new BigDecimal(s);
    }

    @Override
    public BigDecimal convert(Object bean) {
        Class<?> tClass = bean.getClass();
        int i = 0;
        for (; i < classes.length; ++i) {
            if (classes[i] == tClass) {
                break;
            }
        }
        switch (i) {
            case 0:
                return convertBean((Integer) bean);
            case 1:
                return convertBean((Short) bean);
            case 2:
                return convertBean((Byte) bean);
            case 3:
                return convertBean((Float) bean);
            case 4:
                return convertBean((Double) bean);
            case 5:
                return convertBean((Long) bean);
            case 6:
                return convertBean((BigInteger) bean);
            case 7:
                return convertBean((Date) bean);
            case 8:
                return convertBean((java.sql.Date) bean);
            case 9:
                return convertBean((LocalDate) bean);
            case 10:
                return convertBean((LocalDateTime) bean);
            default:
                return convertBean(bean);
        }
    }
}
