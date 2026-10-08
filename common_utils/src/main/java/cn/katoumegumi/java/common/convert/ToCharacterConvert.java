package cn.katoumegumi.java.common.convert;

import cn.katoumegumi.java.common.WsStringUtils;

/**
 * 转换为字符类型
 *
 * @author 星梦苍天
 */
public class ToCharacterConvert implements BaseTypeConvert<Character> {

    public Character convertBaseType(Number source) {
        return (char) (source.byteValue() & 0xFF);
    }

    public Character convertBaseType(Boolean source) {
        return source ? (char) 1 : (char) 0;
    }

    public Character convertBaseType(String source) {
        if (source.isEmpty()) {
            return null;
        } else {
            return source.charAt(0);
        }
    }

    public Character convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        return WsStringUtils.isEmpty(s) ? null : convertBaseType(s);
    }

    @Override
    public Character convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else if (source instanceof String) {
            return convertBaseType((String) source);
        } else if (source instanceof Boolean) {
            return convertBaseType((Boolean) source);
        } else {
            return this.convertBaseType(source);
        }
    }
}
