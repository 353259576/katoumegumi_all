package cn.katoumegumi.java.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 翻译表名和列名
 * @author ws
 */
public class TranslateNameUtils {

    /**
     * 简写数据
     */
    private final Map<String, String> abbreviationMap = new HashMap<>();

    /**
     * 详细数据
     */
    private final Map<String, String> particularMap = new HashMap<>();

    /**
     * 缩写防重复
     */
    private final AtomicInteger abbreviationNum = new AtomicInteger();


    /**
     * 创建简称
     *
     * @param keyword
     * @return
     */
    public String createAbbreviation(String keyword) {
        if (keyword.length() < 2) {
            return keyword + '_' + abbreviationNum.getAndAdd(1);
        } else {
            return keyword.substring(0, 1) + '_' + abbreviationNum.getAndAdd(1);
        }
    }

    /**
     * 获取详细名称
     * @param value 简称
     * @return
     */
    public String getParticular(String value) {
        return particularMap.get(value);
    }


    /**
     * 获取简称
     * @param keyword
     * @return
     */
    public String getAbbreviation(String keyword) {
        String value = abbreviationMap.get(keyword);
        if (value == null) {
            value = particularMap.get(keyword);
            if (value == null) {
                value = createAbbreviation(keyword);
                abbreviationMap.put(keyword, value);
                particularMap.put(value, keyword);
                return value;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    /**
     * 设置简称
     * @param keyword
     * @return
     */
    public String setAbbreviation(String keyword, String value) {
        abbreviationMap.put(keyword, value);
        particularMap.put(value, keyword);
        return value;
    }

}
