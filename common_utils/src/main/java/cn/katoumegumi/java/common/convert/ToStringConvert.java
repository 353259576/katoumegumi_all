package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsDateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为字符串
 *
 * @author 星梦苍天
 */
public class ToStringConvert implements BaseTypeConvert<String> {

    public String convertBaseType(Object source) {
        return source.toString();
    }

    /**
     * 数字转String
     *
     * @param source
     * @return
     */
    public String convertBaseType(Number source) {
        return source.toString();
    }

    public String convertBaseType(Date source) {
        return WsDateUtils.dateToString(source, WsDateUtils.DEFAULT_TIME_TEMPLATE);
    }

    public String convertBaseType(java.sql.Date source) {
        return WsDateUtils.dateToString(source, WsDateUtils.DEFAULT_TIME_TEMPLATE);
    }

    public String convertBaseType(byte[] source) {
        return new String(source);
    }

    public String convertBaseType(LocalDate source) {
        return source.toString();
    }

    public String convertBaseType(LocalDateTime source) {
        return source.toString();
    }

    public String convertBaseType(Boolean source) {
        return source ? "1" : "0";
    }

    @Override
    public String convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof java.sql.Date) {
            return convertBaseType((java.sql.Date) source);
        } else if (source instanceof Date) {
            return convertBaseType((Date) source);
        } else if (source instanceof LocalDateTime) {
            return convertBaseType((LocalDateTime) source);
        } else if (source instanceof LocalDate) {
            return convertBaseType((LocalDate) source);
        } else if (source instanceof byte[]) {
            return convertBaseType((byte[]) source);
        } else if (source instanceof Boolean) {
            return convertBaseType((Boolean) source);
        } else {
            return this.convertBaseType(source);
        }
    }
}
