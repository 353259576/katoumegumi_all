package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为单精度浮点型
 *
 * @author 星梦苍天
 */
public class ConvertToFloat implements ConvertBean<Float> {

    public Float convertBean(Number bean) {
        return bean.floatValue();
    }

    public Float convertBean(Boolean bean) {
        return bean ? 1F : 0F;
    }


    public Float convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : Float.valueOf(s);
    }

    public Float convertBean(Date date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    public Float convertBean(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    public Float convertBean(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    @Override
    public Float convert(Object bean) {
        if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof String) {
            return convertBean(bean);
        } else if (bean instanceof Boolean) {
            return convertBean((Boolean) bean);
        } else if (bean instanceof Date) {
            return convertBean((Date) bean);
        } else if (bean instanceof LocalDate) {
            return convertBean((LocalDate) bean);
        } else if (bean instanceof LocalDateTime) {
            return convertBean((LocalDateTime) bean);
        } else {
            return this.convertBean(bean);
        }
    }
}
