package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsDateUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 转换为字符串
 * @author 星梦苍天
 */
public class ConvertToString implements ConvertBean<String> {

    public String convertBean(Object bean) {
        return bean.toString();
    }

    /**
     * 数字转String
     * @param bean
     * @return
     */
    public String convertBean(Number bean) {
        return bean.toString();
    }

    public String convertBean(Date bean){
        return WsDateUtils.dateToString(bean,WsDateUtils.DEFAULT_TIME_TEMPLATE);
    }

    public String convertBean(java.sql.Date bean){
        return WsDateUtils.dateToString(bean,WsDateUtils.DEFAULT_TIME_TEMPLATE);
    }

    public String convertBean(byte[] bean){
        return new String(bean);
    }

    public String convertBean(LocalDate bean){
        return bean.toString();
    }

    public String convertBean(LocalDateTime bean){
        return bean.toString();
    }

    public String convertBean(Boolean bean){
        return bean?"1":"0";
    }

    @Override
    public String convert(Object bean) {
        if(bean instanceof Number){
            return convertBean((Number) bean);
        }else if(bean instanceof java.sql.Date){
            return convertBean((java.sql.Date) bean);
        }else if(bean instanceof Date){
            return convertBean((Date) bean);
        }else if(bean instanceof LocalDateTime){
            return convertBean((LocalDateTime) bean);
        }else if(bean instanceof LocalDate){
            return convertBean((LocalDate) bean);
        }else if(bean instanceof byte[]){
            return convertBean((byte[]) bean);
        }else if (bean instanceof Boolean){
            return convertBean((Boolean) bean);
        } else {
            return this.convertBean(bean);
        }
    }
}
