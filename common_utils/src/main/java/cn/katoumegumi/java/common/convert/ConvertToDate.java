package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
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
public class ConvertToDate implements ConvertBean<Date> {

    public Date convertBean(Number bean) {
        return new Date(bean.longValue());
    }

    public Date convertBean(String bean) {
        return WsDateUtils.stringToDate(bean);
    }

    public Date convertBean(java.sql.Date bean) {
        return bean;
    }

    public Date convertBean(LocalDate bean) {
        ZonedDateTime zonedDateTime = bean.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public Date convertBean(LocalDateTime bean) {
        ZonedDateTime zonedDateTime = bean.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public Date convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : WsDateUtils.stringToDate(WsDateUtils.dateStringFormat(s), WsDateUtils.LONGTIMESTRING);
    }

    @Override
    public Date convert(Object bean) {
        if (bean instanceof String) {
            return convertBean((String) bean);
        } else if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof LocalDateTime) {
            return convertBean((LocalDateTime) bean);
        } else if (bean instanceof LocalDate) {
            return convertBean((LocalDate) bean);
        } else if (bean instanceof java.sql.Date) {
            return convertBean((java.sql.Date) bean);
        } else {
            return this.convertBean(bean);
        }

    }
}
