package cn.katoumegumi.java.common.convert;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 转换为日期格式
 *
 * @author 星梦苍天
 */
public class ToSqlTimestampConvert implements BaseTypeConvert<Timestamp> {


    public Timestamp convertBaseType(Object source) {
        Date date = ConvertUtils.convert(source, Date.class);
        if (date == null) {
            return null;
        } else {
            return new Timestamp(date.getTime());
        }
    }

    @Override
    public Timestamp convert(Object source) {
        return this.convertBaseType(source);
    }
}
