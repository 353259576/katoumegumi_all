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
public class ToBigIntegerConvert implements BaseTypeConvert<BigInteger> {

    public BigInteger convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : new BigInteger(s);
    }

    public BigInteger convertBaseType(Date date) {
        return new BigInteger(String.valueOf(date.getTime()));
    }

    public BigInteger convertBaseType(java.sql.Date date) {
        return new BigInteger(String.valueOf(date.getTime()));
    }

    public BigInteger convertBaseType(LocalDate date) {
        return new BigInteger(String.valueOf(ConvertUtils.convert(date, Date.class).getTime()));
    }

    public BigInteger convertBaseType(LocalDateTime date) {
        return new BigInteger(String.valueOf(ConvertUtils.convert(date, Date.class).getTime()));
    }

    @Override
    public BigInteger convert(Object source) {
        if (source instanceof String) {
            return this.convertBaseType(source);
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
