package cn.katoumegumi.java.common.convert;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 转换为日期格式
 *
 * @author 星梦苍天
 */
public class ConvertToSqlTimestamp implements ConvertBean<Timestamp> {


    public Timestamp convertBean(Object bean) {
        Date date = ConvertUtils.convert(bean, Date.class);
        if (date == null) {
            return null;
        } else {
            return new Timestamp(date.getTime());
        }
    }

    @Override
    public Timestamp convert(Object bean) {
        return this.convertBean(bean);
    }
}
