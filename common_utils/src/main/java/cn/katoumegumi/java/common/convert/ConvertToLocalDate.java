package cn.katoumegumi.java.common.convert;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author 星梦苍天
 */
public class ConvertToLocalDate implements ConvertBean<LocalDate> {

    public LocalDate convertBean(LocalDateTime bean){
        return bean.toLocalDate();
    }

    public LocalDate convertBean(Object bean) {
        Date date = ConvertUtils.convert(bean, Date.class);
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate convert(Object bean) {
        if(bean instanceof LocalDateTime){
            return ((LocalDateTime) bean).toLocalDate();
        }else {
            return this.convertBean(bean);
        }
    }
}
