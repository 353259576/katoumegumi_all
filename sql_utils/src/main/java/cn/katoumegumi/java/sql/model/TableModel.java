package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.FieldColumnRelationMapper;

import java.util.List;

/**
 * 表
 */
public class TableModel {

    /**
     * 表信息
     */
    private FieldColumnRelationMapper table;

    /**
     * 别名（一次查询中唯一）
     */
    private String alias;

    private TableModel parentTable;

    private ConditionModel columnCondition;

    private List<ConditionModel> extConditionList;



}
