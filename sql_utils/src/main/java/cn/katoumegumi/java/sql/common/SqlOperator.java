package cn.katoumegumi.java.sql.common;

import cn.katoumegumi.java.sql.MySearch;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.TranslateNameUtils;
import cn.katoumegumi.java.sql.entity.SqlParameter;

import java.util.List;

/**
 * @author 10480
 */

public enum SqlOperator {


    /**
     * 等于
     */
    EQ,
    EQP,
    /**
     * 模糊查询
     */
    LIKE,
    /**
     * 大于
     */
    GT,
    GTP,
    /**
     * 小于
     */
    LT,
    LTP,
    /**
     * 大于等于
     */
    GTE,
    GTEP,
    /**
     * 小于等于
     */
    LTE,
    LTEP,
    /**
     * in
     */
    IN,
    /**
     * not in
     */
    NIN,
    /**
     * not null
     */
    NOTNULL,
    /**
     * is null
     */
    NULL,
    /**
     * 不等于
     */
    NE,
    NEP,
    /**
     * 嵌入sql 只有hibernate支持
     */
    SQL,
    EXISTS,
    NOT_EXISTS,
    BETWEEN,
    NOT_BETWEEN,
    /**
     * 排序
     */
    SORT,
    /**
     * and
     */
    AND,
    /**
     * or
     */
    OR,
    /**
     * 修改
     */
    SET,
    /**
     * 加
     */
    ADD,
    /**
     * 减
     */
    SUBTRACT,
    /**
     * 乘
     */
    MULTIPLY,
    /**
     * 除
     */
    DIVIDE,
    /**
     * 等式
     */
    EQUATION;
}
