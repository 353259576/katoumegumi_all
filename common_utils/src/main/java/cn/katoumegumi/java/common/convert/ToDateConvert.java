package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsDateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 转换为日期
 *
 * @author 星梦苍天
 */
public class ToDateConvert implements BaseTypeConvert<Date> {

    public Date convertBaseType(Number source) {
        return new Date(source.longValue());
    }

    public Date convertBaseType(String source) {
        return WsDateUtils.stringToDate(source);
    }

    public Date convertBaseType(java.sql.Date source) {
        return WsDateUtils.objectToDate(source);
    }

    public Date convertBaseType(LocalDate source) {
        ZonedDateTime zonedDateTime = source.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public Date convertBaseType(LocalDateTime source) {
        ZonedDateTime zonedDateTime = source.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public Date convertBaseType(Object source) {
        return WsDateUtils.objectToDate(source);
        /*String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : WsDateUtils.stringToDate(WsDateUtils.dateStringFormat(s));*/
    }

    @Override
    public Date convert(Object source) {
        if (source instanceof String) {
            return convertBaseType((String) source);
        } else if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof LocalDateTime) {
            return convertBaseType((LocalDateTime) source);
        } else if (source instanceof LocalDate) {
            return convertBaseType((LocalDate) source);
        } else if (source instanceof java.sql.Date) {
            return convertBaseType((java.sql.Date) source);
        } else {
            return this.convertBaseType(source);
        }

    }
}
