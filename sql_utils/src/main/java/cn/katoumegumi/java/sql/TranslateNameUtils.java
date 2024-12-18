package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;
import cn.katoumegumi.java.sql.mapper.model.PropertyColumnRelationMapper;
import cn.katoumegumi.java.sql.model.component.BaseTableColumn;
import cn.katoumegumi.java.sql.model.query.QueryColumn;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 翻译表名和列名
 *
 * @author ws
 */
public class TranslateNameUtils {

    private final TranslateNameUtils parent;

    private final List<String> rootPathPrefixList;

    /**
     * 原始名称->简写
     */
    private final Map<String, String> abbreviationMap;

    /**
     * 简写->原始名称
     */
    private final Map<String, String> particularMap;

    /**
     * 防止重复数字
     */
    private final AtomicInteger abbreviationNum;

    /**
     * 本地对象与表的对应关系
     */
    private final Map<String, PropertyColumnRelationMapper> pathMapperMap;

    //private final Map<String,ColumnBaseEntity> columnBaseEntityCacheMap = new HashMap<>();

    public TranslateNameUtils() {
        this.parent = null;
        this.rootPathPrefixList = new ArrayList<>();
        this.abbreviationMap = new HashMap<>();
        this.particularMap = new HashMap<>();
        this.abbreviationNum = new AtomicInteger();
        this.pathMapperMap = new HashMap<>();
    }

    public TranslateNameUtils(TranslateNameUtils translateNameUtils) {
        this.parent = translateNameUtils;
        this.abbreviationNum = translateNameUtils.abbreviationNum;
        this.pathMapperMap = new HashMap<>();
        this.rootPathPrefixList = new ArrayList<>();
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
        while (returnValue == null && parent != null) {
            returnValue = parent.particularMap.get(value);
            parent = parent.parent;
        }
        return returnValue;
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
            value = getParticular(keyword);
            if (value == null) {
                value = setAbbreviation(keyword);
            }
        }
        return value;
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
                value = setAbbreviation(keyword);
            }
        }
        return value;
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
        if (abbreviationMap.put(keyword, value) != null){
            throw new IllegalArgumentException(keyword + " already exists");
        }
        if (particularMap.put(value, keyword) != null){
            throw new IllegalArgumentException("abbreviation:" + value + " already exists");
        }
    }

    /**
     * 设置简称并返回
     *
     * @param keyword
     * @return
     */
    public String setAbbreviation(String keyword) {
        String value = createAbbreviation(keyword);
        setAbbreviation(keyword, value);
        return value;
    }

    /**
     * 获取本地缓存的mapper
     *
     * @param locationName
     * @return
     */
    public PropertyColumnRelationMapper getLocalMapper(String locationName) {
        TranslateNameUtils parent = this.parent;
        PropertyColumnRelationMapper mapper = pathMapperMap.get(locationName);
        while (mapper == null && parent != null) {
            mapper = parent.pathMapperMap.get(locationName);
            parent = parent.parent;
        }
        return mapper;
        //return localMapperMap.get(locationName);
    }

    /**
     * 添加本地缓存的mapper
     *
     * @param path
     * @param mapper
     */
    public void addLocalMapper(String path, PropertyColumnRelationMapper mapper) {
        pathMapperMap.put(path, mapper);
    }

    public int locationMapperSize() {
        return pathMapperMap.size();
    }

    /**
     * 添加根路径前缀
     * @param rootPath
     */
    public void addRootPathPrefix(String rootPath) {
        this.rootPathPrefixList.add(rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER);
    }

    /**
     * 判断当前路径是否是完整路径
     * 判断路径开头是不是根路径
     * @param path
     * @return
     */
    public boolean isCompletePath(String path) {
        if (WsStringUtils.isEmpty(path)) {
            return false;
        }
        path = path + SqlCommonConstants.PATH_COMMON_DELIMITER;
        for (String rootPathPrefix : this.rootPathPrefixList) {
            if (path.startsWith(rootPathPrefix)) {
                return true;
            }
        }
        if (this.parent == null) {
            return false;
        }
        return this.parent.isCompletePath(path);
    }

    /**
     * 获取相对路径
     * 如果路径是完整路径，去除根路径
     * @return
     */
    public String getRelativePath(final String path) {
        if (WsStringUtils.isEmpty(path)) {
            return null;
        }
        for (String rootPathPrefix : this.rootPathPrefixList) {
            if (path.startsWith(rootPathPrefix)) {
                return path.substring(rootPathPrefix.length());
            }
        }
        if (this.parent == null) {
            return path;
        }
        return this.parent.getRelativePath(path);
        /*TranslateNameUtils parent = this.parent;
        while (parent != null) {
            for (String prefix : parent.mainClassNameList) {
                if (path.startsWith(prefix)) {
                    return path.substring(prefix.length());
                }
            }
            parent = parent.parent;
        }
        return path;*/
    }

    /**
     * 根据查询条件生成列基本信息
     *
     * @param column
     * @param rootPath
     * @param type              1 返回的列名 2 查询的列名
     * @return
     */
    public BaseTableColumn getColumnBaseEntity(QueryColumn column, final String rootPath, final int type) {

        if (WsStringUtils.isBlank(column.getPath())){
            return createColumnBaseEntity(column.getName(), rootPath, type);
        }

        String originalPathName = translateTableNickName(rootPath,column.getPath());
        List<String> pathNameList = WsStringUtils.split(originalPathName, SqlCommonConstants.PATH_COMMON_DELIMITER);
        if (pathNameList.size() == 1){
            String pathName = pathNameList.get(0);
            String completePath = getParticular(pathName);
            if (completePath == null) {
                if (!isCompletePath(pathName)) {
                    completePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + pathName;
                } else {
                    completePath = pathName;
                }
                return createColumnBaseEntity(column.getName(),completePath,type);
            } else {
                return createColumnBaseEntity(column.getName(), completePath, pathName, type);
            }
        }else {
            String completePath;
            if (isCompletePath(pathNameList.get(0))) {
                completePath = String.join(".", pathNameList);
            } else {
                completePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + String.join(".", pathNameList);
            }
            return createColumnBaseEntity(column.getName(),completePath,type);
        }
        
        
//        String path;
//        String fieldName;
//        originalFieldName = translateTableNickName(rootPath, originalFieldName);
//        List<String> fieldNameList = WsStringUtils.split(originalFieldName, SqlCommonConstants.PATH_COMMON_DELIMITER);
//        int size = fieldNameList.size();
//        if (size == 1) {
//            fieldName = fieldNameList.get(0);
//            path = rootPath;
//        } else if (size == 2) {
//            fieldName = fieldNameList.get(1);
//            String key = getParticular(fieldNameList.get(0));
//            if (key == null) {
//                if (!startsWithMainClassName(fieldNameList.get(0))) {
//                    key = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + fieldNameList.get(0);
//                } else {
//                    key = fieldNameList.get(0);
//                }
//            } else {
//                return createColumnBaseEntity(fieldName, key, fieldNameList.get(0), type);
//            }
//            path = key;
//
//        } else {
//            fieldName = fieldNameList.get(size - 1);
//            fieldNameList.remove(size - 1);
//            if (startsWithMainClassName(fieldNameList.get(0))) {
//                path = String.join(".", fieldNameList);
//            } else {
//                path = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + String.join(".", fieldNameList);
//            }
//        }
//        return createColumnBaseEntity(fieldName, path, type);
    }


    public BaseTableColumn createColumnBaseEntity(final PropertyBaseColumnRelation propertyBaseColumnRelation, final PropertyColumnRelationMapper mapper, final String path) {
        return new BaseTableColumn(propertyBaseColumnRelation, mapper.getTableName(), path, getAbbreviation(path));
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final int type) {
        return createColumnBaseEntity(fieldName, path, getAbbreviation(path), type);
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final String abbreviation, final int type) {
        PropertyColumnRelationMapper mapper = getLocalMapper(path);
        if (mapper == null) {
            throw new NullPointerException("can not find mapper by path:" + path);
        }
        if (type == 2) {
            mapper = mapper.getBaseTemplateMapper() == null ? mapper : mapper.getBaseTemplateMapper();
        }
        PropertyBaseColumnRelation propertyBaseColumnRelation = mapper.getFieldColumnRelationByFieldName(fieldName);
        return new BaseTableColumn(propertyBaseColumnRelation, mapper.getTableName(), path, abbreviation);
    }


    /**
     * 转换sql语句中表名为简写
     *
     * @param searchSql
     * @return
     */
    public String translateTableNickName(String prefix, String searchSql) {
        return WsStringUtils.format(searchSql,needReplaceStr->{
            String replaceStr = getParticular(needReplaceStr);
            if (replaceStr == null) {
                needReplaceStr = getAddPathTableNickName(prefix, needReplaceStr);
                return getAbbreviation(needReplaceStr);
            } else {
                return needReplaceStr;
            }
        });

/*        char[] cs = searchSql.toCharArray();
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
        if (locationList.isEmpty()) {
            return searchSql;
        } else {
            startIndex = 0;
            StringBuilder translateStringBuilder = new StringBuilder();
            String replaceStr;

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
        }*/
    }


    /**
     * 转换sql语句里{}里的字段
     *
     * @param searchSql
     * @return
     */
    public String translateToTableName(String searchSql) {
        searchSql = WsStringUtils.format(searchSql,s->{
            String ns = getParticular(s);
            if (ns == null) {
                return s;
            }
            return ns;
        });
        String[] strs = WsStringUtils.splitArray(searchSql, SqlCommonConstants.PATH_COMMON_DELIMITER);
        String ns;
        String s;
        for (int i = 0; i < strs.length; i++) {
            s = strs[i];
            ns = getParticular(s);
            if (ns != null) {
                strs[i] = ns;
            }
        }
        return WsStringUtils.jointListString(strs, SqlCommonConstants.PATH_COMMON_DELIMITER_STR);
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
