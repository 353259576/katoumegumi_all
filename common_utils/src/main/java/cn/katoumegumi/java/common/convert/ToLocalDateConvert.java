package cn.katoumegumi.java.common.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author 星梦苍天
 */
public class ToLocalDateConvert implements BaseTypeConvert<LocalDate> {

    public LocalDate convertBaseType(LocalDateTime source) {
        return source.toLocalDate();
    }

    public LocalDate convertBaseType(Object source) {
        Date date = ConvertUtils.convert(source, Date.class);
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate convert(Object source) {
        if (source instanceof LocalDateTime) {
            return ((LocalDateTime) source).toLocalDate();
        } else {
            return this.convertBaseType(source);
        }
    }
}
