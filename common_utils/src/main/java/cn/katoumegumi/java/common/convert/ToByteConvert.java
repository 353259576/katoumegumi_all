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
public class ToByteConvert implements BaseTypeConvert<Byte> {

    public Byte convertBaseType(Number source) {
        return source.byteValue();
    }

    public Byte convertBaseType(Boolean source) {
        return source ? (byte) 1 : (byte) 0;
    }

    public Byte convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : Byte.valueOf(s);
    }

    public Byte convertBaseType(Date date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    public Byte convertBaseType(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    public Byte convertBaseType(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).byteValue();
    }

    @Override
    public Byte convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof String) {
            return convertBaseType(source);
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
