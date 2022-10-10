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
    EQ((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    EQP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 模糊查询
     */
    LIKE(SQLModelUtils::likeConditionHandle),
    /**
     * 大于
     */
    GT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.GT, translateNameUtils, mySearch, prefix, baseWhereValueList);
    }),
    GTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.GT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 小于
     */
    LT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.LT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    LTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.LT, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 大于等于
     */
    GTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.GTE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    GTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.GTE, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 小于等于
     */
    LTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.LTE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    LTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.LTE, translateNameUtils, mySearch, prefix, baseWhereValueList);
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
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.NEQ, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    NEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonNoValueConditionHandle(SqlCommonConstants.NEQ, translateNameUtils, mySearch, prefix, baseWhereValueList);
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
        return SQLModelUtils.commonConditionHandle(SqlCommonConstants.EQ, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 加
     */
    ADD(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommonConstants.ADD, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 减
     */
    SUBTRACT(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommonConstants.SUBTRACT, translateNameUtils, mySearch, prefix, baseWhereValueList);

    })),
    /**
     * 乘
     */
    MULTIPLY(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommonConstants.MULTIPLY, translateNameUtils, mySearch, prefix, baseWhereValueList);
    })),
    /**
     * 除
     */
    DIVIDE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        return SQLModelUtils.commonUpdateConditionHandle(SqlCommonConstants.DIVIDE, translateNameUtils, mySearch, prefix, baseWhereValueList);

    }),
    /**
     * 等式
     */
    EQUATION(null);


    //private final String value;

    private final ColumnConditionHandle handle;

    SqlOperator(ColumnConditionHandle handle) {
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
        String handle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<SqlParameter> baseWhereValueList);
    }

}
