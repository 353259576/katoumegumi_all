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
public class ConvertToDouble implements ConvertBean<Double> {

    public Double convertBean(Number bean) {
        return bean.doubleValue();
    }

    public Double convertBean(Boolean bean) {
        return bean ? 1D : 0D;
    }


    public Double convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.isEmpty(s) ? null : Double.valueOf(s);
    }

    public Double convertBean(String bean) {
        return Double.parseDouble(bean);
    }

    public Double convertBean(Date date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    public Double convertBean(LocalDate date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    public Double convertBean(LocalDateTime date) {
        return ConvertUtils.convert(date, Long.class).doubleValue();
    }

    @Override
    public Double convert(Object bean) {
        if (bean instanceof Number) {
            return convertBean((Number) bean);
        }else if (bean instanceof String){
            return convertBean((String) bean);
        }else if (bean instanceof Boolean) {
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
