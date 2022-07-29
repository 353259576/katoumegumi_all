package cn.katoumegumi.java.sql.handle;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import cn.katoumegumi.java.sql.common.SqlCommon;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.ValueType;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.SqlEquation;
import cn.katoumegumi.java.sql.entity.SqlWhereValue;
import cn.katoumegumi.java.sql.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MysqlHandle {

    private static final SqlStringAndParameters EMPTY_SQLSTRING_AND_PARAMETERS = new SqlStringAndParameters(null, null, 0, 0);

    /**
     * 处理select语句
     *
     * @param selectModel
     * @return
     */
    public static SelectSqlEntity handleSelect(SelectModel selectModel) {


        List<SqlWhereValue> valueList = new ArrayList<>();


        StringBuilder columnSql = new StringBuilder();
        int columnSize = selectModel.getSelect().size();
        for (int i = 0; i < columnSize - 1; i++) {
            ColumnBaseEntity columnBase = selectModel.getSelect().get(i);
            columnSql.append(SQLModelUtils.guardKeyword(columnBase.getAlias()))
                    .append(SqlCommon.SQL_COMMON_DELIMITER)
                    .append(SQLModelUtils.guardKeyword(columnBase.getColumnName()))
                    .append(SqlCommon.SPACE)
                    .append(SQLModelUtils.guardKeyword(columnBase.getAlias() + SqlCommon.PATH_COMMON_DELIMITER + columnBase.getFieldName()))
                    .append(SqlCommon.COMMA);
        }
        ColumnBaseEntity columnBase = selectModel.getSelect().get(columnSize - 1);
        columnSql.append(SQLModelUtils.guardKeyword(columnBase.getAlias()))
                .append(SqlCommon.SQL_COMMON_DELIMITER)
                .append(SQLModelUtils.guardKeyword(columnBase.getColumnName()))
                .append(SqlCommon.SPACE)
                .append(SQLModelUtils.guardKeyword(columnBase.getAlias() + SqlCommon.PATH_COMMON_DELIMITER + columnBase.getFieldName()));

        StringBuilder tableAndJoinTableSql = new StringBuilder();
        tableAndJoinTableSql.append(SqlCommon.FROM)
                .append(SQLModelUtils.guardKeyword(selectModel.getFrom().getTable().getTableName()))
                .append(SqlCommon.SPACE)
                .append(SQLModelUtils.guardKeyword(selectModel.getFrom().getAlias()));
        if (WsListUtils.isNotEmpty(selectModel.getJoinList())) {
            for (JoinTableModel joinTableModel : selectModel.getJoinList()) {
                tableAndJoinTableSql.append(joinTableModel.getJoinType().getValue())
                        .append(SQLModelUtils.guardKeyword(joinTableModel.getJoinTable().getTable().getTableName()))
                        .append(SqlCommon.SPACE)
                        .append(SQLModelUtils.guardKeyword(joinTableModel.getJoinTable().getAlias()))
                        .append(SqlCommon.ON);
                handleRelation(joinTableModel.getOn(), tableAndJoinTableSql, valueList, false);
            }
        }

        StringBuilder whereConditionSql = new StringBuilder();

        if (selectModel.getWhere() != null) {
            whereConditionSql.append(SqlCommon.WHERE);
            handleRelation(selectModel.getWhere(), whereConditionSql, valueList, false);
        }

        StringBuilder orderBySql = new StringBuilder();
        if (WsListUtils.isNotEmpty(selectModel.getOrderBy())) {
            orderBySql.append(SqlCommon.ORDER_BY);
            int orderByModelSize = selectModel.getOrderBy().size() - 1;
            OrderByModel orderByModel;
            ColumnBaseEntity entity;
            for (int i = 0; i < orderByModelSize; i++) {
                orderByModel = selectModel.getOrderBy().get(i);
                entity = orderByModel.getColumn();
                orderBySql.append(SQLModelUtils.guardKeyword(entity.getAlias()))
                        .append(SqlCommon.SQL_COMMON_DELIMITER)
                        .append(SQLModelUtils.guardKeyword(entity.getColumnName()))
                        .append(SqlCommon.SPACE)
                        .append(orderByModel.getType())
                        .append(SqlCommon.COMMA);
            }
            orderByModel = selectModel.getOrderBy().get(orderByModelSize);
            entity = orderByModel.getColumn();
            orderBySql.append(SQLModelUtils.guardKeyword(entity.getAlias()))
                    .append(SqlCommon.SQL_COMMON_DELIMITER)
                    .append(SQLModelUtils.guardKeyword(entity.getColumnName()))
                    .append(SqlCommon.SPACE)
                    .append(orderByModel.getType());

        }

        StringBuilder limitSql = new StringBuilder();
        if(selectModel.getSqlLimit() != null){
            limitSql.append(" limit ")
                    .append(selectModel.getSqlLimit().getOffset())
                    .append(",")
                    .append(selectModel.getSqlLimit().getSize());
        }

        String sql = SqlCommon.SELECT +
                columnSql +
                tableAndJoinTableSql +
                whereConditionSql +
                orderBySql +
                limitSql;

        String countSql = SqlCommon.SELECT +
                "count(*) " +
                tableAndJoinTableSql +
                whereConditionSql;

        SelectSqlEntity entity = new SelectSqlEntity();
        entity.setSelectSql(sql);
        entity.setCountSql(countSql);
        entity.setValueList(valueList);
        return entity;
    }


    private static void handleRelation(ConditionRelationModel conditionRelationModel, StringBuilder sql, List<SqlWhereValue> valueList, boolean needBrackets) {
        if (WsListUtils.isEmpty(conditionRelationModel.getConditionList())) {
            return;
        }
        int conditionSize = conditionRelationModel.getConditionList().size();
        if (conditionSize == 1) {
            Condition condition = conditionRelationModel.getConditionList().get(0);
            if (condition instanceof SingleExpressionCondition) {
                handleSingleExpressionCondition((SingleExpressionCondition) condition, sql, valueList);
            } else if (condition instanceof ConditionRelationModel) {
                handleRelation((ConditionRelationModel) condition, sql, valueList, true);
            } else if (condition instanceof MultiExpressionCondition) {
                handleMultiExpressionCondition((MultiExpressionCondition) condition, sql, valueList);
            }
            return;
        }
        if (needBrackets) {
            sql.append(SqlCommon.LEFT_BRACKETS);
        }
        Condition condition = conditionRelationModel.getConditionList().get(0);
        if (condition instanceof SingleExpressionCondition) {
            handleSingleExpressionCondition((SingleExpressionCondition) condition, sql, valueList);
        } else if (condition instanceof ConditionRelationModel) {
            handleRelation((ConditionRelationModel) condition, sql, valueList, true);
        } else if (condition instanceof MultiExpressionCondition) {
            handleMultiExpressionCondition((MultiExpressionCondition) condition, sql, valueList);
        }

        for (int i = 1; i < conditionSize; i++) {
            sql.append(conditionRelationModel.getRelation().equals(SqlOperator.AND) ? SqlCommon.SQL_AND : SqlCommon.SQL_OR);
            condition = conditionRelationModel.getConditionList().get(i);
            if (condition instanceof SingleExpressionCondition) {
                handleSingleExpressionCondition((SingleExpressionCondition) condition, sql, valueList);
            } else if (condition instanceof ConditionRelationModel) {
                handleRelation((ConditionRelationModel) condition, sql, valueList, true);
            } else if (condition instanceof MultiExpressionCondition) {
                handleMultiExpressionCondition((MultiExpressionCondition) condition, sql, valueList);
            }
        }

        if (needBrackets) {
            sql.append(SqlCommon.RIGHT_BRACKETS);
        }
    }

    private static void handleSingleExpressionCondition(SingleExpressionCondition singleExpressionCondition, StringBuilder sql, List<SqlWhereValue> valueList) {

        SqlStringAndParameters left;
        SqlStringAndParameters right;

        switch (singleExpressionCondition.getSymbol()) {
            case SQL:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                if (left.getSql() != null) {
                    sql.append(left.getSql());
                }
                sqlStringAndParametersValueAddInList(left, valueList);
                break;
            case IN:
            case NOT_IN:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommon.LEFT_BRACKETS);
                if (right.isOnlyValue()) {
                    sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(right, valueList);
                } else {
                    sql.append(right.getSql());
                    sqlStringAndParametersValueAddInList(right, valueList);
                }
                sql.append(SqlCommon.RIGHT_BRACKETS);
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.SQL_AND));
                sqlStringAndParametersValueAddInList(right, valueList);
                break;
            case EXISTS:
            case NOT_EXISTS:
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommon.LEFT_BRACKETS);
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sqlStringAndParametersValueAddInList(left, valueList);
                sql.append(SqlCommon.RIGHT_BRACKETS);
                break;
            case NULL:
            case NOT_NULL:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                break;
            case LIKE:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                String searchKey = WsBeanUtils.convertBean(right.getValue(), String.class);
                if (searchKey == null) {
                    throw new IllegalArgumentException("模糊查询条件不能为空");
                }
                int checkKey = 0;
                if (searchKey.startsWith("%")) {
                    checkKey |= 1;
                }
                if (searchKey.endsWith("%")) {
                    checkKey |= 2;
                }
                switch (checkKey) {
                    case 0:
                        sql.append("concat('%',?,'%')");
                        break;
                    case 1:
                        searchKey = searchKey.substring(1);
                        sql.append("concat('%',?)");
                        break;
                    case 2:
                        sql.append("concat(?,'%')");
                        searchKey = searchKey.substring(0, searchKey.length() - 1);
                        break;
                    case 3:
                        sql.append("concat('%',?,'%')");
                        searchKey = searchKey.substring(1, searchKey.length() - 1);
                        break;
                }
                valueList.add(new SqlWhereValue(null, searchKey));
                break;
            default:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                if (left.isOnlyValue()) {
                    sql.append(createPlaceholder(left.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(left, valueList);
                } else {
                    sql.append(left.getSql());
                }
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                if (right.isOnlyValue()) {
                    sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(right, valueList);
                } else {
                    sql.append(right.getSql());
                }
                break;
        }

    }

    private static void handleMultiExpressionCondition(MultiExpressionCondition multiExpressionCondition, StringBuilder sql, List<SqlWhereValue> valueList) {

        for (int i = 0; i < multiExpressionCondition.getLength(); i++) {
            int type = multiExpressionCondition.getTypes()[i];
            Object value = multiExpressionCondition.getValues()[i];
            SqlStringAndParameters sqlStringAndParameters = handleExpressionConditionValue(type, value);

            boolean addBrackets = true;
            switch (type) {
                case ValueType.NULL_TYPE:
                    continue;
                case ValueType.COLUMN_NAME_TYPE:
                case ValueType.SYMBOL_TYPE:
                case ValueType.BASE_VALUE_TYPE:
                    addBrackets = false;
                    break;
            }
            if (i != 0) {
                sql.append(SqlCommon.SPACE);
            }
            if (addBrackets) {
                sql.append(SqlCommon.LEFT_BRACKETS);
            }
            if (sqlStringAndParameters.isOnlyValue()) {
                sql.append(createPlaceholder(sqlStringAndParameters.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.COMMA));
            } else {
                sql.append(sqlStringAndParameters.getSql());
            }
            sqlStringAndParametersValueAddInList(sqlStringAndParameters, valueList);
            if (addBrackets) {
                sql.append(SqlCommon.RIGHT_BRACKETS);
            }

        }

    }

    private static SqlStringAndParameters handleExpressionConditionValue(int type, Object value) {
        switch (type) {
            case ValueType.NULL_TYPE:
                return EMPTY_SQLSTRING_AND_PARAMETERS;
            case ValueType.BASE_VALUE_TYPE:
                return new SqlStringAndParameters(null, value, 1, ValueType.BASE_VALUE_TYPE);
            case ValueType.COLLECTION_TYPE:
                return new SqlStringAndParameters(null, value, ((Collection<?>) value).size(), ValueType.COLLECTION_TYPE);
            case ValueType.ARRAY_TYPE:
                return new SqlStringAndParameters(null, value, ((Object[]) value).length, ValueType.ARRAY_TYPE);
            case ValueType.SELECT_MODEL_TYPE:
                SelectSqlEntity entity = MysqlHandle.handleSelect((SelectModel) value);
                return new SqlStringAndParameters(entity.getSelectSql(), entity.getValueList().stream().map(SqlWhereValue::getValue).collect(Collectors.toList()), entity.getValueList().size());
            case ValueType.COLUMN_NAME_TYPE:
                ColumnBaseEntity columnBaseEntity = (ColumnBaseEntity) value;
                return new SqlStringAndParameters(
                        SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) + SqlCommon.SQL_COMMON_DELIMITER + SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()),
                        null,
                        0
                );
            case ValueType.SQL_STRING_MODEL_TYPE:
                SqlStringModel sqlStringModel = (SqlStringModel) value;
                return new SqlStringAndParameters(sqlStringModel.getSql(), sqlStringModel.getValue());
            case ValueType.CONDITION_RELATION_MODEL_TYPE:
                StringBuilder stringBuilder = new StringBuilder();
                List<SqlWhereValue> objectList = new ArrayList<>();
                handleRelation((ConditionRelationModel) value, stringBuilder, objectList, true);
                return new SqlStringAndParameters(stringBuilder.toString(), objectList.stream().map(SqlWhereValue::getValue).collect(Collectors.toList()), objectList.size(), ValueType.COLLECTION_TYPE);
            case ValueType.SYMBOL_TYPE:
                return new SqlStringAndParameters(((SqlEquation.Symbol) value).getSymbol(), null);
            case ValueType.SINGLE_EXPRESSION_CONDITION_MODEL:
                StringBuilder singleExpressionConditionSql = new StringBuilder();
                List<SqlWhereValue> singleExpressionConditionSqlWhereValueList = new ArrayList<>();
                handleSingleExpressionCondition((SingleExpressionCondition) value, singleExpressionConditionSql, singleExpressionConditionSqlWhereValueList);
                return new SqlStringAndParameters(singleExpressionConditionSql.toString(), singleExpressionConditionSqlWhereValueList.stream().map(SqlWhereValue::getValue).collect(Collectors.toList()));
            case ValueType.MULTI_EXPRESSION_CONDITION_MODEL:
                StringBuilder multiExpressionConditionSql = new StringBuilder();
                List<SqlWhereValue> multiExpressionConditionSqlWhereValueList = new ArrayList<>();
                handleMultiExpressionCondition((MultiExpressionCondition) value, multiExpressionConditionSql, multiExpressionConditionSqlWhereValueList);
                return new SqlStringAndParameters(multiExpressionConditionSql.toString(), multiExpressionConditionSqlWhereValueList.stream().map(SqlWhereValue::getValue).collect(Collectors.toList()));
            default:
                throw new IllegalArgumentException("不支持的类:" + value.getClass());
        }
    }


    /**
     * 将value加入列表
     *
     * @param valueList
     */
    private static void sqlStringAndParametersValueAddInList(SqlStringAndParameters sqlStringAndParameters, List<SqlWhereValue> valueList) {

        switch (sqlStringAndParameters.getValueType()) {
            case ValueType.BASE_VALUE_TYPE:
                valueList.add(new SqlWhereValue(null, sqlStringAndParameters.getValue()));
                break;
            case ValueType.COLLECTION_TYPE:
                Collection<?> collection = (Collection<?>) sqlStringAndParameters.getValue();
                if (WsListUtils.isNotEmpty(collection)) {
                    for (Object o : collection) {
                        valueList.add(new SqlWhereValue(null, o));
                    }
                }
                break;
            case ValueType.ARRAY_TYPE:
                Object[] objects = (Object[]) sqlStringAndParameters.getValue();
                if (WsListUtils.isNotEmpty(objects)) {
                    for (Object o : objects) {
                        valueList.add(new SqlWhereValue(null, o));
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 生成由分隔符分割的任意数量的占位符字符串
     *
     * @param placeholderSize 占位符数量
     * @param placeholderStr  占位符
     * @param separator       分隔符
     * @return
     */
    private static String createPlaceholder(int placeholderSize, String placeholderStr, String separator) {
        if (placeholderSize == 0) {
            return "";
        }
        if (placeholderSize == 1) {
            return placeholderStr;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < placeholderSize - 1; i++) {
            stringBuilder.append(placeholderStr)
                    .append(separator);
        }
        stringBuilder.append(placeholderStr);
        return stringBuilder.toString();
    }


}
