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
    private final Map<String, String> aliasMap;

    /**
     * 简写->原始名称
     */
    private final Map<String, String> originalNameMap;

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
        this.aliasMap = new HashMap<>();
        this.originalNameMap = new HashMap<>();
        this.abbreviationNum = new AtomicInteger();
        this.pathMapperMap = new HashMap<>();
    }

    public TranslateNameUtils(TranslateNameUtils translateNameUtils) {
        this.parent = translateNameUtils;
        this.abbreviationNum = translateNameUtils.abbreviationNum;
        this.pathMapperMap = new HashMap<>();
        this.rootPathPrefixList = new ArrayList<>();
        this.aliasMap = new HashMap<>();
        this.originalNameMap = new HashMap<>();
    }


    /**
     * 创建简称
     * @param keyword
     * @return
     */
    public String createAlias(String keyword) {
        String prefixStr = keyword.length() < 2 ? keyword + SqlCommonConstants.KEY_COMMON_DELIMITER : keyword.substring(0, 1) + SqlCommonConstants.KEY_COMMON_DELIMITER;
        String abbreviation = prefixStr + abbreviationNum.getAndAdd(1);
        while (aliasMap.containsKey(abbreviation)) {
            abbreviation = prefixStr + abbreviationNum.getAndAdd(1);
        }
        return abbreviation;
    }

    /**
     * 获取详细名称
     *
     * @param value 简称
     * @return
     */
    public String getOriginalName(String value) {
        TranslateNameUtils parent = this.parent;
        String returnValue = originalNameMap.get(value);
        while (returnValue == null && parent != null) {
            returnValue = parent.originalNameMap.get(value);
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
    public String getAlias(String keyword) {
        TranslateNameUtils parent = this.parent;
        String value = aliasMap.get(keyword);
        while (value == null && parent != null) {
            value = parent.aliasMap.get(keyword);
            parent = parent.parent;
        }
        if (value == null) {
            value = getOriginalName(keyword);
            if (value == null) {
                value = setAlias(keyword);
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
    public String getCurrentAlias(String keyword) {
        String value = aliasMap.get(keyword);
        if (value == null) {
            value = originalNameMap.get(keyword);
            if (value == null) {
                value = setAlias(keyword);
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
    public String getCurrentAliasAndNotAutoSet(String keyword) {
        return aliasMap.get(keyword);
    }

    /**
     * 设置简称
     *
     * @param originalName 实际名称
     * @param alias   简称
     */
    public void setAlias(String originalName, String alias) {
        if (aliasMap.put(originalName, alias) != null){
            throw new IllegalArgumentException(originalName + " already exists");
        }
        if (originalNameMap.put(alias, originalName) != null){
            throw new IllegalArgumentException("alias:" + alias + " already exists");
        }
    }

    /**
     * 设置简称并返回
     *
     * @param keyword
     * @return
     */
    public String setAlias(String keyword) {
        String value = createAlias(keyword);
        setAlias(keyword, value);
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
            String completePath = getOriginalName(pathName);
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
    }


    public BaseTableColumn createColumnBaseEntity(final PropertyBaseColumnRelation propertyBaseColumnRelation, final PropertyColumnRelationMapper mapper, final String path) {
        return new BaseTableColumn(propertyBaseColumnRelation, mapper.getTableName(), path, getAlias(path));
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final int type) {
        return createColumnBaseEntity(fieldName, path, getAlias(path), type);
    }

    public BaseTableColumn createColumnBaseEntity(final String fieldName, final String path, final String alias, final int type) {
        PropertyColumnRelationMapper mapper = getLocalMapper(alias);
        //PropertyColumnRelationMapper mapper = getLocalMapper(path);
        if (mapper == null) {
            throw new NullPointerException("can not find mapper by path:" + path);
        }
        if (type == 2) {
            mapper = mapper.getBaseTemplateMapper() == null ? mapper : mapper.getBaseTemplateMapper();
        }
        PropertyBaseColumnRelation propertyBaseColumnRelation = mapper.getFieldColumnRelationByFieldName(fieldName);
        return new BaseTableColumn(propertyBaseColumnRelation, mapper.getTableName(), path, alias);
    }


    /**
     * 转换sql语句中表名为简写
     *
     * @param searchSql
     * @return
     */
    public String translateTableNickName(String prefix, String searchSql) {
        return WsStringUtils.format(searchSql,needReplaceStr->{
            String replaceStr = getOriginalName(needReplaceStr);
            if (replaceStr == null) {
                //needReplaceStr = getAddPathTableNickName(prefix, needReplaceStr);
                needReplaceStr = getCompleteEntityPath(prefix, needReplaceStr);
                return getAlias(needReplaceStr);
            } else {
                return needReplaceStr;
            }
        });
    }


    /**
     * 将{}包裹的文本转换为实体名称
     * @param searchSql
     * @return
     */
    public String translateToEntityName(String searchSql) {
        searchSql = WsStringUtils.format(searchSql,s->{
            String ns = getOriginalName(s);
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
            ns = getOriginalName(s);
            if (ns != null) {
                strs[i] = ns;
            }
        }
        return WsStringUtils.jointListString(strs, SqlCommonConstants.PATH_COMMON_DELIMITER_STR);
    }


    /**
     * 获取完成表名称
     * @param rootPath
     * @param relativePath
     * @return
     */
    public String getCompleteEntityPath(final String rootPath, String relativePath) {
        String tableNickName = getCurrentAliasAndNotAutoSet(relativePath);
        if (tableNickName == null) {
            //return getAddPathTableNickName(rootPath, relativePath);
            if (relativePath.length() < rootPath.length()) {
                relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
            } else {
                if (relativePath.startsWith(rootPath)) {
                    if (!(relativePath.length() == rootPath.length() || relativePath.charAt(rootPath.length()) == SqlCommonConstants.PATH_COMMON_DELIMITER)) {
                        relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
                    }
                } else {
                    relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
                }
            }
            return relativePath;
        }
        return relativePath;
    }

/*    public String getAddPathTableNickName(final String rootPath, String relativePath) {
        if (relativePath.length() < rootPath.length()) {
            relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
        } else {
            if (relativePath.startsWith(rootPath)) {
                if (!(relativePath.length() == rootPath.length() || relativePath.charAt(rootPath.length()) == SqlCommonConstants.PATH_COMMON_DELIMITER)) {
                    relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
                }
            } else {
                relativePath = rootPath + SqlCommonConstants.PATH_COMMON_DELIMITER + relativePath;
            }
        }
        return relativePath;
    }*/
}
