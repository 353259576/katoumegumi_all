package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * sql函数接口
 */
public class SqlFunction {

    private static final BiFunction<String, SFunction<?, ?>, String> columnNameBiFunction = ((s, sFunction) -> {
        if (WsStringUtils.isBlank(s)) {
            return WsFieldUtils.getFieldName(sFunction);
        } else {
            return s + '.' + WsFieldUtils.getFieldName(sFunction);
        }
    });

    private final String sqlFunctionStr;

    private final List<String> functionSplitList;

    private final List<String> sqlFunctionValue;

    private SqlFunction(String sqlFunctionStr, List<String> sqlFunctionValue) {
        this.sqlFunctionStr = sqlFunctionStr;
        this.sqlFunctionValue = sqlFunctionValue;
        this.functionSplitList = WsStringUtils.split(sqlFunctionStr, '?');
        int valueSize = sqlFunctionValue == null ? 0 : sqlFunctionValue.size();
        int splitSize = functionSplitList.size();
        if (sqlFunctionStr.endsWith(SqlCommonConstants.PLACEHOLDER)) {
            splitSize++;
        }
        if (valueSize != splitSize - 1) {
            throw new RuntimeException("需要：" + splitSize + " 实际：" + valueSize);
        }
    }

    public static SqlFunction create(String function, String... columnNames) {
        return new SqlFunction(function, Arrays.asList(columnNames));
    }

    public static SqlFunction create(String function, List<String> columnNames) {
        return new SqlFunction(function, columnNames);
    }

    public static <T> SqlFunction create(String function, String tableName, SFunction<T, ?> sFunction) {
        return new SqlFunction(function, Collections.singletonList(columnNameBiFunction.apply(tableName, sFunction)));
    }

    public static <T> SqlFunction create(String function, SFunction<T, ?> sFunction) {
        return new SqlFunction(function, Collections.singletonList(columnNameBiFunction.apply(null, sFunction)));
    }

    public String getSqlFunctionStr() {
        return sqlFunctionStr;
    }

    public List<String> getSqlFunctionValue() {
        return sqlFunctionValue;
    }

    public String getFunctionValue(String columnName) {
        if (functionSplitList.size() == 1) {
            return functionSplitList.get(0) + columnName;
        } else {
            return functionSplitList.get(0) + columnName + functionSplitList.get(1);
        }
    }

}
