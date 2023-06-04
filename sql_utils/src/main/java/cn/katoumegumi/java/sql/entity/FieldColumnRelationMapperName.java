package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

import java.util.Map;

/**
 * 字段列名映射关系名称
 */
public class FieldColumnRelationMapperName {

    private final int index;

    private final FieldColumnRelationMapper mapper;

    /**
     * 简称
     */
    private final String abbreviation;

    /**
     * 完整名称
     */
    private final String completeName;

    /**
     * 完整名称拆分列表
     */
    private final String[] completeNameSplitNameList;

    /**
     * 完整名称拆分标记名称列表
     */
    private final int[] completeNameSplitSignNameList;

    public FieldColumnRelationMapperName(int index, String abbreviation, String completeName,FieldColumnRelationMapper mapper) {
        this.index = index;
        this.abbreviation = abbreviation;
        this.completeName = completeName;
        this.mapper = mapper;
        this.completeNameSplitNameList = WsStringUtils.splitArray(this.completeName, '.');
        this.completeNameSplitSignNameList = new int[completeNameSplitNameList.length];
    }

    public int setCompleteNameSplitSignNameList(Map<String, Integer> existNameMap, int signOffset) {
        Integer tempSign;
        String tempSplitName;
        int length = completeNameSplitNameList.length;
        for (int i = 0; i < length; i++) {
            tempSplitName = this.completeNameSplitNameList[i];
            tempSign = existNameMap.get(tempSplitName);
            if (tempSign == null) {
                tempSign = signOffset++;
                existNameMap.put(tempSplitName, tempSign);
            }
            this.completeNameSplitSignNameList[i] = tempSign;
        }
        return signOffset;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String[] getCompleteNameSplitNameList() {
        return completeNameSplitNameList;
    }

    public int[] getCompleteNameSplitSignNameList() {
        return completeNameSplitSignNameList;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getIndex() {
        return index;
    }

    public FieldColumnRelationMapper getMapper() {
        return mapper;
    }
}
