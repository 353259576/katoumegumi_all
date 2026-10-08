package cn.katoumegumi.java.common.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author 星梦苍天
 */
public class ToLocalDateTimeConvert implements BaseTypeConvert<LocalDateTime> {

    public LocalDateTime convertBaseType(LocalDate source) {
        return source.atStartOfDay();
    }

    public LocalDateTime convertBaseType(Object source) {
        Date date = ConvertUtils.convert(source, Date.class);
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime convert(Object source) {
        if (source instanceof LocalDate) {
            return convertBaseType((LocalDate) source);
        } else {
            return this.convertBaseType(source);
        }

    }
}
