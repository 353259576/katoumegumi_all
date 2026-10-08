package cn.katoumegumi.java.common.convert;

import java.sql.Date;

/**
 * 转换为日期格式
 *
 * @author 星梦苍天
 */
public class ToSqlDateConvert implements BaseTypeConvert<Date> {


    public Date convertBaseType(Object source) {
        java.util.Date date = ConvertUtils.convert(source, java.util.Date.class);
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }

    @Override
    public Date convert(Object source) {
        return this.convertBaseType(source);
    }
}
