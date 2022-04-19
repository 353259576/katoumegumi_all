package cn.katoumegumi.java.common.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author 星梦苍天
 */
public class ConvertToLocalDateTime implements ConvertBean<LocalDateTime> {

    public LocalDateTime convertBean(LocalDate bean) {
        return bean.atStartOfDay();
    }

    public LocalDateTime convertBean(Object bean) {
        Date date = ConvertUtils.convert(bean, Date.class);
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public LocalDateTime convert(Object bean) {
        if (bean instanceof LocalDate) {
            return convertBean((LocalDate) bean);
        } else {
            return this.convertBean(bean);
        }

    }
}
