package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为整形
 *
 * @author 星梦苍天
 */
public class ToIntegerConvert implements BaseTypeConvert<Integer> {

    public Integer convertBaseType(Number source) {
        return source.intValue();
    }

    public Integer convertBaseType(Boolean source) {
        return source ? 1 : 0;
    }

    public Integer convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : Integer.valueOf(s);
    }

    public Integer convertBaseType(Date date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    public Integer convertBaseType(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    public Integer convertBaseType(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    @Override
    public Integer convert(Object source) {
        if (source instanceof String) {
            return convertBaseType(source);
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
            return this.convertBaseType(source);
        }

    }
}
