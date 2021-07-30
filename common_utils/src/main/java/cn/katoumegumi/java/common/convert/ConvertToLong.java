package cn.katoumegumi.java.common.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * 转换为长整形
 * @author 星梦苍天
 */
public class ConvertToLong implements ConvertBean<Long>{

    public Long convertBean(Number bean){
        return bean.longValue();
    }

    public Long convertBean(Boolean bean) {
        return bean ? 1L : 0L;
    }

    public Long convertBean(Date date){
        return date.getTime();
    }

    public Long convertBean(java.sql.Date date){
        return date.getTime();
    }

    public Long convertBean(LocalDate localDate){
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant()).getTime();
    }


    public Long convertBean(LocalDateTime localDateTime){
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant()).getTime();
    }


    public Long convertBean(Object bean) {
        String o = ConvertUtils.convert(bean,String.class);
        return o==null?null:Long.valueOf(o);
    }

    @Override
    public Long convert(Object bean) {
        if(bean instanceof String){
            return this.convertBean(bean);
        }else if(bean instanceof Number){
            return convertBean((Number) bean);
        }else if(bean instanceof java.sql.Date){
            return convertBean((java.sql.Date) bean);
        }else if(bean instanceof Date){
            return convertBean((Date) bean);
        }else if(bean instanceof LocalDateTime){
            return convertBean((LocalDateTime) bean);
        }else if(bean instanceof LocalDate){
            return convertBean((LocalDate) bean);
        }else if(bean instanceof Boolean){
            return convertBean((Boolean) bean);
        }else {
            return convertBean(bean);
        }

    }
}
