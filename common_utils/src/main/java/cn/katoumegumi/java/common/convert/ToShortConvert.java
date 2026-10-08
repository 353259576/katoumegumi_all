package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为短整形
 *
 * @author 星梦苍天
 */
public class ToShortConvert implements BaseTypeConvert<Short> {

    public Short convertBaseType(Number source) {
        return source.shortValue();
    }

    public Short convertBaseType(Boolean source) {
        return source ? (short) 1 : (short) 0;
    }

    public Short convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : Short.valueOf(s);
    }

    public Short convertBaseType(Date date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    public Short convertBaseType(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    public Short convertBaseType(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).shortValue();
    }

    @Override
    public Short convert(Object source) {
        if (source instanceof String) {
            return this.convertBaseType(source);
        } else if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof Boolean) {
            return convertBaseType((Boolean) source);
        } else if (source instanceof Date) {
            return convertBaseType((Date) source);
        } else if (source instanceof LocalDate) {
            return convertBaseType((LocalDate) source);
        } else if (source instanceof LocalDateTime) {
            return convertBaseType((LocalDateTime) source);
        } else {
            return convertBaseType(source);
        }

    }
}
