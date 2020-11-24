package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsStringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 翻译表名和列名
 * @author ws
 */
public class TranslateNameUtils {

    private final List<String> mainClassNameList = new ArrayList<>();

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
     * 本地对象与表的对应关系
     */
    private final Map<String, FieldColumnRelationMapper> localMapperMap = new HashMap<>();


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
     */
    public void setAbbreviation(String keyword, String value) {
        abbreviationMap.put(keyword, value);
        particularMap.put(value, keyword);
    }

    /**
     * 获取本地缓存的mapper
     * @param locationName
     * @return
     */
    public FieldColumnRelationMapper getLocalMapper(String locationName){
        return localMapperMap.get(locationName);
    }

    /**
     * 添加本地缓存的mapper
     * @param locationName
     * @param mapper
     */
    public void addLocalMapper(String locationName,FieldColumnRelationMapper mapper){
        localMapperMap.put(locationName,mapper);
    }

    public int locationMapperSize(){
        return localMapperMap.size();
    }

    public void addMainClassName(String mainClassName){
        this.mainClassNameList.add(mainClassName+'.');
    }

    public boolean startsWithMainClassName(String mainClassName){
        final String tableName = mainClassName + '.';
        for(String prefix:mainClassNameList) {
            if (tableName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去除表别名的主表名称
     * @return
     */
    public String getNoPrefixTableName(final String tableName) {
        if (WsStringUtils.isBlank(tableName)) {
            return null;
        }

        for(String prefix:mainClassNameList) {
            if (tableName.startsWith(prefix)) {
                return tableName.substring(prefix.length());
            }
        }
        return tableName;
    }
}
