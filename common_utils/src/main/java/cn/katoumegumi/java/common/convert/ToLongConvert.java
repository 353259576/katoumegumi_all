package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 转换为长整形
 *
 * @author 星梦苍天
 */
public class ToLongConvert implements BaseTypeConvert<Long> {

    public Long convertBaseType(Number source) {
        return source.longValue();
    }

    public Long convertBaseType(Boolean source) {
        return source ? 1L : 0L;
    }

    public Long convertBaseType(Date date) {
        return date.getTime();
    }

    public Long convertBaseType(java.sql.Date date) {
        return date.getTime();
    }

    public Long convertBaseType(LocalDate localDate) {
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant()).getTime();
    }


    public Long convertBaseType(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant()).getTime();
    }


    public Long convertBaseType(Object source) {
        String o = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(o) ? null : Long.valueOf(o);
    }

    @Override
    public Long convert(Object source) {
        if (source instanceof String) {
            return this.convertBaseType(source);
        } else if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof java.sql.Date) {
            return convertBaseType((java.sql.Date) source);
        } else if (source instanceof Date) {
            return convertBaseType((Date) source);
        } else if (source instanceof LocalDateTime) {
            return convertBaseType((LocalDateTime) source);
        } else if (source instanceof LocalDate) {
            return convertBaseType((LocalDate) source);
        } else if (source instanceof Boolean) {
            return convertBaseType((Boolean) source);
        } else {
            return convertBaseType(source);
        }

    }
}
