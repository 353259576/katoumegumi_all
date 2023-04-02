package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.entity.BaseTableColumn;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 翻译表名和列名
 *
 * @author ws
 */
public class TranslateNameUtils {

    private final TranslateNameUtils parent;

    private final List<String> mainClassNameList;

    /**
     * 简写
     */
    private final Map<String, String> abbreviationMap;

    /**
     * 原始名称
     */
    private final Map<String, String> particularMap;

    /**
     * 缩写防重复
     */
    private final AtomicInteger abbreviationNum;

    /**
     * 本地对象与表的对应关系
     */
    private final Map<String, FieldColumnRelationMapper> localMapperMap;

    //private final Map<String,ColumnBaseEntity> columnBaseEntityCacheMap = new HashMap<>();

    public TranslateNameUtils() {
        this.parent = null;
        this.mainClassNameList = new ArrayList<>();
        this.abbreviationMap = new HashMap<>();
        this.particularMap = new HashMap<>();
        this.abbreviationNum = new AtomicInteger();
        this.localMapperMap = new HashMap<>();
    }

    public TranslateNameUtils(TranslateNameUtils translateNameUtils) {
        this.parent = translateNameUtils;
        this.abbreviationNum = translateNameUtils.abbreviationNum;

        this.localMapperMap = new HashMap<>();
        this.mainClassNameList = new ArrayList<>();
        this.abbreviationMap = new HashMap<>();
        this.particularMap = new HashMap<>();
    }


    /**
     * 创建简称
     *
     * @param keyword
     * @return
     */
    public String createAbbreviation(String keyword) {
        if (keyword.length() < 2) {
            return keyword + SqlCommonConstants.KEY_COMMON_DELIMITER + abbreviationNum.getAndAdd(1);
        } else {
            return keyword.substring(0, 1) + SqlCommonConstants.KEY_COMMON_DELIMITER + abbreviationNum.getAndAdd(1);
        }
    }

    /**
     * 获取详细名称
     *
     * @param value 简称
     * @return
     */
    public String getParticular(String value) {
        TranslateNameUtils parent = this.parent;
        String returnValue = particularMap.get(value);
        if (returnValue == null && parent != null) {
            returnValue = parent.particularMap.get(value);
            parent = parent.parent;
        }
        return returnValue;
        //return particularMap.get(value);
    }


    /**
     * 获取简称
     *
     * @param keyword
     * @return
     */
    public String getAbbreviation(String keyword) {
        TranslateNameUtils parent = this.parent;
        String value = abbreviationMap.get(keyword);
        while (value == null && parent != null) {
            value = parent.abbreviationMap.get(keyword);
            parent = parent.parent;
        }
        if (value == null) {
            //value = particularMap.get(keyword);
            value = getParticular(keyword);
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
     * 获取当前的简称
     *
     * @param keyword
     * @return
     */
    public String getCurrentAbbreviation(String keyword) {
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
     * 获取当前简称并不自动设置简称
     *
     * @param keyword
     * @return
     */
    public String getCurrentAbbreviationAndNotAutoSet(String keyword) {
        return abbreviationMap.get(keyword);
    }

    /**
     * 设置简称
     *
     * @param keyword 实际名称
     * @param value   简称
     */
    public void setAbbreviation(String keyword, String value) {
        abbreviationMap.put(keyword, value);
        particularMap.put(value, keyword);
    }

    /**
     * 设置简称并返回
     *
     * @param keyword
     * @return
     */
    public String setAbbreviation(String keyword) {
        String value = createAbbreviation(keyword);
        abbreviationMap.put(keyword, value);
        particularMap.put(value, keyword);
        return value;
    }

    /**
     * 获取本地缓存的mapper
     *
     * @param locationName
     * @return
     */
    public FieldColumnRelationMapper getLocalMapper(String locationName) {
        TranslateNameUtils parent = this.parent;
        FieldColumnRelationMapper mapper = localMapperMap.get(locationName);
        while (mapper == null && parent != null) {
            mapper = parent.localMapperMap.get(locationName);
            parent = parent.parent;
        }
        return mapper;
        //return localMapperMap.get(locationName);
    }

    /**
     * 添加本地缓存的mapper
     *
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
     *
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
        TranslateNameUtils parent = this.parent;
        while (parent != null) {
            for (String prefix : parent.mainClassNameList) {
                if (tableName.startsWith(prefix)) {
                    return tableName.substring(prefix.length());
                }
            }
            parent = parent.parent;
        }
        return tableName;
    }

    /**
     * 根据查询条件生成列基本信息
     *
     * @param originalFieldName
     * @param rootPath
     * @param type              1 返回的列名 2 查询的列名
     * @return
     */
    public BaseTableColumn getColumnBaseEntity(String originalFieldName, final String rootPath, final int type) {
        String path;
        String fieldName;
        originalFieldName = translateTableNickName(rootPath, originalFieldName);
        List<String> fieldNameList = WsStringUtils.split(originalFieldName, SqlCommonConstants.PATH_COMMON_DELIMITER);
        int size = fieldNameList.size();
        if (size == 1) {
            fieldName = fieldNameList.get(0);
            path = rootPath;
        } else if (size == 2) {
            fieldName = fieldNameList.get(1);
            String key = getParticular(fieldNameList.get(0));
            if (key == null) {
                if (!startsWithMainClassName(fieldNameList.get(0))) {
                    key = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + fieldNameList.get(0);
                } else {
                    key = fieldNameList.get(0);
                }
            } else {
                return createColumnBaseEntity(fieldName, key, fieldNameList.get(0), type);
            }
            path = key;

        } else {
            fieldName = fieldNameList.get(size - 1);
            fieldNameList.remove(size - 1);
            if (startsWithMainClassName(fieldNameList.get(0))) {
                path = String.join(".", fieldNameList);
            } else {
                path = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + String.join(".", fieldNameList);
            }
        }
        return createColumnBaseEntity(fieldName, path, type);
    }


    public BaseTableColumn createColumnBaseEntity(final FieldColumnRelation fieldColumnRelation, final FieldColumnRelationMapper mapper, final String path) {
        return new BaseTableColumn(fieldColumnRelation, mapper.getTableName(), path, getAbbreviation(path));
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final int type) {
        return createColumnBaseEntity(fieldName, path, getAbbreviation(path), type);
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final String abbreviation, final int type) {
        FieldColumnRelationMapper mapper = getLocalMapper(path);
        if (mapper == null) {
            throw new NullPointerException("can not find mapper by path:" + path);
        }
        if (type == 2) {
            mapper = mapper.getBaseTemplateMapper() == null ? mapper : mapper.getBaseTemplateMapper();
        }
        FieldColumnRelation fieldColumnRelation = mapper.getFieldColumnRelationByFieldName(fieldName);
        return new BaseTableColumn(fieldColumnRelation, mapper.getTableName(), path, abbreviation);
    }


    /**
     * 转换sql语句中表名为简写
     *
     * @param searchSql
     * @return
     */
    public String translateTableNickName(String prefix, String searchSql) {
        char[] cs = searchSql.toCharArray();
        List<int[]> locationList = new ArrayList<>();

        boolean isStart = false;
        int startIndex = 0;
        for (int i = 0; i < cs.length; ++i) {
            if (cs[i] == '{') {
                isStart = true;
                startIndex = i;
            } else if (cs[i] == '}' && isStart) {
                locationList.add(new int[]{startIndex, i});
            }
        }
        if (locationList.size() == 0) {
            return searchSql;
        } else {
            startIndex = 0;
            StringBuilder translateStringBuilder = new StringBuilder();
            String replaceStr = null;

            for (int[] ints : locationList) {
                if (ints[0] > startIndex) {
                    translateStringBuilder.append(Arrays.copyOfRange(cs, startIndex, ints[0]));
                }
                String needReplaceStr = new String(Arrays.copyOfRange(cs, ints[0] + 1, ints[1]));
                replaceStr = getParticular(needReplaceStr);
                if (replaceStr == null) {
                    needReplaceStr = getAddPathTableNickName(prefix, needReplaceStr);
                    translateStringBuilder.append(getAbbreviation(needReplaceStr));
                } else {
                    translateStringBuilder.append(needReplaceStr);
                }
                startIndex = ints[1] + 1;
            }
            if (startIndex < cs.length) {
                translateStringBuilder.append(Arrays.copyOfRange(cs, startIndex, cs.length));
            }
            return translateStringBuilder.toString();
        }
    }


    /**
     * 转换sql语句里{}里的字段
     *
     * @param searchSql
     * @return
     */
    public String translateToTableName(String searchSql) {
        String[] strs = WsStringUtils.splitArray(searchSql, '.');
        String ns;
        String s;
        for (int i = 0; i < strs.length; i++) {
            s = strs[i];
            if (s.startsWith("{")) {
                s = s.substring(1, s.length() - 1);
                strs[i] = s;
            }
            ns = getParticular(s);
            if (ns != null) {
                strs[i] = ns;
            }
        }
        return WsStringUtils.jointListString(strs, ".");
    }


    /**
     * 获取完成表名称
     *
     * @param rootPath
     * @param originalTableNickName
     * @return
     */
    public String getCompleteTableNickName(final String rootPath, String originalTableNickName) {
        String tableNickName = getCurrentAbbreviationAndNotAutoSet(originalTableNickName);
        if (tableNickName == null) {
            return getAddPathTableNickName(rootPath, originalTableNickName);
        }
        return originalTableNickName;
    }

    public String getAddPathTableNickName(final String rootPath, String tableNickName) {
        if (tableNickName.length() < rootPath.length()) {
            tableNickName = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableNickName;
        } else {
            if (tableNickName.startsWith(rootPath)) {
                if (!(tableNickName.length() == rootPath.length() || tableNickName.charAt(rootPath.length()) == SqlCommonConstants.PATH_COMMON_DELIMITER)) {
                    tableNickName = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableNickName;
                }
            } else {
                tableNickName = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + tableNickName;
            }
        }
        return tableNickName;
    }
}
