package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为整形
 *
 * @author 星梦苍天
 */
public class ConvertToInteger implements ConvertBean<Integer> {

    public Integer convertBean(Number bean) {
        return bean.intValue();
    }

    public Integer convertBean(Boolean bean) {
        return bean ? 1 : 0;
    }

    public Integer convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : Integer.valueOf(s);
    }

    public Integer convertBean(Date date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    public Integer convertBean(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    public Integer convertBean(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).intValue();
    }

    @Override
    public Integer convert(Object bean) {
        if (bean instanceof String) {
            return convertBean(bean);
        } else if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof Boolean) {
            return convertBean((Boolean) bean);
        } else if (bean instanceof Date) {
            return convertBean((Date) bean);
        } else if (bean instanceof LocalDate) {
            return convertBean((LocalDate) bean);
        } else if (bean instanceof LocalDateTime) {
            return convertBean((LocalDateTime) bean);
        } else {
            return this.convertBean(bean);
        }

    }
}
