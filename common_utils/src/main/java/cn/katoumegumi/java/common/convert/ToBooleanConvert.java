package cn.katoumegumi.java.common.convert;

import java.util.HashMap;
import java.util.Map;

/**
 * 转换为布尔类型
 *
 * @author 星梦苍天
 */
public class ToBooleanConvert implements BaseTypeConvert<Boolean> {

    private final Map<String, Boolean> map = new HashMap<>();

    {
        map.put("是", true);
        map.put("否", false);
        map.put("1", true);
        map.put("0", false);
        map.put("yes", true);
        map.put("no", false);
        map.put("y", true);
        map.put("n", false);
        map.put("Y", true);
        map.put("N", false);
        map.put("YES", true);
        map.put("NO", false);
        map.put("true", true);
        map.put("false", false);
        map.put("TRUE", true);
        map.put("FALSE", false);
    }

    public Boolean convertBaseType(Number source) {
        return source.intValue() == 1;
    }


    public Boolean convertBaseType(Object source) {
        String s = ConvertUtils.convert(source, String.class);
        if (s == null) {
            return null;
        } else {
            return map.get(s);
        }
    }

    @Override
    public Boolean convert(Object source) {
        if (source instanceof Number) {
            return convertBaseType((Number) source);
        } else {
            return this.convertBaseType(source);
        }
    }
}
