package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 翻译表名和列名
 *
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
    public FieldColumnRelationMapper getLocalMapper(String locationName) {
        return localMapperMap.get(locationName);
    }

    /**
     * 添加本地缓存的mapper
     * @param locationName
     * @param mapper
     */
    public void addLocalMapper(String locationName, FieldColumnRelationMapper mapper) {
        localMapperMap.put(locationName, mapper);
    }

    public int locationMapperSize() {
        return localMapperMap.size();
    }

    public void addMainClassName(String mainClassName) {
        this.mainClassNameList.add(mainClassName + '.');
    }

    public boolean startsWithMainClassName(String mainClassName) {
        final String tableName = mainClassName + '.';
        for (String prefix : mainClassNameList) {
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
        for (String prefix : mainClassNameList) {
            if (tableName.startsWith(prefix)) {
                return tableName.substring(prefix.length());
            }
        }
        return tableName;
    }

    /**
     * 根据查询条件生成列基本信息
     * @param originalFieldName
     * @param prefix
     * @param type 1 返回的列名 1 查询的列名
     * @return
     */
    public ColumnBaseEntity getColumnBaseEntity(String originalFieldName, String prefix,int type) {
        String prefixString;
        String fieldName;
        originalFieldName = translateTableNickName(prefix,originalFieldName);
        List<String> fieldNameList = WsStringUtils.split(originalFieldName, '.');
        int size = fieldNameList.size();
        if (size == 1) {
            fieldName = fieldNameList.get(0);
            prefixString = prefix;
        } else if (size == 2) {
            fieldName = fieldNameList.get(1);
            String key = getParticular(fieldNameList.get(0));
            if (key == null) {
                if (!startsWithMainClassName(fieldNameList.get(0))) {
                    key = prefix + '.' + fieldNameList.get(0);
                } else {
                    key = fieldNameList.get(0);
                }
            }
            prefixString = key;
        } else {
            fieldName = fieldNameList.get(size - 1);
            fieldNameList.remove(size - 1);
            if (startsWithMainClassName(fieldNameList.get(0))) {
                prefixString = String.join(".", fieldNameList);
            } else {
                prefixString = prefix + '.' + String.join(".", fieldNameList);
            }

        }
        FieldColumnRelationMapper mapper = getLocalMapper(prefixString);
        if (mapper == null) {
            throw new RuntimeException(prefixString + "不存在");
        }
        if(type == 2){
            mapper = mapper.getBaseTemplateMapper() == null?mapper:mapper.getBaseTemplateMapper();
        }
        FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByField(fieldName);
        return new ColumnBaseEntity(fieldColumnRelation, mapper.getTableName(), prefixString, getAbbreviation(prefixString));
    }


    /**
     * 转换sql语句中表名为简写
     * @param searchSql
     * @return
     */
    public String translateTableNickName(String prefix, String searchSql) {
        char[] cs = searchSql.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder replaceSb = new StringBuilder();
        char c;
        boolean isReplace = false;
        String replaceStr = null;
        for (char value : cs) {
            c = value;
            if (isReplace) {
                if (c == '}') {
                    replaceStr = getParticular(replaceSb.toString());
                    if (replaceStr == null) {
                        if (replaceSb.toString().startsWith(prefix)) {
                            stringBuilder.append(getAbbreviation(replaceSb.toString()));
                        } else {
                            stringBuilder.append(getAbbreviation(prefix + "." + replaceSb.toString()));
                        }
                    } else {
                        stringBuilder.append(replaceSb.toString());
                    }
                    isReplace = false;
                } else {
                    replaceSb.append(c);
                }
            } else {
                if (c == '{') {
                    replaceSb = new StringBuilder();
                    isReplace = true;
                } else {
                    stringBuilder.append(c);
                }
            }
        }
        return stringBuilder.toString();

    }
}
