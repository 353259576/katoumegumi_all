package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为字节类型
 *
 * @author 星梦苍天
 */
public class ConvertToByte implements ConvertBean<Byte> {

    public Byte convertBean(Number bean) {
        return bean.byteValue();
    }

    public Byte convertBean(Boolean bean) {
        return bean ? (byte) 1 : (byte) 0;
    }

    public Byte convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.isEmpty(s) ? null : Byte.valueOf(s);
    }

    public Byte convertBean(Date date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    public Byte convertBean(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    public Byte convertBean(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    @Override
    public Byte convert(Object bean) {
        if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof String) {
            return convertBean(bean);
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
