package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.convert.ConvertBean;
import cn.katoumegumi.java.common.convert.ConvertUtils;

/**
 * 转换为字符类型
 *
 * @author 星梦苍天
 */
public class ConvertToCharacter implements ConvertBean<Character> {

    public Character convertBean(Number bean) {
        return (char) (bean.byteValue() & 0xFF);
    }

    public Character convertBean(Boolean bean) {
        return bean ? (char) 1 : (char) 0;
    }

    public Character convertBean(String bean) {
        if (bean.length() == 0) {
            return null;
        } else {
            return bean.charAt(0);
        }
    }

    public Character convertBean(Object bean) {
        String s = ConvertUtils.convert(bean, String.class);
        return WsStringUtils.notHasLength(s) ? null : convertBean(s);
    }

    @Override
    public Character convert(Object bean) {
        if (bean instanceof Number) {
            return convertBean((Number) bean);
        } else if (bean instanceof String) {
            return convertBean((String) bean);
        } else if (bean instanceof Boolean) {
            return convertBean((Boolean) bean);
        } else {
            return this.convertBean(bean);
        }
    }
}
