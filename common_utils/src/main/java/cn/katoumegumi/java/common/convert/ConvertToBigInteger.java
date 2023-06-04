package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为单精度浮点型
 *
 * @author 星梦苍天
 */
public class ConvertToBigInteger implements ConvertBean<BigInteger> {

    public BigInteger convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : new BigInteger(s);
    }

    public BigInteger convertBean(Date date) {
        return new BigInteger(String.valueOf(date.getTime()));
    }

    public BigInteger convertBean(java.sql.Date date) {
        return new BigInteger(String.valueOf(date.getTime()));
    }

    public BigInteger convertBean(LocalDate date) {
        return new BigInteger(String.valueOf(ConvertUtils.convert(date, Date.class).getTime()));
    }

    public BigInteger convertBean(LocalDateTime date) {
        return new BigInteger(String.valueOf(ConvertUtils.convert(date, Date.class).getTime()));
    }

    @Override
    public BigInteger convert(Object bean) {
        if (bean instanceof String) {
            return this.convertBean(bean);
        } else if (bean instanceof Date) {
            return convertBean((Date) bean);
        } else if (bean instanceof LocalDate) {
            return convertBean((LocalDate) bean);
        } else if (bean instanceof LocalDateTime) {
            return convertBean((LocalDateTime) bean);
        } else {
            return convertBean(bean);
        }

    }
}
