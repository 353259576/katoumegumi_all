package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;

import java.util.List;

/**
 * select 语句
 */
public class SelectModel {

    /**
     * 需要查询的列名
     */
    private List<ColumnBaseEntity> selectColumnList;

    /**
     * 查询主表
     */
    private TableModel mainTable;

    /**
     * 关联表
     */
    private List<TableModel> joinTableList;



}
