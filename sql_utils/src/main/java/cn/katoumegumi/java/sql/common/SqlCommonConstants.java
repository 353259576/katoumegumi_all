package cn.katoumegumi.java.sql.common;

import cn.katoumegumi.java.sql.model.component.SqlStringModel;

public interface SqlCommonConstants {

    String PLACEHOLDER = "?";

    String COMMA = ",";

    char SPACE = ' ';

    String SELECT = "select ";

    String UPDATE = "update ";

    String DELETE = "delete ";

    String INSERT_INTO = "insert into ";

    String VALUE = " value ";

    String SET = " set ";

    String ON = " on ";

    String WHERE = " where ";

    String FROM = " from ";

    String ORDER_BY = " order by ";

    String LIKE = " like ";

    String DISTINCT = " distinct ";

    String EQ = " = ";

    String NEQ = " != ";

    String GT = " > ";

    String GTE = " >= ";

    String LT = " < ";

    String LTE = " <= ";

    String NULL = " is null";

    String NOT_NULL = " is not null";

    String IN = " in ";

    String NOT_IN = " not in ";

    String EXISTS = " exists ";

    String NOT_EXISTS = " not exists ";

    String BETWEEN = " between ";

    String NOT_BETWEEN = " not between ";

    String SQL_AND = " and ";

    String SQL_OR = " or ";

    String AND = (" & ");

    String OR = (" | ");

    String XOR = (" ^ ");

    String NOT = " ~ ";

    String ADD = " + ";

    String SUBTRACT = " - ";

    String MULTIPLY = " * ";

    String DIVIDE = " / ";

    char LEFT_BRACKETS = '(';

    char RIGHT_BRACKETS = ')';

    char KEY_COMMON_DELIMITER = '_';

    char PATH_COMMON_DELIMITER = '.';

    String PATH_COMMON_DELIMITER_STR = ".";

    char SQL_COMMON_DELIMITER = '.';

    /**
     * sql通用update set条件分隔符
     */
    char SQL_COMMON_UPDATE_SET_CONDITION_DELIMITER = ',';

    /**
     * 表示空值
     */
    NullValue NULL_VALUE = NullValue.getInstance();

    String EMPTY_STRING = "";

    /**
     * 条件为空
     */
    SqlStringModel EMPTY_SQL_STRING_AND_PARAMETERS = new SqlStringModel(null, null, 0, ValueTypeConstants.NULL_TYPE);

    /**
     * null值
     */
    SqlStringModel NULL_VALUE_SQL_STRING_AND_PARAMETERS = new SqlStringModel(null, null, 1, ValueTypeConstants.NULL_VALUE_MODEL);

}
