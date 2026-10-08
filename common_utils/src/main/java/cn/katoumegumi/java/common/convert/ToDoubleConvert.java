package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为单精度浮点型
 *
 * @author 星梦苍天
 */
public class ToDoubleConvert implements BaseTypeConvert<Double> {

    public Double convertBaseType(Number source) {
        return source.doubleValue();
    }

    public Double convertBaseType(Boolean source) {
        return source ? 1D : 0D;
    }


    public Double convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : Double.valueOf(s);
    }

    public Double convertBaseType(String source) {
        return Double.parseDouble(source);
    }

    public Double convertBaseType(Date date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    public Double convertBaseType(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    public Double convertBaseType(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    @Override
    public Double convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        }else if (source instanceof String){
            return convertBaseType((String) source);
        }else if (source instanceof Boolean) {
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
