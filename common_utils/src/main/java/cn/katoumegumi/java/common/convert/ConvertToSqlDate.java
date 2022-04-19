package cn.katoumegumi.java.common.convert;

import java.sql.Date;

/**
 * 转换为日期格式
 *
 * @author 星梦苍天
 */
public class ConvertToSqlDate implements ConvertBean<Date> {


    public Date convertBean(Object bean) {
        java.util.Date date = ConvertUtils.convert(bean, java.util.Date.class);
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }

    @Override
    public Date convert(Object bean) {
        return this.convertBean(bean);
    }
}
