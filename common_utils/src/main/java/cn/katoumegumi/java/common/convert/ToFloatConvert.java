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
public class ToFloatConvert implements BaseTypeConvert<Float> {

    public Float convertBaseType(Number source) {
        return source.floatValue();
    }

    public Float convertBaseType(Boolean source) {
        return source ? 1F : 0F;
    }


    public Float convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : Float.valueOf(s);
    }

    public Float convertBaseType(Date date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    public Float convertBaseType(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    public Float convertBaseType(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).floatValue();
    }

    @Override
    public Float convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof String) {
            return convertBaseType(source);
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
