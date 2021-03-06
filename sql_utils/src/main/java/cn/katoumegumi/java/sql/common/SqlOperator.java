package cn.katoumegumi.java.sql.common;

import cn.katoumegumi.java.sql.MySearch;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.TranslateNameUtils;

import java.util.List;

/**
 * @author 10480
 */

public enum SqlOperator {


    /**
     * 等于
     */
    EQ((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    EQP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 模糊查询
     */
    LIKE(SQLModelUtils::likeConditionHandle),
    /**
     * 大于
     */
    GT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.GT, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    GTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.GT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 小于
     */
    LT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.LT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    LTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.LT, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 大于等于
     */
    GTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.GTE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    GTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.GTE, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 小于等于
     */
    LTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.LTE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    LTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.LTE, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * in
     */
    IN((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.inConditionHandle(true, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    /**
     * not in
     */
    NIN((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.inConditionHandle(false, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    /**
     * not null
     */
    NOTNULL((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.nullConditionHandle(false, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    /**
     * is null
     */
    NULL((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.nullConditionHandle(true, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    /**
     * 不等于
     */
    NE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.NEQ, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    NEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommon.NEQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 嵌入sql 只有hibernate支持
     */
    SQL((SQLModelUtils::sqlConditionHandle)),
    EXISTS(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.existsConditionHandle(true, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    NOT_EXISTS(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.existsConditionHandle(false, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    BETWEEN(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.betweenConditionHandle(true, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    NOT_BETWEEN(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.betweenConditionHandle(false, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 排序
     */
    SORT((SQLModelUtils::sortConditionHandle)),
    /**
     * and
     */
    AND(null),
    /**
     * or
     */
    OR(null),
    /**
     * 修改
     */
    SET(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommon.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 加
     */
    ADD(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommon.ADD, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 减
     */
    SUBTRACT(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommon.SUBTRACT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 乘
     */
    MULTIPLY(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommon.MULTIPLY, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 除
     */
    DIVIDE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommon.DIVIDE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    /**
     * 等式
     */
    EQUATION(null);


    //private final String value;

    private final ColumnConditionHandle handle;

    private SqlOperator(ColumnConditionHandle handle) {
        //this.value = value;
        this.handle = handle;
    }


    public ColumnConditionHandle getHandle() {
        return handle;
    }

    public interface ColumnConditionHandle {

        /**
         * 处理方法
         *
         * @param translateNameUtils
         * @param mySearch
         * @param prefix
         * @param baseWhereValueList
         * @return
         */
        public String handle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList);
    }

}
