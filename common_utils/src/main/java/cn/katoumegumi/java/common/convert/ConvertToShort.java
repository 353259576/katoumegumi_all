package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为短整形
 *
 * @author 星梦苍天
 */
public class ConvertToShort implements ConvertBean<Short> {

    public Short convertBean(Number bean) {
        return bean.shortValue();
    }

    public Short convertBean(Boolean bean) {
        return bean ? (short) 1 : (short) 0;
    }

    public Short convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : Short.valueOf(s);
    }

    public Short convertBean(Date date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    public Short convertBean(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    public Short convertBean(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    @Override
    public Short convert(Object bean) {
        if (bean instanceof String) {
            return this.convertBean(bean);
        } else if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof Boolean) {
            return convertBean((Boolean) bean);
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
